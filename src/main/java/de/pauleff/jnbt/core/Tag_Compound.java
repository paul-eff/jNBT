package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.util.NBTTags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A compound with unique child names. Lookup, replacement, and removal by name use direct children.
 * Inputs and getData() use shallow copies with shared child objects. Recursive operations are
 * available through {@link Collection_Tag}.
 *
 * @since 0.1
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
     * Creates an empty compound tag.
     *
     * @param name the tag name
     */
    public Tag_Compound(String name)
    {
        super(NBTTags.Tag_Compound.getId(), name);
    }

    /**
     * Creates a compound tag filled with child tags.
     *
     * @param name the tag name
     * @param data the initial collection of child tags (membership is copied)
     * @throws IllegalArgumentException if data is null or contains null, END, or duplicate keys
     */
    public Tag_Compound(String name, ArrayList<Tag<?>> data)
    {
        super(NBTTags.Tag_Compound.getId(), name, validatedCopy(data));
        registerChildren();
    }

    /**
     * Copies the children after rejecting nulls, END markers, and duplicate names.
     */
    private static ArrayList<Tag<?>> validatedCopy(List<Tag<?>> values)
    {
        if (values == null) throw new IllegalArgumentException("Compound data cannot be null");
        ArrayList<Tag<?>> copy = new ArrayList<>(values);
        Set<String> names = new HashSet<>();
        for (Tag<?> child : copy)
        {
            validateChildTag(child);
            if (!names.add(child.getName()))
            {
                throw new IllegalArgumentException("Duplicate compound key: " + child.getName());
            }
        }
        return copy;
    }

    /**
     * Registers this compound as a parent of each child.
     */
    private void registerChildren()
    {
        for (Tag<?> child : data) child.registerCompoundParent(this);
    }

    /**
     * Rejects a new name that would duplicate another direct child.
     */
    final void validateChildRename(Tag<?> child, String name)
    {
        boolean present = false;
        for (Tag<?> member : data)
        {
            if (member == child)
            {
                present = true;
                break;
            }
        }
        if (!present) return;
        for (Tag<?> member : data)
        {
            if (member != child && member.getName().equals(name))
            {
                throw new IllegalArgumentException("Duplicate compound key: " + name);
            }
        }
    }

    /**
     * Returns a shallow copy of the children. Child objects are shared.
     * Renames are checked for duplicate names in every containing compound.
     *
     * @return a copy of the child collection
     */
    @Override
    public ArrayList<Tag<?>> getData()
    {
        return new ArrayList<>(data);
    }

    /**
     * Replaces membership with a validated copy, preserving the old children on failure.
     *
     * @param data replacement children
     * @throws IllegalArgumentException if data is null or contains null, END, or duplicate keys
     */
    @Override
    public void setData(ArrayList<Tag<?>> data)
    {
        this.data = validatedCopy(data);
        registerChildren();
    }

    /**
     * Copies the name and membership from another compound after validating both.
     *
     * @param newTag the source compound
     * @throws IllegalArgumentException if the source or proposed name is invalid
     */
    @Override
    public void editTag(ITag<?> newTag)
    {
        if (!(newTag instanceof ICompoundTag compound))
        {
            throw new IllegalArgumentException("Source must be a compound");
        }
        ArrayList<Tag<?>> copy = validatedCopy(compound.getData());
        setName(compound.getName());
        this.data = copy;
        registerChildren();
    }

    /**
     * Adds a child, rejecting a name already present in this compound.
     *
     * @param tag the child to add
     * @return this compound
     * @throws IllegalArgumentException if the child is null, END, or has a duplicate name
     */
    @Override
    public Tag_Compound addTag(ITag<?> tag)
    {
        validateChildTag(tag);
        if (findDirectChild(tag.getName()) != null)
        {
            throw new IllegalArgumentException("Duplicate compound key: " + tag.getName());
        }
        super.addTag(tag);
        ((Tag<?>) tag).registerCompoundParent(this);
        return this;
    }

    /**
     * Adds children after checking all names. Invalid input leaves this compound unchanged.
     *
     * @param tags the children to add
     * @return this compound
     * @throws IllegalArgumentException if the combined membership is invalid
     */
    @Override
    public Tag_Compound addAllTags(List<Tag<?>> tags)
    {
        if (tags == null) throw new IllegalArgumentException("Compound children cannot be null");
        ArrayList<Tag<?>> combined = new ArrayList<>(data);
        combined.addAll(tags);
        setData(combined);
        return this;
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
     * {@inheritDoc}
     */
    public ICompoundTag addString(String name, String value)
    {
        this.addTag(new Tag_String(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addInt(String name, int value)
    {
        this.addTag(new Tag_Int(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addDouble(String name, double value)
    {
        this.addTag(new Tag_Double(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addFloat(String name, float value)
    {
        this.addTag(new Tag_Float(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addByte(String name, byte value)
    {
        this.addTag(new Tag_Byte(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addShort(String name, short value)
    {
        this.addTag(new Tag_Short(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addLong(String name, long value)
    {
        this.addTag(new Tag_Long(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addByteArray(String name, byte[] value)
    {
        this.addTag(new Tag_Byte_Array(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addIntArray(String name, int[] value)
    {
        this.addTag(new Tag_Int_Array(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ICompoundTag addLongArray(String name, long[] value)
    {
        this.addTag(new Tag_Long_Array(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasTag(String tagName)
    {
        return findDirectChild(tagName) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITag<?> getTag(String name)
    {
        return findDirectChild(name);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setString(String name, String value)
    {
        return setTag(new Tag_String(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setInt(String name, int value)
    {
        return setTag(new Tag_Int(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setDouble(String name, double value)
    {
        return setTag(new Tag_Double(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setFloat(String name, float value)
    {
        return setTag(new Tag_Float(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setByte(String name, byte value)
    {
        return setTag(new Tag_Byte(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setShort(String name, short value)
    {
        return setTag(new Tag_Short(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setLong(String name, long value)
    {
        return setTag(new Tag_Long(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setByteArray(String name, byte[] value)
    {
        return setTag(new Tag_Byte_Array(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setIntArray(String name, int[] value)
    {
        return setTag(new Tag_Int_Array(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setLongArray(String name, long[] value)
    {
        return setTag(new Tag_Long_Array(name, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag setTag(ITag<?> tag)
    {
        validateChildTag(tag);
        removeDirectChild(tag.getName());
        addTag(tag);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag removeTag(String name)
    {
        removeDirectChild(name);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag removeTags(String... names)
    {
        for (String name : names)
        {
            removeDirectChild(name);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICompoundTag clear()
    {
        data.clear();
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return data.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    /**
     * Returns a string with the compound name and nested tags.
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
