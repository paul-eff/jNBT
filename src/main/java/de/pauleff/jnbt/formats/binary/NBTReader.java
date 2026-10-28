package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.INBTReader;
import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.internal.NBTBudget;
import de.pauleff.jnbt.util.NBTTags;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Reads binary NBT and adds the stream position to parse errors. Uses {@link NBTLimits#DEFAULT}
 * unless limits are supplied to the constructor.
 *
 * @since 0.1
 */
public class NBTReader implements INBTReader
{
    private final PositionTrackingDataInputStream stream;
    private final NBTBudget budget;

    /**
     * Creates a reader by passing it the target NBT file.
     *
     * @param nbtFile the target NBT file.
     */
    public NBTReader(File nbtFile)
    {
        this(nbtFile, NBTLimits.DEFAULT);
    }

    /**
     * Creates a file reader with configurable limits applied after decompression.
     *
     * @param nbtFile the target NBT file
     * @param limits  the limits for this operation
     * @throws NullPointerException if limits is null
     * @throws RuntimeException     if the file stream or decompression wrapper cannot be opened
     */
    public NBTReader(File nbtFile, NBTLimits limits)
    {
        this.budget = new NBTBudget(limits);
        try
        {
            DataInputStream baseStream = NBTFileHandler.loadNBTToReader(nbtFile);
            this.stream = new PositionTrackingDataInputStream(baseStream, limits.maxBytes());
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
        this(dis, NBTLimits.DEFAULT);
    }

    /**
     * Creates a stream reader with configurable limits. The input must already be decompressed.
     *
     * @param dis    the binary NBT input stream, closed after reading
     * @param limits the limits for this operation
     * @throws NullPointerException if limits is null
     */
    public NBTReader(DataInputStream dis, NBTLimits limits)
    {
        this.budget = new NBTBudget(limits);
        this.stream = new PositionTrackingDataInputStream(dis, limits.maxBytes());
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

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException
    {
        stream.close();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Closes the reader after reading, including on failure.</p>
     *
     * @throws IOException if parsing fails or a configured limit is exceeded
     */
    public ICompoundTag read() throws IOException
    {
        try
        {
            return (Tag_Compound) readNBTTag(0, -1);
        } catch (IOException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new IOException(stream.createContextualError("Failed to parse NBT data: " + e.getMessage()), e);
        } finally
        {
            this.close();
        }
    }

    /**
     * Reserves array storage and verifies declared payload bytes before allocation.
     *
     * @param length the declared number of elements
     * @param width  the bytes per element (1, 4, or 8)
     * @throws IOException              if the payload exceeds the remaining binary byte budget
     * @throws IllegalArgumentException if the length is invalid or the allocation limit is exceeded
     */
    private void reserveArray(int length, int width) throws IOException
    {
        budget.length(length);
        long bytes = (long) length * width;
        stream.checkAvailable(bytes);
        budget.allocate(16 + bytes);
    }

    /**
     * Reserves encoded and decoded string storage before reading the payload.
     *
     * @return the decoded modified UTF-8 string
     * @throws IOException              if the field is truncated, malformed, or exceeds the binary byte budget
     * @throws IllegalArgumentException if the allocation limit is exceeded
     */
    private String readString() throws IOException
    {
        int length = stream.readUnsignedShort();
        budget.allocate(64 + 6L * length);
        return stream.readUTF(length);
    }

    /**
     * Reads an entire NBT tag including its name header.
     *
     * @param depth    the current nesting depth for validation and error reporting
     * @param siblings the number of preceding compound children, or -1 at the root
     * @return the complete tag read from the stream
     * @throws IOException              if tag structure is invalid, the stream is corrupted, or the byte limit is exceeded
     * @throws IllegalArgumentException if a collection or allocation limit is exceeded
     */
    private Tag<?> readNBTTag(int depth, int siblings) throws IOException
    {
        try
        {
            int type = stream.readByte();
            String name = "";
            stream.setCurrentContext(depth, name, type);

            if (type != NBTTags.Tag_End.getId())
            {
                if (siblings >= 0) budget.length(siblings + 1);
                name = readString();
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
     * @throws IOException              if the stream or payload is invalid or the byte limit is exceeded
     * @throws IllegalArgumentException if a depth, count, length, or allocation limit is exceeded
     */
    private Tag<?> readNBTPayload(int type, String name, int depth) throws IOException
    {
        NBTTags tagType = NBTTags.getById(type);
        if (tagType == null)
        {
            throw new IOException(stream.createContextualError(
                    String.format("Unknown tag type: %d", type)));
        }

        stream.setCurrentContext(depth, name, type);
        if (type != 0) budget.tag();
        if (type == 9 || type == 10) budget.depth(depth + 1);

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
                reserveArray(arrayLength, 1);
                byte[] byteBuffer = new byte[arrayLength];
                stream.readFully(byteBuffer);
                return new Tag_Byte_Array(name, byteBuffer);
            case Tag_String:
                return new Tag_String(name, readString());
            case Tag_List:
                int listType = stream.readByte();
                if (NBTTags.getById(listType) == null && listType != 0) // 0 is valid for empty lists
                {
                    throw new IOException(stream.createContextualError(
                            String.format("Invalid list element type: %d", listType)));
                }
                arrayLength = stream.readInt();
                budget.length(arrayLength);
                budget.requireTags(arrayLength);
                budget.allocate(16L * arrayLength);
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
                    Tag<?> tag = readNBTTag(depth + 1, tagList.size());
                    if (tag.getId() == NBTTags.Tag_End.getId()) break;
                    budget.allocate(64);
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
                reserveArray(arrayLength, 4);
                int[] intArray = new int[arrayLength];
                for (int i = 0; i < arrayLength; i++)
                {
                    intArray[i] = stream.readInt();
                }
                return new Tag_Int_Array(name, intArray);
            case Tag_Long_Array:
                arrayLength = stream.readInt();
                reserveArray(arrayLength, 8);
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
