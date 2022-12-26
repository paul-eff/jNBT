package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT end tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_End extends Tag
{
    /**
     * Create an end tag with the value.
     */
    public Tag_End()
    {
        super(0);
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public Object getValue()
    {
        return null;
    }

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representatin of tag.
     */
    @Override
    public String toString()
    {
        return "Tag_End()";
    }
}
