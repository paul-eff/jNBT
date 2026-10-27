package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.util.NBTTags;

import java.util.ArrayList;

/**
 * Represents an NBT compound tag - a named collection of heterogeneous tags
 * that forms the backbone of NBT data structures.
 * <p>Compound tags store multiple child tags of different types, accessed by name.
 * Lookup, replacement, and removal by name operate on direct children only.
 * Explicit recursive operations such as {@link #getTagByName(String)} remain
 * available separately.</p>
 * <p>Extends {@link Collection_Tag} with compound-specific operations and
 * implements {@link ICompoundTag} for standardized access.</p>
 *
 */
public class Tag_Compound extends Collection_Tag implements ICompoundTag
{
    /**
     * Creates an empty compound tag with default name.
     */
    public Tag_Compound()
    {
        super(NBTTags.Tag_Compound.getId());
    }

    /**
     * Creates an empty compound tag with the specified name.
     *
     * @param name the tag name
     */
    public Tag_Compound(String name)
    {
        super(NBTTags.Tag_Compound.getId(), name);
    }

    /**
     * Creates a compound tag pre-populated with child tags.
     *
     * @param name the tag name
     * @param data the initial collection of child tags
     */
    public Tag_Compound(String name, ArrayList<Tag<?>> data)
    {
        super(NBTTags.Tag_Compound.getId(), name, data);
    }

    /**
     * Finds the first matching direct child without descending into containers.
     *
     * @param name the child name
     * @return the matching child, or null if absent
     */
    private Tag<?> findDirectChild(String name)
    {
        for (Tag<?> child : data)
        {
            if (child.getName().equals(name))
            {
                return child;
            }
        }
        return null;
    }

    /**
     * Removes the first matching direct child, leaving descendants untouched.
     *
     * @param name the child name
     */
    private void removeDirectChild(String name)
    {
        for (int i = 0; i < data.size(); i++)
        {
            if (data.get(i).getName().equals(name))
            {
                data.remove(i);
                return;
            }
        }
    }

    /**
     * Adds a string tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the string value
     * @return this compound tag for fluent operations
     * @see Tag_String
     */
    public ICompoundTag addString(String name, String value)
    {
        this.addTag(new Tag_String(name, value));
        return this;
    }

    /**
     * Adds an integer tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the integer value
     * @return this compound tag for fluent operations
     * @see Tag_Int
     */
    public ICompoundTag addInt(String name, int value)
    {
        this.addTag(new Tag_Int(name, value));
        return this;
    }

    /**
     * Adds a double-precision tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the double value
     * @return this compound tag for fluent operations
     * @see Tag_Double
     */
    public ICompoundTag addDouble(String name, double value)
    {
        this.addTag(new Tag_Double(name, value));
        return this;
    }

    /**
     * Adds a single-precision float tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the float value
     * @return this compound tag for fluent operations
     * @see Tag_Float
     */
    public ICompoundTag addFloat(String name, float value)
    {
        this.addTag(new Tag_Float(name, value));
        return this;
    }

    /**
     * Adds a byte tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the byte value
     * @return this compound tag for fluent operations
     * @see Tag_Byte
     */
    public ICompoundTag addByte(String name, byte value)
    {
        this.addTag(new Tag_Byte(name, value));
        return this;
    }

    /**
     * Adds a short integer tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the short value
     * @return this compound tag for fluent operations
     * @see Tag_Short
     */
    public ICompoundTag addShort(String name, short value)
    {
        this.addTag(new Tag_Short(name, value));
        return this;
    }

    /**
     * Adds a long integer tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the long value
     * @return this compound tag for fluent operations
     * @see Tag_Long
     */
    public ICompoundTag addLong(String name, long value)
    {
        this.addTag(new Tag_Long(name, value));
        return this;
    }

    /**
     * Adds a byte array tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the byte array value
     * @return this compound tag for fluent operations
     * @see Tag_Byte_Array
     */
    public ICompoundTag addByteArray(String name, byte[] value)
    {
        this.addTag(new Tag_Byte_Array(name, value));
        return this;
    }

    /**
     * Adds an integer array tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the integer array value
     * @return this compound tag for fluent operations
     * @see Tag_Int_Array
     */
    public ICompoundTag addIntArray(String name, int[] value)
    {
        this.addTag(new Tag_Int_Array(name, value));
        return this;
    }

