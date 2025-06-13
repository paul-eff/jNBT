package de.paulferlitz.core;

import de.paulferlitz.util.NBTTags;

import java.util.Arrays;

/**
 * Class representing a NBT long array tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Long_Array extends Basic_Tag<long[]>
{
    /**
     * Create an empty long array tag.
     */
    public Tag_Long_Array()
    {
        super(NBTTags.Tag_Long_Array.getId());
    }

    /**
     * Create a long array tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Long_Array(String name)
    {
        super(NBTTags.Tag_Long_Array.getId(), name);
    }

    /**
     * Create a long array tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Long_Array(String name, long[] data)
    {
        super(NBTTags.Tag_Long_Array.getId(), name, data);
    }

    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + Arrays.toString(getData());
    }
}
