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
     * Convenience method for adding a String tag to this list with fluent API.
     * Only works if this list is of type Tag_String.
     *
     * @param name The name of the tag.
     * @param value The string value.
     * @return This list tag for method chaining.
     * @throws IllegalArgumentException if list type doesn't match Tag_String.
     */
    public Tag_List addString(String name, String value)
    {
        this.addTag(new Tag_String(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding an Int tag to this list with fluent API.
     * Only works if this list is of type Tag_Int.
     *
     * @param name The name of the tag.
     * @param value The integer value.
     * @return This list tag for method chaining.
     * @throws IllegalArgumentException if list type doesn't match Tag_Int.
     */
    public Tag_List addInt(String name, int value)
    {
        this.addTag(new Tag_Int(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding a Double tag to this list with fluent API.
     * Only works if this list is of type Tag_Double.
     *
     * @param name The name of the tag.
     * @param value The double value.
     * @return This list tag for method chaining.
     * @throws IllegalArgumentException if list type doesn't match Tag_Double.
     */
    public Tag_List addDouble(String name, double value)
    {
        this.addTag(new Tag_Double(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding a Compound tag to this list with fluent API.
     * Only works if this list is of type Tag_Compound.
     *
     * @param name The name of the tag.
     * @return This list tag for method chaining.
     * @throws IllegalArgumentException if list type doesn't match Tag_Compound.
     */
    public Tag_List addCompound(String name)
    {
        this.addTag(new Tag_Compound(name));
        return this;
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
