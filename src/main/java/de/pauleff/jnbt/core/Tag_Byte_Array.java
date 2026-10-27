package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

import java.util.Arrays;

/**
 *  An NBT byte array tag.
 *
 * @since 0.1
 */
public class Tag_Byte_Array extends Tag<byte[]>
{
    /** Creates a tag initialized to a new empty array. */
    public Tag_Byte_Array()
    {
        super(NBTTags.Tag_Byte_Array.getId());
    }

    /**
     * Creates a named tag initialized to a new empty array.
     *
     * @param name the tag name
     */
    public Tag_Byte_Array(String name)
    {
        super(NBTTags.Tag_Byte_Array.getId(), name);
    }

    /**
     * Creates a byte array tag with name and data.
     *
     * @param name the tag name
     * @param data the non-null initial value
     * @throws IllegalArgumentException if data is null
     */
    public Tag_Byte_Array(String name, byte[] data)
    {
        super(NBTTags.Tag_Byte_Array.getId(), name, data);
    }

    /** Returns a string with the stored values. */
    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + Arrays.toString(getData());
    }
}
