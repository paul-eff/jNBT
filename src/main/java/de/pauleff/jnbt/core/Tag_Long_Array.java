package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

import java.util.Arrays;

/**
 *  An NBT long array tag.
 *
 * @since 0.1
 */
public class Tag_Long_Array extends Tag<long[]>
{
    /** Creates a tag initialized to a new empty array. */
    public Tag_Long_Array()
    {
        super(NBTTags.Tag_Long_Array.getId());
    }

    /**
     * Creates a named tag initialized to a new empty array.
     *
     * @param name the tag name
     */
    public Tag_Long_Array(String name)
    {
        super(NBTTags.Tag_Long_Array.getId(), name);
    }

    /**
     * Creates a long array tag with name and data.
     *
     * @param name the tag name
     * @param data the non-null initial value
     * @throws IllegalArgumentException if data is null
     */
    public Tag_Long_Array(String name, long[] data)
    {
        super(NBTTags.Tag_Long_Array.getId(), name, data);
    }

    /** Returns a string with the stored values. */
    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + Arrays.toString(getData());
    }
}
