package me.paulferlitz.NBTTags;

import java.util.ArrayList;

/**
 * Class representing a NBT list tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_List extends Tag
{
    private final ArrayList<Tag> value;

    private final int typeID;

    /**
     * Create a list tag with the value.
     *
     * @param typeID Type ID of the type found in the list.
     * @param value  List of all contained tags.
     */
    public Tag_List(int typeID, ArrayList<Tag> value)
    {
        super(9);
        this.typeID = typeID;
        this.value = value;
    }

    /**
     * Create a list tag with name and value.
     *
     * @param name   Name of the tag.
     * @param typeID Type ID of the type found in the list.
     * @param value  List of all contained tags.
     */
    public Tag_List(String name, int typeID, ArrayList<Tag> value)
    {
        super(9, name);
        this.typeID = typeID;
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
     * Method returning the tag's type ID.
     *
     * @return The type ID of the tag.
     */
    public int getType()
    {
        return typeID;
    }

    // TODO: Implement a tag type ID to tag type String method.

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representatin of tag.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Tag_List(\"" + getName() + ", type=" + getType() + ", size=" + getValue().size() + "):\n");
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
