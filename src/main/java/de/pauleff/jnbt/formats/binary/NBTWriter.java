package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.*;
import de.pauleff.jnbt.internal.NBTBudget;
import de.pauleff.jnbt.util.NBTTags;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Writes validated binary NBT to a file or stream. Uses {@link NBTLimits#DEFAULT}
 * unless explicit limits are supplied.
 *
 * @since 0.1
 */
public class NBTWriter implements INBTWriter
{
    private final File file;
    private final Compression_Types compression;
    private final NBTLimits limits;
    private DataOutputStream stream;
    private boolean closed;

    /**
     * Creates a file writer with compression detected from the existing file.
     *
     * @param nbtFile the existing destination file
     * @throws FileNotFoundException if the file does not exist
     */
    public NBTWriter(File nbtFile) throws FileNotFoundException
    {
        this(nbtFile, NBTLimits.DEFAULT);
    }

    /**
     * Creates a file writer with detected compression and explicit limits.
     *
     * @param nbtFile the existing target file
     * @param limits  the limits for this operation
     * @throws FileNotFoundException if the file does not exist
     * @throws NullPointerException  if limits is null
     */
    public NBTWriter(File nbtFile, NBTLimits limits) throws FileNotFoundException
    {
        this.limits = java.util.Objects.requireNonNull(limits, "limits");
        if (Files.notExists(nbtFile.toPath()))
        {
            throw new FileNotFoundException(String.format("The file %s doesn't exist. It's needed to determine the compression type!", nbtFile.getPath()));
        }
        this.file = nbtFile;
        this.compression = NBTFileFactory.getCompressionType(nbtFile);
    }

    /**
     * Creates a file writer with explicit compression.
     *
     * @param nbtFile     the destination file
     * @param compression the output compression
     */
    public NBTWriter(File nbtFile, Compression_Types compression)
    {
        this(nbtFile, compression, NBTLimits.DEFAULT);
    }

    /**
     * Creates a file writer with explicit compression and limits.
     *
     * @param nbtFile     the destination file, replaced only after writing succeeds
     * @param compression the output compression
     * @param limits      the limits for this operation
     * @throws NullPointerException if limits is null
     */
    public NBTWriter(File nbtFile, Compression_Types compression, NBTLimits limits)
    {
        this.limits = java.util.Objects.requireNonNull(limits, "limits");
        this.file = nbtFile;
        this.compression = compression;
    }

    /**
     * Creates a writer for the supplied output stream.
     *
     * @param dos the output stream, closed after writing
     */
    public NBTWriter(DataOutputStream dos)
    {
        this(dos, NBTLimits.DEFAULT);
    }

    /**
     * Creates a stream writer with explicit limits.
     *
     * @param dos    the binary output stream, closed after writing or validation failure
     * @param limits the limits for this operation
     * @throws NullPointerException if limits is null
     */
    public NBTWriter(DataOutputStream dos, NBTLimits limits)
    {
        this.limits = java.util.Objects.requireNonNull(limits, "limits");
        this.stream = dos;
        this.file = null;
        this.compression = null;
    }

    /**
     * Closes the writer and its output stream. Repeated calls have no effect.
     * Closing an unused file writer does not create a file.
     *
     * @throws IOException if closing the stream fails
     */
    public void close() throws IOException
    {
        closed = true;
        DataOutputStream output = stream;
        stream = null;
        if (output != null)
        {
            output.close();
        }
    }

    /**
     * Validates and writes the root compound. Closes this writer after the operation.
     * Do not modify the tag tree during writing.
     *
     * <p>File saves finish temporary output before atomically replacing the destination.
     * Existing files are copied to {@code .bak} before writing; the backup is deleted
     * on success and retained on failure. New files leave existing backups untouched.
     * Unsupported atomic moves fail without fallback.</p>
     *
     * <p>Destination and backup paths must be regular files, not symbolic links.
     * Callers must coordinate concurrent access. Replacement does not preserve file
     * identity or all metadata, or guarantee power-loss durability.
     * Supplied streams are written directly and closed.</p>
     *
     * @param root the root compound
     * @throws NBTSaveCleanupException   if saving succeeded but backup deletion failed
     * @throws IOException              if validation, writing, or closing fails
     * @throws IllegalArgumentException if root is null
     */
    public void write(ICompoundTag root) throws IOException
    {
        if (root == null)
        {
            throw new IllegalArgumentException("Root compound tag cannot be null");
        }
        try (this)
        {
            if (closed)
            {
                throw new IOException("NBT writer is closed");
            }
            validateForWrite(root, true, 0, new NBTBudget(limits),
                    Collections.newSetFromMap(new IdentityHashMap<>()));
            if (file != null)
            {
                NBTFileSave.write(file.toPath(), compression, output -> writeNBTTag(root, output));
            } else
            {
                writeNBTTag(root, stream);
            }
        } catch (IOException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new IOException("Failed to write NBT data: " + e.getMessage(), e);
        }
    }

    /**
     * Checks the whole tree before output. List element names are skipped.
     *
     * @param tag    the tag to validate
     * @param named  true if the tag has a name on wire
     * @param depth  the number of ancestor containers, zero at the root
     * @param budget the shared accounting for this write
     * @param active the collection identities on the current traversal path
     * @throws IOException              if the structure, encoded strings, or collection graph is invalid
     * @throws IllegalArgumentException if a configured limit is exceeded
     */
    private void validateForWrite(ITag<?> tag, boolean named, int depth, NBTBudget budget,
                                  Set<ITag<?>> active) throws IOException
    {
        budget.tag();
        NBTTags type = NBTTags.getById(tag.getId());
        if (type == null) throw new IOException("Invalid tag type: " + tag.getId());
        if (type == NBTTags.Tag_End)
        {
            throw new IOException("TAG_End is a structural marker, not a value");
        }
        if (named)
        {
            budget.bytes(3L + validateUTFLength(tag.getName(), "Tag name"));
        }
        if (type == NBTTags.Tag_String)
        {
            budget.bytes(2L + validateUTFLength((String) tag.getData(), "String payload"));
        } else if (type == NBTTags.Tag_Compound || type == NBTTags.Tag_List)
        {
            budget.depth(depth + 1);
            if (!active.add(tag)) throw new IOException("Cyclic NBT collection graph");
            int length = type == NBTTags.Tag_List ? ((IListTag) tag).size() : ((ICompoundTag) tag).size();
            budget.length(length);
            budget.requireTags(length);
            budget.allocate(64L * length);
            Collection<?> children = (Collection<?>) tag.getData();
            budget.bytes(type == NBTTags.Tag_List ? 5 : 1);
            int listType = type == NBTTags.Tag_List ? ((IListTag) tag).getListTypeID() : -1;
            if (type == NBTTags.Tag_List && NBTTags.getById(listType) == null)
            {
                throw new IOException("Invalid list element type: " + listType);
            }
            if (listType == 0 && !children.isEmpty())
            {
                throw new IOException("TAG_End list type is only valid for empty lists");
            }
            Set<String> names = new HashSet<>();
            for (Object value : children)
            {
                if (!(value instanceof ITag<?> child))
                {
                    throw new IOException("Invalid collection child");
                }
                if (child.getId() == 0)
                {
                    throw new IOException("TAG_End is a structural marker, not a value");
                }
                if (type == NBTTags.Tag_List && child.getId() != listType)
                {
                    throw new IOException("Invalid collection child or list element type mismatch");
                }
                if (type == NBTTags.Tag_Compound && !names.add(child.getName()))
                {
                    throw new IOException("Duplicate compound key: " + child.getName());
                }
                validateForWrite(child, type == NBTTags.Tag_Compound, depth + 1, budget, active);
            }
            active.remove(tag);
        } else if (type == NBTTags.Tag_Byte_Array || type == NBTTags.Tag_Int_Array || type == NBTTags.Tag_Long_Array)
        {
            int length = java.lang.reflect.Array.getLength(tag.getData());
            budget.length(length);
            long bytes = (long) length * (type == NBTTags.Tag_Byte_Array ? 1 : type == NBTTags.Tag_Int_Array ? 4 : 8);
            budget.allocate(16 + bytes);
            budget.bytes(4 + bytes);
        } else
        {
            budget.bytes(switch (type)
            {
                case Tag_Byte -> 1;
                case Tag_Short -> 2;
                case Tag_Int, Tag_Float -> 4;
                case Tag_Long, Tag_Double -> 8;
                default -> throw new IOException("Invalid value type: " + type);
            });
        }
    }

    /**
     * Rejects strings longer than 65,535 bytes in modified UTF-8.
     *
     * @param value the string to validate
     * @param field the field description used in error messages
     * @return the encoded byte length
     * @throws UTFDataFormatException if the encoded length exceeds 65,535 bytes
     */
    private int validateUTFLength(String value, String field) throws UTFDataFormatException
    {
        int length = 0;
        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            // Modified UTF-8 encodes NUL in two bytes and each surrogate in three.
            length += c >= 1 && c <= 0x7f ? 1 : c <= 0x7ff ? 2 : 3;
            if (length > 65535)
            {
                throw new UTFDataFormatException(field + " exceeds the modified UTF-8 limit of 65535 bytes");
            }
        }
        return length;
    }

    /**
     * Writes a tag with its type ID, name, and payload.
     */
    private void writeNBTTag(ITag<?> tag, DataOutputStream stream) throws IOException
    {
        if (tag.getId() == 0)
        {
            throw new IOException("TAG_End cannot be written as a named value");
        }
        stream.writeByte(tag.getId());
        stream.writeUTF(tag.getName());

        writeNBTPayload(tag, stream);
    }

    /**
     * Writes a tag payload without a type or name header.
     */
    private void writeNBTPayload(ITag<?> tag, DataOutputStream stream) throws IOException
    {
        switch (NBTTags.getById(tag.getId()))
        {
            case Tag_End -> throw new IOException("TAG_End cannot be written as a value");
            case Tag_Byte -> stream.writeByte((byte) tag.getData());
            case Tag_Short -> stream.writeShort((short) tag.getData());
            case Tag_Int -> stream.writeInt((int) tag.getData());
            case Tag_Long -> stream.writeLong((long) tag.getData());
            case Tag_Float -> stream.writeFloat((float) tag.getData());
            case Tag_Double -> stream.writeDouble((double) tag.getData());
            case Tag_Byte_Array ->
            {
                byte[] bytes = (byte[]) tag.getData();
                stream.writeInt(bytes.length);
                stream.write(bytes);
            }
            case Tag_String -> stream.writeUTF((String) tag.getData());
            case Tag_List ->
            {
                var listTags = ((IListTag) tag).getData();
                int size = listTags.size();

                stream.writeByte(((IListTag) tag).getListTypeID());
                stream.writeInt(size);
                for (int i = 0; i < size; i++)
                {
                    writeNBTPayload(listTags.get(i), stream);
                }
            }
            case Tag_Compound ->
            {
                for (ITag<?> compTag : ((ICompoundTag) tag).getData())
                {
                    writeNBTTag(compTag, stream);
                }

                // A zero type ID ends the compound.
                stream.writeByte((byte) 0);
            }
            case Tag_Int_Array ->
            {
                int[] intArray = (int[]) tag.getData();
                int size = intArray.length;
                stream.writeInt(size);
                for (int j : intArray)
                {
                    stream.writeInt(j);
                }
            }
            case Tag_Long_Array ->
            {
                long[] longArray = (long[]) tag.getData();
                int size = longArray.length;
                stream.writeInt(size);
                for (long l : longArray)
                {
                    stream.writeLong(l);
                }
            }
            case null, default -> throw new IOException("Invalid tag type: " + tag.getId() + ".");
        }
    }
}
