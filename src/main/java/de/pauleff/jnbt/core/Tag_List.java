package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.util.NBTTags;

import java.util.ArrayList;

/**
 * A list with one fixed element type. Type 0 (END) is allowed only for empty lists. Inputs and
 * getData() use shallow copies with shared element objects.
 *
 * @since 0.1
 */
public class Tag_List extends Collection_Tag implements IListTag
{
    private final int listTypeID;

    /**
     * Creates an empty list with the element type.
     *
     * @param listTypeID the NBT type ID for all elements in this list
     * @throws IllegalArgumentException if the element type ID is invalid
     */
    public Tag_List(int listTypeID)
    {
        this(null, listTypeID);
    }

    /**
     * Creates an empty named list with the element type.
     *
     * @param name       the tag name
     * @param listTypeID the NBT type ID for all elements in this list
     */
    public Tag_List(String name, int listTypeID)
    {
        this(name, listTypeID, new ArrayList<>());
    }

    /**
     * Creates a list filled with elements of the given type.
     *
     * @param name       the tag name
     * @param listTypeID the NBT type ID for all elements in this list
     * @param data       the initial collection of elements
     * @throws IllegalArgumentException if the type ID is invalid, data is null,
     * or an element is null, END, or of the wrong type
     */
    public Tag_List(String name, int listTypeID, ArrayList<Tag<?>> data)
    {
        super(NBTTags.Tag_List.getId(), name, validatedCopy(listTypeID, data));
        this.listTypeID = listTypeID;
    }

    /** Copies the elements after checking the declared type and each element. */
    private static ArrayList<Tag<?>> validatedCopy(int type, ArrayList<Tag<?>> values)
    {
        if (NBTTags.getById(type) == null || values == null)
        {
            throw new IllegalArgumentException("List requires a valid element type and non-null data");
        }
        ArrayList<Tag<?>> copy = new ArrayList<>(values);
        for (Tag<?> child : copy)
        {
            validateChildTag(child);
            if (child.getId() != type)
            {
                throw new IllegalArgumentException("List element type mismatch: expected " + type + ", got " + child.getId());
            }
        }
        return copy;
    }

    /** {@inheritDoc IListTag} */
    @Override
    public ArrayList<Tag<?>> getData()
    {
        return new ArrayList<>(data);
    }

    /**
     * Replaces the elements with a validated copy, preserving the declared type.
     *
     * @param data the replacement elements
     * @throws IllegalArgumentException if data is null or contains incompatible elements
     */
    @Override
    public void setData(ArrayList<Tag<?>> data)
    {
        this.data = validatedCopy(listTypeID, data);
    }

    /**
     * Copies the name and elements from a list with the same declared element type.
     *
     * @param newTag the source list
     * @throws IllegalArgumentException if the source is not a list of the same element type
     */
    @Override
    public void editTag(ITag<?> newTag)
    {
        if (!(newTag instanceof IListTag list) || list.getListTypeID() != listTypeID)
        {
            throw new IllegalArgumentException("Source must be a list with element type " + listTypeID);
        }
        validateNameChange(list.getName());
        setData(list.getData());
        setName(list.getName());
    }

    /** {@inheritDoc} */
    public int getListTypeID()
    {
        return listTypeID;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addString(String name, String value)
    {
        this.addTag(new Tag_String(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addInt(String name, int value)
    {
        this.addTag(new Tag_Int(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addDouble(String name, double value)
    {
        this.addTag(new Tag_Double(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addCompound(String name)
    {
        this.addTag(new Tag_Compound(name));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addFloat(String name, float value)
    {
        this.addTag(new Tag_Float(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addByte(String name, byte value)
    {
        this.addTag(new Tag_Byte(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addShort(String name, short value)
    {
        this.addTag(new Tag_Short(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public IListTag addLong(String name, long value)
    {
        this.addTag(new Tag_Long(name, value));
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setString(int index, String name, String value)
    {
        return setTag(index, new Tag_String(name, value));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setInt(int index, String name, int value)
    {
        return setTag(index, new Tag_Int(name, value));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setDouble(int index, String name, double value)
    {
        return setTag(index, new Tag_Double(name, value));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setCompound(int index, String name)
    {
        return setTag(index, new Tag_Compound(name));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setFloat(int index, String name, float value)
    {
        return setTag(index, new Tag_Float(name, value));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setByte(int index, String name, byte value)
    {
        return setTag(index, new Tag_Byte(name, value));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setShort(int index, String name, short value)
    {
        return setTag(index, new Tag_Short(name, value));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setLong(int index, String name, long value)
    {
        return setTag(index, new Tag_Long(name, value));
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag setTag(int index, ITag<?> tag)
    {
        validateIndex(index);
        validateChildTag(tag);
        if (tag.getId() != this.listTypeID)
        {
            throw new IllegalArgumentException(String.format("Tag type mismatch! Expected %d but got %d", this.listTypeID, tag.getId()));
        }
        data.set(index, (Tag<?>) tag);
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public IListTag removeAt(int index)
    {
        validateIndex(index);
        data.remove(index);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public IListTag remove(ITag<?> tag)
    {
        data.remove(tag);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public IListTag clear()
    {
        data.clear();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    @Override
    public ITag<?> get(int index)
    {
        validateIndex(index);
        return data.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public int size()
    {
        return data.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        return data.isEmpty();
    }

    /**
     * Validates that the given index is within bounds.
     *
     * @param index the index to validate
     * @throws IndexOutOfBoundsException if index is invalid
     */
    private void validateIndex(int index)
    {
        if (index < 0 || index >= data.size())
        {
            throw new IndexOutOfBoundsException(String.format("Index %d out of bounds for list size %d", index, data.size()));
        }
    }

    /**
     * Returns a string with the list type, name, and elements.
     *
     * @return formatted multi-line string representation
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NBTTags.Tag_List.getName());
        sb.append("(name=").append(getName()).append(", type=").append(getListTypeID()).append(", size=").append(data.size()).append("):");
        for (Tag<?> tag : data)
        {
            sb.append("\n  ").append(tag.toString().replace("\n", "\n  "));
        }
        sb.append("\n").append(NBTTags.Tag_End.getName());
        sb.append("(name=").append(getName()).append(")");
        return sb.toString();
    }
}
