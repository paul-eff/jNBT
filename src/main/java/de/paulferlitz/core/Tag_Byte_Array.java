package de.paulferlitz.core;

import de.paulferlitz.util.NBTTags;

import java.util.Arrays;

/**
 * Class representing a NBT byte array tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Byte_Array extends Basic_Tag<byte[]>
{
    /**
     * Create an empty byte array tag.
     */
    public Tag_Byte_Array()
    {
        super(NBTTags.Tag_Byte_Array.getId());
    }

    /**
     * Create a byte array tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Byte_Array(String name)
    {
        super(NBTTags.Tag_Byte_Array.getId(), name);
    }

    /**
     * Create a byte array tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Byte_Array(String name, byte[] data)
    {
        super(NBTTags.Tag_Byte_Array.getId(), name, data);
    }

    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + Arrays.toString(getData());
    }
}
