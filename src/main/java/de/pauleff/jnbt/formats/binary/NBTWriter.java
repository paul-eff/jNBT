package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.INBTWriter;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.util.NBTTags;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class for handling the writing of Java NBT to file.
 *
 */
public class NBTWriter implements INBTWriter
{
    private DataOutputStream stream;
    private final File file;
    private final Compression_Types compression;
    private boolean closed;

    /**
     * Create a writer by passing it the target NBT file.
     * Target file must already exist to determine the compression type.
     * The destination is opened and backed up by {@link #write(ICompoundTag)}
     * after encoded string lengths have been validated.
     *
     * @param nbtFile The target NBT file.
     */
    public NBTWriter(File nbtFile) throws FileNotFoundException
    {
        if (Files.notExists(nbtFile.toPath()))
        {
            throw new FileNotFoundException(String.format("The file %s doesn't exist. It's needed to determine the compression type!", nbtFile.getPath()));
        }
        this.file = nbtFile;
        this.compression = NBTFileHandler.getCompressionType(nbtFile);
    }

    /**
     * Create a writer by passing it the target NBT file and how to compress it.
     * The destination is opened and, if it exists, backed up by
     * {@link #write(ICompoundTag)} after encoded string lengths have been validated.
     *
     * @param nbtFile     The target NBT file.
     * @param compression The compression type of the file.
     */
    public NBTWriter(File nbtFile, Compression_Types compression)
    {
        this.file = nbtFile;
        this.compression = compression;
    }

    /**
     * Create a writer by passing it the target NBT file as a {@link DataOutputStream}.
     *
     * @param dos A {@link DataOutputStream} containing a NBT file.
     */
    public NBTWriter(DataOutputStream dos)
    {
        this.stream = dos;
        this.file = null;
        this.compression = null;
    }

    /**
     * Method to close the writer.
     *
     * @throws IOException When encountering an error whilst closing the writer.
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
     * Writes the compound tag structure to file and closes the writer.
     * The NBT specification requires compound tags as file roots, so this method enforces that constraint.
     * All stored names and string values are checked against the 65,535-byte
     * modified UTF-8 limit before output starts. Explicit TAG_End values and
     * nonempty END-typed lists are also rejected before output starts.
     * The tree must not be mutated during this operation.
     *
     * @param root The root {@link ICompoundTag} containing the complete NBT structure to write
     * @throws IOException              If an error occurs during file writing or if the data is invalid
     * @throws IllegalArgumentException If root is null
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
            validateForWrite(root, true);
            if (stream == null)
            {
                stream = NBTFileHandler.loadNBTToWriter(file, compression);
            }
            writeNBTTag(root);
        } catch (IOException e)
        {
            throw e; // Re-throw IOException directly
        } catch (Exception e)
        {
            throw new IOException("Failed to write NBT data: " + e.getMessage(), e);
        } finally
        {
            this.close();
        }
    }

    private void validateForWrite(ITag<?> tag, boolean named) throws IOException
    {
        NBTTags type = NBTTags.getById(tag.getId());
        if (type == NBTTags.Tag_End)
        {
            throw new IOException("TAG_End is a structural marker, not a value");
        }
        if (named)
        {
            validateUTFLength(tag.getName(), "Tag name");
        }
        if (type == NBTTags.Tag_String)
        {
            validateUTFLength((String) tag.getData(), "String payload");
        } else if (type == NBTTags.Tag_Compound || type == NBTTags.Tag_List)
        {
            if (type == NBTTags.Tag_List && ((IListTag) tag).getListTypeID() == 0
                    && !((Collection<?>) tag.getData()).isEmpty())
            {
                throw new IOException("TAG_End list type is only valid for empty lists");
            }
            for (ITag<?> child : (Iterable<ITag<?>>) tag.getData())
            {
                validateForWrite(child, type == NBTTags.Tag_Compound);
            }
        }
    }

    private void validateUTFLength(String value, String field) throws UTFDataFormatException
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
    }

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
                // Simulate Tag_End
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
