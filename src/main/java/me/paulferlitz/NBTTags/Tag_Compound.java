package me.paulferlitz.NBTTags;

import java.util.ArrayList;

/**
 * Class representing a NBT compound tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Compound extends Tag
{
    private final ArrayList<Tag> value;

    /**
     * Create a compound tag with the value.
     *
     * @param value List of all contained tags.
     */
    public Tag_Compound(ArrayList<Tag> value)
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
    public Tag_Compound(String name, ArrayList<Tag> value)
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
    public ArrayList<Tag> getValue()
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
        sb.append("Tag_Compound(name=" + getName() + ", size=" + getValue().size() + "):");
        for (Tag tag : getValue())
        {
            sb.append("\n  " + tag.toString().replace("\n", "\n  "));
        }
        return sb.toString();
    }
}
