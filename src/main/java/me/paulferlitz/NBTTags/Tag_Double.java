package me.paulferlitz.NBTTags;

import me.paulferlitz.Constants;

/**
 * Class representing a NBT double tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Double extends Tag
{
    private final double value;

    /**
     * Create a double tag with the value.
     *
     * @param value Double value of the tag.
     */
    public Tag_Double(double value)
    {
        super(Constants.NBTTags.Tag_Double.getId());
        this.value = value;
    }

    /**
     * Create a double tag with name and value.
     *
     * @param name Name of the tag.
     * @param value Double value of the tag.
     */
    public Tag_Double(String name, double value)
    {
        super(Constants.NBTTags.Tag_Double.getId(), name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public Double getValue()
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
        return Constants.NBTTags.Tag_Double.getName() + "(\"" + getName() + "\"): " + getValue();
    }
}
