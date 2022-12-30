package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT int tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Int extends Tag
{
    private final int value;

    /**
     * Create a int tag with the value.
     *
     * @param value Int value of the tag.
     */
    public Tag_Int(int value)
    {
        super(3);
        this.value = value;
    }

    /**
     * Create a int tag with name and value.
     *
     * @param name  Name of the tag.
     * @param value Int value of the tag.
     */
    public Tag_Int(String name, int value)
    {
        super(3, name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public Integer getValue()
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
        return "Tag_Int(\"" + getName() + "\"): " + getValue();
    }
}
