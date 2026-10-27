package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

import java.util.Arrays;

/**
 * Class representing a NBT int array tag.
 *
 */
public class Tag_Int_Array extends Tag<int[]>
{
    /**
     * Creates a tag initialized to a new empty array.
     */
    public Tag_Int_Array()
    {
        super(NBTTags.Tag_Int_Array.getId());
    }

    /**
     * Creates a named tag initialized to a new empty array.
     *
     * @param name Name of the tag.
     */
    public Tag_Int_Array(String name)
    {
        super(NBTTags.Tag_Int_Array.getId(), name);
    }

    /**
     * Create an int array tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The non-null initial data of the tag.
     * @throws IllegalArgumentException if data is null
     */
    public Tag_Int_Array(String name, int[] data)
    {
        super(NBTTags.Tag_Int_Array.getId(), name, data);
    }

    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + Arrays.toString(getData());
    }
}
