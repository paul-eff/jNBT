package me.paulferlitz.io;

import me.paulferlitz.api.ICompoundTag;
import me.paulferlitz.api.ITag;
import me.paulferlitz.api.IListTag;
import me.paulferlitz.api.INBTWriter;
import me.paulferlitz.util.NBTTags;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Class for handling the writing of Java NBT to file.
 *
 * @author Paul Ferlitz
 */
public class NBTWriter implements INBTWriter
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
     * Writes the compound tag structure to file and closes the writer.
     * The NBT specification requires compound tags as file roots, so this method enforces that constraint.
     *
     * @param root The root {@link ICompoundTag} containing the complete NBT structure to write
     * @throws IOException If an error occurs during file writing or if the data is invalid
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
            writeNBTTag(root);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } finally
        {
            this.close();
        }
    }

    private void writeNBTTag(ITag<?> tag) throws IOException {
        String name = tag.getName();
        // TODO: Extract charsets to main location
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

        stream.writeByte(tag.getId());
        stream.writeShort(nameBytes.length);
        stream.write(nameBytes);

        writeNBTPayload(tag);
    }

    private void writeNBTPayload(ITag<?> tag) throws IOException {
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
                ArrayList<ITag<?>> listTags = (ArrayList<ITag<?>>) tag.getData();
                int size = listTags.size();

                stream.writeByte(((IListTag) tag).getListTypeID());
                stream.writeInt(size);
                for(int i = 0; i < size; i++) {
                    writeNBTPayload(listTags.get(i));
                }
                break;
            case NBTTags.Tag_Compound:
                for(ITag<?> compTag : (ArrayList<ITag<?>>) tag.getData()) {
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
