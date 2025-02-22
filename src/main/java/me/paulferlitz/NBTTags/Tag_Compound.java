package me.paulferlitz.NBTTags;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Class representing a NBT compound tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Compound extends Tag<ArrayList<Tag<?>>>
{
    /**
     * Create an empty compound tag.
     */
    public Tag_Compound()
    {
        super(NBTTags.Tag_Compound.getId());
    }

    /**
     * Create a compound tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Compound(String name)
    {
        super(NBTTags.Tag_Compound.getId(), name);
    }

    /**
     * Create a compound tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Compound(String name, ArrayList<Tag<?>> data)
    {
        super(NBTTags.Tag_Compound.getId(), name, data);
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
        sb.append(NBTTags.Tag_Compound.getName());
        sb.append("(name=" + getName() + ", size=" + getData().size() + "):");
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
