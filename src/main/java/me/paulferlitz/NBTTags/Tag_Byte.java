package me.paulferlitz.NBTTags;

import me.paulferlitz.Constants;

/**
 * Class representing a NBT byte tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Byte extends Tag
{
    private final byte value;

    /**
     * Create a byte tag with the value.
     *
     * @param value Byte value of the tag.
     */
    public Tag_Byte(byte value)
    {
        super(Constants.NBTTags.Tag_Byte.getId());
        this.value = value;
    }

    /**
     * Create a byte tag with name and value.
     *
     * @param name Name of the tag.
     * @param value Byte value of the tag.
     */
    public Tag_Byte(String name, byte value)
    {
        super(Constants.NBTTags.Tag_Byte.getId(), name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public Byte getValue()
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
        return Constants.NBTTags.Tag_Byte.getName() + "(\"" + getName() + "\"): " + getValue();
    }
}
