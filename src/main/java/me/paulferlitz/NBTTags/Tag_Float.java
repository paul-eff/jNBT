package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT float tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Float extends Tag
{
    private final float value;

    /**
     * Create a float tag with the value.
     *
     * @param value Float value of the tag.
     */
    public Tag_Float(float value)
    {
        super(5);
        this.value = value;
    }

    /**
     * Create a float tag with name and value.
     *
     * @param name Name of the tag.
     * @param value Float value of the tag.
     */
    public Tag_Float(String name, float value)
    {
        super(5, name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public Float getValue()
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
        return "Tag_Float(\"" + getName() + "\"): " + getValue();
    }
}
