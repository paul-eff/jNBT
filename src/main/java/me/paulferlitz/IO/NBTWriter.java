package me.paulferlitz.IO;

import me.paulferlitz.NBTTags.NBTTags;
import me.paulferlitz.NBTTags.Tag;
import me.paulferlitz.NBTTags.Tag_Compound;
import me.paulferlitz.NBTTags.Tag_List;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Class for handling the writing of Java NBT to file.
 *
 * @author Paul Ferlitz
 */
public class NBTWriter
{
    private final DataOutputStream stream;

    /**
     * Create a writer by passing it the target NBT file.
     * Target file must already exist to determine the compression type.
     * A backup of the target file will be made before overwriting it.
     *
     * @param nbtFile The target NBT file.
     */
    public NBTWriter(File nbtFile) throws FileNotFoundException
    {
        if (Files.notExists(nbtFile.toPath()))
        {
            throw new FileNotFoundException(String.format("The file %s doesn't exist. It's needed to determine the compression type!", nbtFile.getPath()));
        }
        try
        {
            this.stream = NBTFileHandler.loadNBTToWriter(nbtFile, NBTFileHandler.getCompressionType(nbtFile));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a writer by passing it the target NBT file and how to compress it.
     * A backup of the target file will be made before overwriting it, if it exists.
     *
     * @param nbtFile The target NBT file.
     * @param compression The compression type of the file.
     */
    public NBTWriter(File nbtFile, Compression_Types compression)
    {
        try
        {
            this.stream = NBTFileHandler.loadNBTToWriter(nbtFile, compression);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a writer by passing it the target NBT file as a {@link DataOutputStream}.
     *
     * @param dos A {@link DataOutputStream} containing a NBT file.
     */
    public NBTWriter(DataOutputStream dos)
    {
        this.stream = dos;
    }

    /**
     * Method to close the writer.
     *
     * @throws IOException When encountering an error whilst closing the writer.
     */
    public void close() throws IOException {
        stream.close();
    }

    /**
     * Method to write a NBT compound tag to file.
     * Executing this method will close the writer.
     *
     * @param root The root NBT tag, holding the entire file, to be written to the file.
     * @throws IOException When encountering a parsing error caused by the file (e.g. corrupted).
     */
    public void write(Tag<?> root) throws IOException
    {
        /*
         * As per the NBT specs (https://minecraft.wiki/w/NBT_format), every NBT file must be a compound tag at the root.
         * If you feel the urge to ask me to implement the ability to write non-standard NBT files I kindly ask you to do it yourself >:(.
         */
        if (!(root instanceof Tag_Compound)) throw new IOException("Root tag must always be a compound tag if written to file!");
        try
        {
            writeNBTTag(root);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } finally
        {
            this.close();
        }
    }

    private void writeNBTTag(Tag<?> tag) throws IOException {
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
