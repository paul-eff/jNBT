package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.INBTWriter;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.internal.NBTBudget;
import de.pauleff.jnbt.util.NBTTags;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *  Writes binary NBT after validating the tag tree. Uses {@link NBTLimits#DEFAULT}
 * unless limits are supplied to the constructor.
 *
 * @since 0.1
 */
public class NBTWriter implements INBTWriter
{
    private DataOutputStream stream;
    private final File file;
    private final Compression_Types compression;
    private boolean closed;
    private final NBTLimits limits;

    /**
     * Creates a writer by passing it the target NBT file. Target file must already exist to determine the
     * compression type.
     *
     * @param nbtFile the target NBT file.
     *
     * @throws FileNotFoundException if the file does not exist
     */
    public NBTWriter(File nbtFile) throws FileNotFoundException
    {
        this(nbtFile, NBTLimits.DEFAULT);
    }

    /**
     * Creates a writer that detects existing file compression and uses configurable limits.
     *
     * @param nbtFile the existing target file
     * @param limits  the limits for this operation
     * @throws FileNotFoundException if the file does not exist
     * @throws NullPointerException if limits is null
     */
    public NBTWriter(File nbtFile, NBTLimits limits) throws FileNotFoundException
    {
        this.limits = java.util.Objects.requireNonNull(limits, "limits");
        if (Files.notExists(nbtFile.toPath()))
        {
            throw new FileNotFoundException(String.format("The file %s doesn't exist. It's needed to determine the compression type!", nbtFile.getPath()));
        }
        this.file = nbtFile;
        this.compression = NBTFileHandler.getCompressionType(nbtFile);
    }

    /**
     * Creates a writer by passing it the target NBT file and how to compress it.
     *
     * @param nbtFile     the target NBT file.
     * @param compression the compression type of the file.
     */
    public NBTWriter(File nbtFile, Compression_Types compression)
    {
        this(nbtFile, compression, NBTLimits.DEFAULT);
    }

    /**
     * Creates a file writer with explicit compression and configurable limits.
     *
     * @param nbtFile     the target file, opened only after validation succeeds
     * @param compression the compression type
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
     * Creates a writer by passing it the target NBT file as a {@link DataOutputStream}.
     *
     * @param dos a {@link DataOutputStream} containing a NBT file.
     */
    public NBTWriter(DataOutputStream dos)
    {
        this(dos, NBTLimits.DEFAULT);
    }

    /**
     * Creates a stream writer with configurable limits on the uncompressed NBT data.
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
     * {@inheritDoc}
     *
     * <p>Closing an unused file writer does not create a file.</p>
     */
    public void close() throws IOException
    {
        closed = true;
        if (stream != null)
        {
            stream.close();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Validates names and strings against the 65,535-byte modified UTF-8 limit
     * before opening the file. Rejects END values, invalid lists, duplicate keys, cycles, and exceeded limits.
     * Do not change the tree during writing. Closes the writer after validation
     * or writing fails, and after a successful write.</p>
     *
     * @throws IOException if writing fails, the data is invalid, or a configured limit is exceeded
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public void write(ICompoundTag root) throws IOException
    {
        if (root == null)
        {
            throw new IllegalArgumentException("Root compound tag cannot be null");
        }
        try
        {
            if (closed)
            {
                throw new IOException("NBT writer is closed");
            }
            validateForWrite(root, true, 0, new NBTBudget(limits),
                    Collections.newSetFromMap(new IdentityHashMap<>()));
            if (stream == null)
            {
                stream = NBTFileHandler.loadNBTToWriter(file, compression);
            }
            writeNBTTag(root);
        } catch (IOException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new IOException("Failed to write NBT data: " + e.getMessage(), e);
        } finally
        {
            this.close();
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
     * @throws IOException if the structure, encoded strings, or collection graph is invalid
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
            for (ITag<?> child : (Iterable<ITag<?>>) children)
            {
                if (child != null && child.getId() == 0)
                {
                    throw new IOException("TAG_End is a structural marker, not a value");
                }
                if (child == null || (type == NBTTags.Tag_List && child.getId() != listType))
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

    /** Writes a tag with its type ID, name, and payload. */
    private void writeNBTTag(ITag<?> tag) throws IOException
    {
        if (tag.getId() == 0)
        {
            throw new IOException("TAG_End cannot be written as a named value");
        }
        stream.writeByte(tag.getId());
        stream.writeUTF(tag.getName());

        writeNBTPayload(tag);
    }

    /** Writes a tag payload without a type or name header. */
    private void writeNBTPayload(ITag<?> tag) throws IOException
    {
        switch (NBTTags.getById(tag.getId()))
        {
            case NBTTags.Tag_End:
                throw new IOException("TAG_End cannot be written as a value");
            case NBTTags.Tag_Byte:
                stream.writeByte((byte) tag.getData());
                break;
            case NBTTags.Tag_Short:
                stream.writeShort((short) tag.getData());
                break;
            case NBTTags.Tag_Int:
                stream.writeInt((int) tag.getData());
                break;
            case NBTTags.Tag_Long:
                stream.writeLong((long) tag.getData());
                break;
            case NBTTags.Tag_Float:
                stream.writeFloat((float) tag.getData());
                break;
            case NBTTags.Tag_Double:
                stream.writeDouble((double) tag.getData());
                break;
            case NBTTags.Tag_Byte_Array:
                byte[] bytes = (byte[]) tag.getData();
                stream.writeInt(bytes.length);
                stream.write(bytes);
                break;
            case NBTTags.Tag_String:
                stream.writeUTF((String) tag.getData());
                break;
            case NBTTags.Tag_List:
                ArrayList<ITag<?>> listTags = (ArrayList<ITag<?>>) tag.getData();
                int size = listTags.size();

                stream.writeByte(((IListTag) tag).getListTypeID());
                stream.writeInt(size);
                for (int i = 0; i < size; i++)
                {
                    writeNBTPayload(listTags.get(i));
                }
                break;
            case NBTTags.Tag_Compound:
                for (ITag<?> compTag : (ArrayList<ITag<?>>) tag.getData())
                {
                    writeNBTTag(compTag);
                }

                // A zero type ID ends the compound.
                stream.writeByte((byte) 0);
                break;
            case NBTTags.Tag_Int_Array:
                int[] intArray = (int[]) tag.getData();
                size = intArray.length;
                stream.writeInt(size);
                for (int i = 0; i < size; i++)
                {
                    stream.writeInt(intArray[i]);
                }
                break;
            case NBTTags.Tag_Long_Array:
                long[] longArray = (long[]) tag.getData();
                size = longArray.length;
                stream.writeInt(size);
                for (int i = 0; i < size; i++)
                {
                    stream.writeLong(longArray[i]);
                }
                break;
            case null:
            default:
                throw new IOException("Invalid tag type: " + tag.getId() + ".");
        }
    }
}
