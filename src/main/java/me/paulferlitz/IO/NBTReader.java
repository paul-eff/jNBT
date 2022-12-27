package me.paulferlitz.IO;

import me.paulferlitz.Constants;
import me.paulferlitz.NBTTags.Tag;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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

        if (type != Constants.NBTTags.Tag_End.id)
        {
            int nameLength = stream.readShort();
            byte[] byteBuffer = new byte[stream.readShort()];
            stream.readFully(byteBuffer);
            name = new String(byteBuffer, StandardCharsets.UTF_8);
        }

        return readNBTPayload(type, name, depth);
    }

    private Tag readNBTPayload(int type, String name, int depth)
    {
        switch(Constants.NBTTags.getById(type))
        {
            case Tag_End:
                break;
            case Tag_Byte:
                break;
            case Tag_Short:
                break;
            case Tag_Int:
                break;
            case Tag_Long:
                break;
            case Tag_Float:
                break;
            case Tag_Double:
                break;
            case Tag_Byte_Array:
                break;
            case Tag_List:
                break;
            case Tag_Compound:
                break;
            case Tag_Int_Array:
                break;
            case Tag_Long_Array:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        return null;
    }
}
