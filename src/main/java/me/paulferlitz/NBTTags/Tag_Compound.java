package me.paulferlitz.NBTTags;

import java.util.ArrayList;

/**
 * Class representing a NBT compound tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Compound extends Collection_Tag
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
}
