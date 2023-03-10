package me.paulferlitz.NBTTags;

import me.paulferlitz.Constants;

/**
 * Class representing a NBT string tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_String extends Tag
{
    private final String value;

    /**
     * Create a string tag with the value.
     *
     * @param value String value of the tag.
     */
    public Tag_String(String value)
    {
        super(Constants.NBTTags.Tag_String.getId());
        this.value = value;
    }

    /**
     * Create a string tag with name and value.
     *
     * @param name  Name of the tag.
     * @param value String value of the tag.
     */
    public Tag_String(String name, String value)
    {
        super(Constants.NBTTags.Tag_String.getId(), name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public String getValue()
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
        return Constants.NBTTags.Tag_String.getName() + "(\"" + getName() + "\"): " + getValue();
    }
}
