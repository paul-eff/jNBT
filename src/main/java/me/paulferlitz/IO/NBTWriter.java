package me.paulferlitz.IO;

import me.paulferlitz.NBTTags.NBTTags;
import me.paulferlitz.NBTTags.Tag;
import me.paulferlitz.NBTTags.Tag_List;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * Class for handling the writing of a Java NBT to file.
 *
 * @author Paul Ferlitz
 */
public class NBTWriter
{
    private DataOutputStream stream;

    /**
     * Create a writer by passing it the target NBT file.
     *
     * @param nbtFile The target NBT file.
     */
    public NBTWriter(File nbtFile)
    {
        try
        {
            File backupFile = new File(nbtFile.getPath() + ".bak");
            Files.copy(nbtFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            this.stream = NBTFileHandler.targetFileToStream(nbtFile);
            //this.stream = new DataOutputStream(new FileOutputStream(nbtFile));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a writer by passing it the target NBT file as DataOutputStream.
     *
     * @param dos A {@link DataOutputStream} containing a NBT file.
     */
    public NBTWriter(DataOutputStream dos)
    {
        this.stream = dos;
    }

    public void writeNBTTag(Tag<?> tag) throws IOException {
        String name = tag.getName();
        // TODO: Extract charsets to main location
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

        stream.writeByte(tag.getId());
        stream.writeShort(nameBytes.length);
        stream.write(nameBytes);

        writeNBTPayload(tag);
    }

    private void writeNBTPayload(Tag<?> tag) throws IOException {
        switch(NBTTags.getById(tag.getId())) {
            case NBTTags.Tag_End:
                // Do nothing! Handled by compound.
                break;
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
                bytes = ((String) tag.getData()).getBytes(StandardCharsets.UTF_8);
                stream.writeShort(bytes.length);
                stream.write(bytes);
                break;
            case NBTTags.Tag_List:
                ArrayList<Tag<?>> listTags = (ArrayList<Tag<?>>) tag.getData();
                int size = listTags.size();

                stream.writeByte(((Tag_List) tag).getListTypeID());
                stream.writeInt(size);
                for(int i = 0; i < size; i++) {
                    writeNBTPayload(listTags.get(i));
                }
                break;
            case NBTTags.Tag_Compound:
                for(Tag<?> compTag : (ArrayList<Tag<?>>) tag.getData()) {
                    writeNBTTag(compTag);
                }
                // Simulate Tag_End
                stream.writeByte((byte) 0);
                break;
            case NBTTags.Tag_Int_Array:
                int[] intArray = (int[]) tag.getData();
                size = intArray.length;
                stream.writeInt(size);
                for (int i = 0; i < size; i++) {
                    stream.writeInt(intArray[i]);
                }
                break;
            case NBTTags.Tag_Long_Array:
                long[] longArray = (long[]) tag.getData();
                size = longArray.length;
                stream.writeInt(size);
                for (int i = 0; i < size; i++) {
                    stream.writeLong(longArray[i]);
                }
                break;
            case null:
            default:
                throw new IOException("Invalid tag type: " + tag.getId() + ".");
        }
    }
}
