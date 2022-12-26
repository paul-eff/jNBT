package me.paulferlitz.NBTTags;

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
        super(4);
        this.value = value;
    }

    /**
     * Create a long tag with name and value.
     *
     * @param name Name of the tag.
     * @param value Long value of the tag.
     */
    public Tag_Long(String name, long value)
    {
        super(4, name);
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
        return "Tag_Long(\"" + getName() + "\"): " + getValue();
    }
}