    /**
     * Adds a long array tag and returns this compound for method chaining.
     *
     * @param name  the tag name
     * @param value the long array value
     * @return this compound tag for fluent operations
     * @see Tag_Long_Array
     */
    public ICompoundTag addLongArray(String name, long[] value)
    {
        this.addTag(new Tag_Long_Array(name, value));
        return this;
    }

    // Interface methods from ICompoundTag

    @Override
    public boolean hasTag(String tagName)
    {
        return findDirectChild(tagName) != null;
    }

    @Override
    public ITag<?> getTag(String name)
    {
        return findDirectChild(name);
    }

    @Override
    public String getString(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_String)
        {
            return (String) tag.getData();
        }
        return null;
    }

    @Override
    public int getInt(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Int)
        {
            return (Integer) tag.getData();
        }
        return 0;
    }

    @Override
    public double getDouble(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Double)
        {
            return (Double) tag.getData();
        }
        return 0.0;
    }

    @Override
    public byte getByte(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Byte)
        {
            return (Byte) tag.getData();
        }
        return 0;
    }

    @Override
    public float getFloat(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Float)
        {
            return (Float) tag.getData();
        }
        return 0.0f;
    }

    @Override
    public short getShort(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Short)
        {
            return (Short) tag.getData();
        }
        return 0;
    }

    @Override
    public long getLong(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Long)
        {
            return (Long) tag.getData();
        }
        return 0L;
    }

    @Override
    public ICompoundTag getCompound(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Compound)
        {
            return (ICompoundTag) tag;
        }
        return null;
    }

    @Override
    public IListTag getList(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_List)
        {
            return (IListTag) tag;
        }
        return null;
    }

    @Override
    public byte[] getByteArray(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Byte_Array)
        {
            return (byte[]) tag.getData();
        }
        return null;
    }

    @Override
    public int[] getIntArray(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Int_Array)
        {
            return (int[]) tag.getData();
        }
        return null;
    }

    @Override
    public long[] getLongArray(String name)
    {
        Tag<?> tag = findDirectChild(name);
        if (tag instanceof Tag_Long_Array)
        {
            return (long[]) tag.getData();
        }
        return null;
    }

    @Override
    public ICompoundTag setString(String name, String value)
    {
        removeDirectChild(name);
        addString(name, value);
        return this;
    }

    @Override
    public ICompoundTag setInt(String name, int value)
    {
        removeDirectChild(name);
        addInt(name, value);
        return this;
    }

    @Override
    public ICompoundTag setDouble(String name, double value)
    {
        removeDirectChild(name);
        addDouble(name, value);
        return this;
    }

    @Override
    public ICompoundTag setFloat(String name, float value)
    {
        removeDirectChild(name);
        addFloat(name, value);
        return this;
    }

    @Override
    public ICompoundTag setByte(String name, byte value)
    {
        removeDirectChild(name);
        addByte(name, value);
        return this;
    }

    @Override
    public ICompoundTag setShort(String name, short value)
    {
        removeDirectChild(name);
        addShort(name, value);
        return this;
    }

    @Override
    public ICompoundTag setLong(String name, long value)
    {
        removeDirectChild(name);
        addLong(name, value);
        return this;
    }

    @Override
    public ICompoundTag setByteArray(String name, byte[] value)
    {
        removeDirectChild(name);
        addByteArray(name, value);
        return this;
    }

    @Override
    public ICompoundTag setIntArray(String name, int[] value)
    {
        removeDirectChild(name);
        addIntArray(name, value);
        return this;
    }

    @Override
    public ICompoundTag setLongArray(String name, long[] value)
    {
        removeDirectChild(name);
        addLongArray(name, value);
        return this;
    }

    @Override
    public ICompoundTag setTag(ITag<?> tag)
    {
        validateChildTag(tag);
        removeDirectChild(tag.getName());
        addTag(tag);
        return this;
    }

    @Override
    public ICompoundTag removeTag(String name)
    {
        removeDirectChild(name);
        return this;
    }

    @Override
    public ICompoundTag removeTags(String... names)
    {
        for (String name : names)
        {
            removeDirectChild(name);
        }
        return this;
    }

    @Override
    public ICompoundTag clear()
    {
        getData().clear();
        return this;
    }

    @Override
    public int size()
    {
        return getData().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getData().isEmpty();
    }

    /**
     * Generates a hierarchical string representation showing all nested tags.
     *
     * <p>Output includes tag type, name, size, and recursively formatted child tags.</p>
     *
     * @return formatted multi-line string representation
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
