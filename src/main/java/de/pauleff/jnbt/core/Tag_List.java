package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.util.NBTTags;

import java.util.ArrayList;

/**
 * Represents an NBT list tag - a homogeneous collection containing multiple
 * tags of the same type, accessed by index rather than name.
 * <p>List tags enforce type consistency, ensuring all contained elements
 * match the declared list type. They provide indexed access and fluent
 * builder methods for type-safe element addition.</p>
 * <p>Extends {@link Collection_Tag} with list-specific type validation and
 * implements {@link IListTag} for standardized access.</p>
 * <p>Collection inputs are copied, and {@link #getData()} returns a shallow
 * snapshot. Use list operations to change membership. Type 0 (END) is valid
 * only for empty lists.</p>
 *
 */
public class Tag_List extends Collection_Tag implements IListTag
{
    private final int listTypeID;

    /**
     * Creates an empty list with the specified element type.
     *
     * @param listTypeID the NBT type ID for all elements in this list
     * @throws IllegalArgumentException if the element type ID is invalid
     */
    public Tag_List(int listTypeID)
    {
        this(null, listTypeID);
    }

    /**
     * Creates an empty named list with the specified element type.
     *
     * @param name       the tag name
     * @param listTypeID the NBT type ID for all elements in this list
     */
    public Tag_List(String name, int listTypeID)
    {
        this(name, listTypeID, new ArrayList<>());
    }

    /**
     * Creates a list pre-populated with elements of the specified type.
     *
     * @param name       the tag name
     * @param listTypeID the NBT type ID for all elements in this list
     * @param data       the initial collection of elements
     * @throws IllegalArgumentException if the type ID is invalid, data is null,
     *                                  or an element is null, END, or of the wrong type
     */
    public Tag_List(String name, int listTypeID, ArrayList<Tag<?>> data)
    {
        super(NBTTags.Tag_List.getId(), name, validatedCopy(listTypeID, data));
        this.listTypeID = listTypeID;
    }

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

    /**
     * Returns a shallow snapshot of the elements. Use list methods to change membership.
     * The element objects themselves remain shared.
     *
     * @return a copy of the element collection
     */
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
        setData(list.getData());
        setName(list.getName());
    }

    /**
     * Returns the NBT type identifier for elements in this list.
     *
     * <p>All elements must match this type to maintain list homogeneity.</p>
     *
     * @return the NBT type ID of list elements
     */
    public int getListTypeID()
    {
        return listTypeID;
    }

    /**
     * Adds a string element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as string type.</p>
     *
     * @param name  the element name
     * @param value the string value
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_String}
     * @see #getListTypeID()
     */
    public IListTag addString(String name, String value)
    {
        this.addTag(new Tag_String(name, value));
        return this;
    }

    /**
     * Adds an integer element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as integer type.</p>
     *
     * @param name  the element name
     * @param value the integer value
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_Int}
     * @see #getListTypeID()
     */
    public IListTag addInt(String name, int value)
    {
        this.addTag(new Tag_Int(name, value));
        return this;
    }

    /**
     * Adds a double-precision element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as double type.</p>
     *
     * @param name  the element name
     * @param value the double value
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_Double}
     * @see #getListTypeID()
     */
    public IListTag addDouble(String name, double value)
    {
        this.addTag(new Tag_Double(name, value));
        return this;
    }

    /**
     * Adds a compound element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as compound type.</p>
     *
     * @param name the element name
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_Compound}
     * @see #getListTypeID()
     */
    public IListTag addCompound(String name)
    {
        this.addTag(new Tag_Compound(name));
        return this;
    }

    /**
     * Adds a float element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as float type.</p>
     *
     * @param name  the element name
     * @param value the float value
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_Float}
     * @see #getListTypeID()
     */
    public IListTag addFloat(String name, float value)
    {
        this.addTag(new Tag_Float(name, value));
        return this;
    }

    /**
     * Adds a byte element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as byte type.</p>
     *
     * @param name  the element name
     * @param value the byte value
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_Byte}
     * @see #getListTypeID()
     */
    public IListTag addByte(String name, byte value)
    {
        this.addTag(new Tag_Byte(name, value));
        return this;
    }

    /**
     * Adds a short element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as short type.</p>
     *
     * @param name  the element name
     * @param value the short value
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_Short}
     * @see #getListTypeID()
     */
    public IListTag addShort(String name, short value)
    {
        this.addTag(new Tag_Short(name, value));
        return this;
    }

    /**
     * Adds a long element to this list with fluent chaining.
     *
     * <p>Only valid for lists declared as long type.</p>
     *
     * @param name  the element name
     * @param value the long value
     * @return this list tag for fluent operations
     * @throws IllegalArgumentException if list type is not {@link Tag_Long}
     * @see #getListTypeID()
     */
    public IListTag addLong(String name, long value)
    {
        this.addTag(new Tag_Long(name, value));
        return this;
    }

    @Override
    public IListTag setString(int index, String name, String value)
    {
        return setTag(index, new Tag_String(name, value));
    }

    @Override
    public IListTag setInt(int index, String name, int value)
    {
        return setTag(index, new Tag_Int(name, value));
    }

    @Override
    public IListTag setDouble(int index, String name, double value)
    {
        return setTag(index, new Tag_Double(name, value));
    }

    @Override
    public IListTag setCompound(int index, String name)
    {
        return setTag(index, new Tag_Compound(name));
    }

    @Override
    public IListTag setFloat(int index, String name, float value)
    {
        return setTag(index, new Tag_Float(name, value));
    }

    @Override
    public IListTag setByte(int index, String name, byte value)
    {
        return setTag(index, new Tag_Byte(name, value));
    }

    @Override
    public IListTag setShort(int index, String name, short value)
    {
        return setTag(index, new Tag_Short(name, value));
    }

    @Override
    public IListTag setLong(int index, String name, long value)
    {
        return setTag(index, new Tag_Long(name, value));
    }

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

    @Override
    public IListTag removeAt(int index)
    {
        validateIndex(index);
        data.remove(index);
        return this;
    }

    @Override
    public IListTag remove(ITag<?> tag)
    {
        data.remove(tag);
        return this;
    }

    @Override
    public IListTag clear()
    {
        data.clear();
        return this;
    }

    @Override
    public ITag<?> get(int index)
    {
        validateIndex(index);
        return data.get(index);
    }

    @Override
    public int size()
    {
        return data.size();
    }

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

    // Interface methods from IListTag are inherited from Collection_Tag

    /**
     * Generates a hierarchical string representation showing list metadata and elements.
     *
     * <p>Output includes tag type, name, element type, size, and formatted child elements.</p>
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
