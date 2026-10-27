package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.INBTReader;
import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.util.NBTTags;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *  Reads binary NBT and adds the stream position to parse errors.
 *
 * @since 0.1
 */
public class NBTReader implements INBTReader
{
    private final PositionTrackingDataInputStream stream;

    /**
     * Creates a reader by passing it the target NBT file.
     *
     * @param nbtFile the target NBT file.
     */
    public NBTReader(File nbtFile)
    {
        try
        {
            DataInputStream baseStream = NBTFileHandler.loadNBTToReader(nbtFile);
            this.stream = new PositionTrackingDataInputStream(baseStream);
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to initialize NBT reader for file: " +
                    nbtFile.getAbsolutePath(), e);
        }
    }

    /**
     * Creates a reader by passing it a {@link DataInputStream}.
     *
     * @param dis a {@link DataInputStream} containing a NBT file.
     */
    public NBTReader(DataInputStream dis)
    {
        this.stream = new PositionTrackingDataInputStream(dis);
    }

    /**
     * Wraps a byte array in an input stream that counts bytes read.
     *
     * @param chunkData the byte array to convert
     * @return a {@link PositionTrackingDataInputStream} with a byte count
     */
    public static PositionTrackingDataInputStream byteArrayToDataInputStream(byte[] chunkData)
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(chunkData);
        return new PositionTrackingDataInputStream(byteArrayInputStream);
    }

    /** {@inheritDoc} */
    public void close() throws IOException
    {
        stream.close();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the reader after reading, including on failure.</p>
     */
    public ICompoundTag read() throws IOException
    {
        try
        {
            Tag_Compound tag = (Tag_Compound) readNBTTag(0);
            return tag;
        } catch (IOException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new IOException("Failed to parse NBT data: " + e.getMessage(), e);
        } finally
        {
            this.close();
        }
    }

    /**
     * Reads an entire NBT tag including its name header.
     *
     * @param depth the current nesting depth for validation and error reporting
     * @return the complete tag read from the stream
     * @throws IOException if tag structure is invalid or stream is corrupted
     */
    private Tag readNBTTag(int depth) throws IOException
    {
        try
        {
            int type = stream.readByte();
            String name = "";
            stream.setCurrentContext(depth, name, type);

            if (type != NBTTags.Tag_End.getId())
            {
                name = stream.readUTF();
            }

            stream.setCurrentContext(depth, name, type);
            return readNBTPayload(type, name, depth);
        } catch (IOException e)
        {
            if (e.getMessage() != null && e.getMessage().contains("[Position:"))
            {
                throw e;
            }
            throw new IOException(stream.createContextualError("Failed to read NBT tag: " + e.getMessage()), e);
        }
    }

    /**
     * Reads a tag payload without a name header.
     *
     * @param type  the NBT type ID
     * @param name  the name of the NBT tag.
     * @param depth the current nesting depth
     * @return the complete read NBT tag.
     * @throws IOException if the stream or payload is invalid
     */
    private Tag readNBTPayload(int type, String name, int depth) throws IOException
    {
        NBTTags tagType = NBTTags.getById(type);
        if (tagType == null)
        {
            throw new IOException(stream.createContextualError(
                    String.format("Unknown tag type: %d", type)));
        }

        switch (tagType)
        {
            case Tag_End:
                if (depth == 0)
                {
                    throw new IOException(stream.createContextualError(
                            "Unexpected Tag_End at root level - NBT files must start with a compound tag"));
                }
                return new Tag_End();
            case Tag_Byte:
                return new Tag_Byte(name, stream.readByte());
            case Tag_Short:
                return new Tag_Short(name, stream.readShort());
            case Tag_Int:
                return new Tag_Int(name, stream.readInt());
            case Tag_Long:
                return new Tag_Long(name, stream.readLong());
            case Tag_Float:
                return new Tag_Float(name, stream.readFloat());
            case Tag_Double:
                return new Tag_Double(name, stream.readDouble());
            case Tag_Byte_Array:
                int arrayLength = stream.readInt();
                if (arrayLength < 0 || arrayLength > 100_000_000) // 100MB limit
                {
                    throw new IOException(stream.createContextualError(
                            String.format("Invalid byte array length: %d", arrayLength)));
                }
                byte[] byteBuffer = new byte[arrayLength];
                stream.readFully(byteBuffer);
                return new Tag_Byte_Array(name, byteBuffer);
            case Tag_String:
                return new Tag_String(name, stream.readUTF());
            case Tag_List:
                int listType = stream.readByte();
                if (NBTTags.getById(listType) == null && listType != 0) // 0 is valid for empty lists
                {
                    throw new IOException(stream.createContextualError(
                            String.format("Invalid list element type: %d", listType)));
                }
                arrayLength = stream.readInt();
                if (arrayLength < 0 || arrayLength > 10_000_000) // 10M elements max
                {
                    throw new IOException(stream.createContextualError(
                            String.format("Invalid list length: %d", arrayLength)));
                }
                if (listType == 0 && arrayLength > 0)
                {
                    throw new IOException(stream.createContextualError(
                            "TAG_End list type is only valid for empty lists"));
                }
                ArrayList<Tag<?>> tagList = new ArrayList<>(Math.min(arrayLength, 1000)); // Limit the initial allocation for large lists.
                for (int i = 0; i < arrayLength; i++)
                {
                    tagList.add(readNBTPayload(listType, "", depth + 1));
                }
                return new Tag_List(name, listType, tagList);
            case Tag_Compound:
                tagList = new ArrayList<>(16);
                Set<String> names = new HashSet<>();
                while (true)
                {
                    Tag tag = readNBTTag(depth + 1);
                    if (tag.getId() == NBTTags.Tag_End.getId()) break;
                    if (!names.add(tag.getName()))
                    {
                        throw new IOException(stream.createContextualError(
                                "Duplicate compound key: " + tag.getName()));
                    }
                    tagList.add(tag);
                }
                return new Tag_Compound(name, tagList);
            case Tag_Int_Array:
                arrayLength = stream.readInt();
                int[] intArray = new int[arrayLength];
                for (int i = 0; i < arrayLength; i++)
                {
                    intArray[i] = stream.readInt();
                }
                return new Tag_Int_Array(name, intArray);
            case Tag_Long_Array:
                arrayLength = stream.readInt();
                long[] longArray = new long[arrayLength];
                for (int i = 0; i < arrayLength; i++)
                {
                    longArray[i] = stream.readLong();
                }
                return new Tag_Long_Array(name, longArray);
            case null:
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
