package me.paulferlitz.NBTTags;

import java.util.ArrayList;

/**
 * Class representing a NBT list tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_List extends Collection_Tag
{
    private final int listTypeID;

    /**
     * Create an empty list tag.
     */
    public Tag_List(int listTypeID)
    {
        super(NBTTags.Tag_List.getId());
        this.listTypeID = listTypeID;
    }

    /**
     * Create a list tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_List(String name, int listTypeID)
    {
        super(NBTTags.Tag_List.getId(), name);
        this.listTypeID = listTypeID;
    }

    /**
     * Create a list tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_List(String name, int listTypeID, ArrayList<Tag<?>> data)
    {
        super(NBTTags.Tag_List.getId(), name, data);
        this.listTypeID = listTypeID;
    }

    /**
     * Method to get the type ID of the list.
     *
     * @return The type ID of the list.
     */
    public int getListTypeID()
    {
        return listTypeID;
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
        sb.append(NBTTags.Tag_List.getName());
        sb.append("(name=").append(getName()).append(", type=").append(getListTypeID()).append(", size=").append(getData().size()).append("):");
        for (Tag<?> tag : getData())
        {
            sb.append("\n  ").append(tag.toString().replace("\n", "\n  "));
        }
        sb.append("\n").append(NBTTags.Tag_End.getName());
        sb.append("(name=").append(getName()).append(")");
        return sb.toString();
    }
}
