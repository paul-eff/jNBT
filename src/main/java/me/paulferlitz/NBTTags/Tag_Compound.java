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
     * Convenience method for adding a String tag with fluent API.
     *
     * @param name The name of the tag.
     * @param value The string value.
     * @return This compound tag for method chaining.
     */
    public Tag_Compound addString(String name, String value)
    {
        this.addTag(new Tag_String(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding an Int tag with fluent API.
     *
     * @param name The name of the tag.
     * @param value The integer value.
     * @return This compound tag for method chaining.
     */
    public Tag_Compound addInt(String name, int value)
    {
        this.addTag(new Tag_Int(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding a Double tag with fluent API.
     *
     * @param name The name of the tag.
     * @param value The double value.
     * @return This compound tag for method chaining.
     */
    public Tag_Compound addDouble(String name, double value)
    {
        this.addTag(new Tag_Double(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding a Float tag with fluent API.
     *
     * @param name The name of the tag.
     * @param value The float value.
     * @return This compound tag for method chaining.
     */
    public Tag_Compound addFloat(String name, float value)
    {
        this.addTag(new Tag_Float(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding a Byte tag with fluent API.
     *
     * @param name The name of the tag.
     * @param value The byte value.
     * @return This compound tag for method chaining.
     */
    public Tag_Compound addByte(String name, byte value)
    {
        this.addTag(new Tag_Byte(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding a Short tag with fluent API.
     *
     * @param name The name of the tag.
     * @param value The short value.
     * @return This compound tag for method chaining.
     */
    public Tag_Compound addShort(String name, short value)
    {
        this.addTag(new Tag_Short(name, value));
        return this;
    }
    
    /**
     * Convenience method for adding a Long tag with fluent API.
     *
     * @param name The name of the tag.
     * @param value The long value.
     * @return This compound tag for method chaining.
     */
    public Tag_Compound addLong(String name, long value)
    {
        this.addTag(new Tag_Long(name, value));
        return this;
    }

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representation of tag.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NBTTags.Tag_Compound.getName());
        sb.append("(name=").append(getName()).append(", size=").append(getData().size()).append("):");
        for (Tag<?> tag : getData())
        {
            sb.append("\n  ").append(tag.toString().replace("\n", "\n  "));
        }
        sb.append("\n").append(NBTTags.Tag_End.getName());
        sb.append("(name=").append(getName()).append(")");
        return sb.toString();
    }
}
