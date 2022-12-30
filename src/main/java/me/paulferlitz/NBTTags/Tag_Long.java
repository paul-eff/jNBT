package me.paulferlitz.NBTTags;

import me.paulferlitz.Constants;

/**
 * Class representing a NBT long tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Long extends Tag
{
    private final long value;

    /**
     * Create a long tag with the value.
     *
     * @param value Long value of the tag.
     */
    public Tag_Long(long value)
    {
        super(Constants.NBTTags.Tag_Long.getId());
        this.value = value;
    }

    /**
     * Create a long tag with name and value.
     *
     * @param name  Name of the tag.
     * @param value Long value of the tag.
     */
    public Tag_Long(String name, long value)
    {
        super(Constants.NBTTags.Tag_Long.getId(), name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public Long getValue()
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
        return Constants.NBTTags.Tag_Long.getName() + "(\"" + getName() + "\"): " + getValue();
    }
}
