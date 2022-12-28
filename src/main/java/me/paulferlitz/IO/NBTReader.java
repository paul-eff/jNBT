package me.paulferlitz.IO;

import me.paulferlitz.Constants;
import me.paulferlitz.NBTTags.*;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class NBTReader
{
    private DataInputStream stream;

    public NBTReader(File nbtFile)
    {
        try {
            this.stream = NBTFileHandler.laodFileToStream(nbtFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Tag readNBTTag(int depth) throws IOException
    {
        int type = stream.readByte();
        String name = "";

        if (type != Constants.NBTTags.Tag_End.getId())
        {
            int nameLength = stream.readShort();
            byte[] byteBuffer = new byte[nameLength];
            stream.readFully(byteBuffer);
            name = new String(byteBuffer, StandardCharsets.UTF_8);
        }

        return readNBTPayload(type, name, depth);
    }

    private Tag readNBTPayload(int type, String name, int depth) throws IOException
    {
        switch(Constants.NBTTags.getById(type))
        {
            case Tag_End:
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
                return new Tag_Byte_Array(name, stream.readNBytes(arrayLength));
            case Tag_String:
                arrayLength = stream.readUnsignedShort();
                byteBuffer = new byte[arrayLength];
                stream.readFully(byteBuffer);
                return new Tag_String(name, new String(byteBuffer, StandardCharsets.UTF_8));
            case Tag_List:
                ArrayList<Tag> tagList = new ArrayList();
                int listType = stream.readByte();
                arrayLength = stream.readInt();
                for(int i = 0; i < arrayLength; i++)
                {
                    tagList.add(readNBTPayload(listType, "", depth + 1));
                }
                return new Tag_List(name, listType, tagList);
            case Tag_Compound:
                tagList = new ArrayList();
                while(true)
                {
                    Tag tag = readNBTTag(depth + 1);
                    if (tag.getId() == Constants.NBTTags.Tag_End.getId()) break;
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
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
