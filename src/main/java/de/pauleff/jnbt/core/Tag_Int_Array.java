package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

import java.util.Arrays;

/**
 *  An NBT int array tag.
 *
 * @since 0.1
 */
public class Tag_Int_Array extends Tag<int[]>
{
    /** Creates a tag initialized to a new empty array. */
    public Tag_Int_Array()
    {
        super(NBTTags.Tag_Int_Array.getId());
    }

    /**
     * Creates a named tag initialized to a new empty array.
     *
     * @param name the tag name
     */
    public Tag_Int_Array(String name)
    {
        super(NBTTags.Tag_Int_Array.getId(), name);
    }

    /**
     * Create an int array tag with name and data.
     *
     * @param name the tag name
     * @param data the non-null initial value
     * @throws IllegalArgumentException if data is null
     */
    public Tag_Int_Array(String name, int[] data)
    {
        super(NBTTags.Tag_Int_Array.getId(), name, data);
    }

    /** Returns a string with the stored values. */
    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + Arrays.toString(getData());
    }
}
