package me.paulferlitz.io;

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
    private final DataInputStream stream;

    /**
     * Create a reader by passing it the target NBT file.
     *
     * @param nbtFile The target NBT file.
     */
    public NBTReader(File nbtFile)
    {
        try
        {
            this.stream = NBTFileHandler.loadNBTToReader(nbtFile);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a reader by passing it a {@link DataInputStream}.
     *
     * @param dis A {@link DataInputStream} containing a NBT file.
     */
    public NBTReader(DataInputStream dis)
    {
        this.stream = dis;
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
     * Converts a byte array into a DataInputStream (to be read by NBTReader).
     * @param chunkData The byte array to convert.
     * @return The DataInputStream.
     */
    public static DataInputStream byteArrayToDataInputStream(byte[] chunkData) {
        // Convert the byte array into a ByteArrayInputStream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(chunkData);

        // Wrap the ByteArrayInputStream with a DataInputStream
        return new DataInputStream(byteArrayInputStream);
    }

    /**
     * Method to parse the set NBT file.
     * Executing this method will close the reader.
     *
     * @return The root NBT tag, holding the entire file, to then interact with.
     * @throws IOException When encountering a parsing error caused by the file (e.g. corrupted).
     */
    public ICompoundTag read() throws IOException
    {
        /*
         * As per the NBT specs (https://minecraft.wiki/w/NBT_format), every NBT file must be a compound tag at the root.
         * If you feel the urge to ask me to implement the ability to parse non-standard NBT files I kindly ask you to do it yourself >:(.
         */
        Tag_Compound tag;
        try
        {
            tag = (Tag_Compound) readNBTTag(0);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } finally
        {
            this.close();
        }
        return tag;
    }

    /**
     * Method to read an entire NBT tag.
     *
     * @param depth Parameter, lateron increment by recursion, to keep track of current depth in NBT file.
     * @return The tag read at the current position in the {@link NBTReader#stream}.
     * @throws IOException When encountering a parsing error caused by the file (e.g. corrupted).
     */
    private Tag readNBTTag(int depth) throws IOException
    {
        int type = stream.readByte();
        String name = "";

        if (type != NBTTags.Tag_End.getId())
        {
            int nameLength = stream.readUnsignedShort();
            byte[] byteBuffer = new byte[nameLength];
            stream.readFully(byteBuffer);
            name = new String(byteBuffer, StandardCharsets.UTF_8);
        }
        return readNBTPayload(type, name, depth);
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
        switch (NBTTags.getById(type))
        {
            case Tag_End:
                if (depth == 0)
                    throw new IOException("Tag_End found before the first Tag_Compound was started. Invalid!");
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
                byte[] byteBuffer = new byte[arrayLength];
                stream.readFully(byteBuffer);
                return new Tag_Byte_Array(name, byteBuffer);
            case Tag_String:
                arrayLength = stream.readUnsignedShort();
                byteBuffer = new byte[arrayLength];
                stream.readFully(byteBuffer);
                return new Tag_String(name, new String(byteBuffer, StandardCharsets.UTF_8));
            case Tag_List:
                int listType = stream.readByte();
                arrayLength = stream.readInt();
                ArrayList<Tag<?>> tagList = new ArrayList<>(arrayLength);
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
