package me.paulferlitz.NBTTags;

import java.util.List;

/**
 * Class representing a NBT compound tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Compound extends Tag
{
    private final List<Tag> value;

    /**
     * Create a compound tag with the value.
     *
     * @param value List of all contained tags.
     */
    public Tag_Compound(List<Tag> value)
    {
        super(10);
        this.value = value;
    }

    /**
     * Create a compound tag with name and value.
     *
     * @param name  Name of the tag.
     * @param value List of all contained tags.
     */
    public Tag_Compound(String name, List<Tag> value)
    {
        super(10, name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public List<Tag> getValue()
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
        StringBuilder sb = new StringBuilder();
        sb.append("Tag_Compound(\"" + getName() + "\", size=" + getValue().size() + "):\n");
        for (Tag tag : getValue())
        {
            if (getValue().indexOf(tag) < (getValue().size() - 1))
            {
                sb.append("  " + tag.toString() + "\n");
            }
        }
        return sb.toString();
    }
}
