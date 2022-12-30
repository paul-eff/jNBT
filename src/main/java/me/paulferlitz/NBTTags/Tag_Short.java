package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT short tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Short extends Tag
{
    private final short value;

    /**
     * Create a short tag with the value.
     *
     * @param value Short value of the tag.
     */
    public Tag_Short(short value)
    {
        super(2);
        this.value = value;
    }

    /**
     * Create a short tag with name and value.
     *
     * @param name  Name of the tag.
     * @param value Short value of the tag.
     */
    public Tag_Short(String name, short value)
    {
        super(2, name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public Short getValue()
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
        return "Tag_Short(\"" + getName() + "\"): " + getValue();
    }
}
