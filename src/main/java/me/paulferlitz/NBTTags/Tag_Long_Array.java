package me.paulferlitz.NBTTags;

import me.paulferlitz.Constants;

import java.util.Arrays;

/**
 * Class representing a NBT long array tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Long_Array extends Tag
{
    private final long[] value;

    /**
     * Create a long array tag with the value.
     *
     * @param value Long array value of the tag.
     */
    public Tag_Long_Array(long[] value)
    {
        super(Constants.NBTTags.Tag_Long_Array.getId());
        this.value = value;
    }

    /**
     * Create a long array tag with name and value.
     *
     * @param name  Name of the tag.
     * @param value Long array value of the tag.
     */
    public Tag_Long_Array(String name, long[] value)
    {
        super(Constants.NBTTags.Tag_Long_Array.getId(), name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public long[] getValue()
    {
        return value;
    }

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representatin of tag.
     */
    @Override
    public String toString()
    {
        return Constants.NBTTags.Tag_Long_Array.getName() + "(\"" + getName() + "\"): " + Arrays.toString(getValue());
    }
}
