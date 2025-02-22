package me.paulferlitz.NBTTags;

import java.util.ArrayList;

/**
 * Class representing a NBT list tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_List extends Tag<ArrayList<Tag<?>>>
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
        sb.append("(name=" + getName() + ", type=" + getListTypeID() + ", size=" + getData().size() + "):");
        for (Tag<?> tag : getData())
        {
            sb.append("\n  " + tag.toString().replace("\n", "\n  "));
        }
        sb.append("\n" + NBTTags.Tag_End.getName());
        sb.append("(name=" + getName() + ")");
        return sb.toString();
    }

    /**
     * Method to find a tag in the collection by name.
     *
     * @param name The name of the tag to find.
     * @param global Search and delete from anywhere in the tag or stay on current level.
     */
    public Tag findTagByName(String name, boolean global)
    {
        //TODO: Proper error handling if null Tag found
        if(name.equals("null") || name.isEmpty())
        {
            return null;
        }
        for (Tag tag : data)
        {
            if (tag.getName().equals(name))
            {
                return tag;
            }
            if(global)
            {
                if (tag instanceof Tag_Compound)
                {
                    return ((Tag_Compound) tag).findTagByName(name, true);
                }
                if (tag instanceof Tag_List)
                {
                    return ((Tag_List) tag).findTagByName(name, true);
                }
            }
        }
        return null;
    }
}
