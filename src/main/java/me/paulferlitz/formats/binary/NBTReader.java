package me.paulferlitz.formats.binary;

import me.paulferlitz.api.ICompoundTag;
import me.paulferlitz.api.INBTReader;
import me.paulferlitz.core.*;
import me.paulferlitz.util.NBTTags;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Class for handling the parsing and formatting of a Java NBT file.
 *
 * @author Paul Ferlitz
 */
public class NBTReader implements INBTReader
{
    private final PositionTrackingDataInputStream stream;

    /**
     * Create a reader by passing it the target NBT file.
     *
     * @param nbtFile The target NBT file.
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
     * Create a reader by passing it a {@link DataInputStream}.
     *
     * @param dis A {@link DataInputStream} containing a NBT file.
     */
    public NBTReader(DataInputStream dis)
    {
        this.stream = new PositionTrackingDataInputStream(dis);
    }

    /**
     * Method to close the reader.
     *
     * @throws IOException When encountering an error whilst closing the reader.
     */
    public void close() throws IOException {
        stream.close();
    }

    /**
     * Converts a byte array into a position-tracking DataInputStream for NBT reading.
     * 
     * @param chunkData The byte array to convert
     * @return A {@link PositionTrackingDataInputStream} for enhanced error reporting
     */
    public static PositionTrackingDataInputStream byteArrayToDataInputStream(byte[] chunkData) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(chunkData);
        return new PositionTrackingDataInputStream(byteArrayInputStream);
    }

    /**
     * Parses the NBT file and returns the root compound tag.
     * This method automatically closes the reader when parsing is complete or if an error occurs.
     *
     * @return The root {@link ICompoundTag} containing the complete NBT structure
     * @throws IOException If the file cannot be read, is corrupted, or doesn't follow NBT specification
     */
    public ICompoundTag read() throws IOException
    {
        try
        {
            Tag_Compound tag = (Tag_Compound) readNBTTag(0);
            return tag;
        } catch (IOException e)
        {
            throw e; // Re-throw IOException directly
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
     * @param depth Current nesting depth for validation and error reporting
     * @return The complete tag read from the stream
     * @throws IOException If tag structure is invalid or stream is corrupted
     */
    private Tag readNBTTag(int depth) throws IOException
    {
        try
        {
            int type = stream.readByte();
            String name = "";

            if (type != NBTTags.Tag_End.getId())
            {
                int nameLength = stream.readUnsignedShort();
                if (nameLength < 0 || nameLength > 32767) // Reasonable limit for NBT names
                {
                    throw new IOException(stream.createContextualError(
                        String.format("Invalid tag name length: %d", nameLength)));
                }
                
                byte[] byteBuffer = new byte[nameLength];
                stream.readFully(byteBuffer);
                name = new String(byteBuffer, StandardCharsets.UTF_8);
            }
            
            stream.setCurrentContext(depth, name, type);
            return readNBTPayload(type, name, depth);
        }
        catch (IOException e)
        {
            if (e.getMessage().contains("[Position:")) 
            {
                throw e; // Already has context
            }
            throw new IOException(stream.createContextualError("Failed to read NBT tag: " + e.getMessage()), e);
        }
    }

    /**
     * Method to only read a NBT tag's payload.
     *
     * @param type The type of the NBT tag, which's payload should be read.
     * @param name The name of the NBT tag.
     * @param depth The current depth at which is beeing read.
     * @return The complete read NBT tag.
     * @throws IOException When encountering a parsing error caused by the file (e.g. corrupted).
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
                arrayLength = stream.readUnsignedShort();
                if (arrayLength > 65535) // Max string length in NBT
                {
                    throw new IOException(stream.createContextualError(
                        String.format("Invalid string length: %d", arrayLength)));
                }
                byteBuffer = new byte[arrayLength];
                stream.readFully(byteBuffer);
                return new Tag_String(name, new String(byteBuffer, StandardCharsets.UTF_8));
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
                ArrayList<Tag<?>> tagList = new ArrayList<>(Math.min(arrayLength, 1000)); // Pre-size reasonably
                for (int i = 0; i < arrayLength; i++)
                {
                    tagList.add(readNBTPayload(listType, "", depth + 1));
                }
                return new Tag_List(name, listType, tagList);
            case Tag_Compound:
                tagList = new ArrayList<>(16);
                while (true)
                {
                    Tag tag = readNBTTag(depth + 1);
                    if (tag.getId() == NBTTags.Tag_End.getId()) break;
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
