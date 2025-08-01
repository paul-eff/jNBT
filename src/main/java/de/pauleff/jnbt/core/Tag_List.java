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
 *
 * @author Paul Ferlitz
 */
public class Tag_List extends Collection_Tag implements IListTag
{
    private final int listTypeID;

    /**
     * Creates an empty list with the specified element type.
     *
     * @param listTypeID the NBT type ID for all elements in this list
     */
    public Tag_List(int listTypeID)
    {
        super(NBTTags.Tag_List.getId());
        this.listTypeID = listTypeID;
    }

    /**
     * Creates an empty named list with the specified element type.
     *
     * @param name       the tag name
     * @param listTypeID the NBT type ID for all elements in this list
     */
    public Tag_List(String name, int listTypeID)
    {
        super(NBTTags.Tag_List.getId(), name);
        this.listTypeID = listTypeID;
    }

    /**
     * Creates a list pre-populated with elements of the specified type.
     *
     * @param name       the tag name
     * @param listTypeID the NBT type ID for all elements in this list
     * @param data       the initial collection of elements
     */
    public Tag_List(String name, int listTypeID, ArrayList<Tag<?>> data)
    {
        super(NBTTags.Tag_List.getId(), name, data);
        this.listTypeID = listTypeID;
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

    /*
     * ========== FLUENT API METHODS ==========
     */

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

    /*
     * ========== UPDATE METHODS (INDEX-BASED) ==========
     */

    @Override
    public IListTag setString(int index, String name, String value)
    {
        validateIndex(index);
        getData().set(index, new Tag_String(name, value));
        return this;
    }

    @Override
    public IListTag setInt(int index, String name, int value)
    {
        validateIndex(index);
        getData().set(index, new Tag_Int(name, value));
        return this;
    }

    @Override
    public IListTag setDouble(int index, String name, double value)
    {
        validateIndex(index);
        getData().set(index, new Tag_Double(name, value));
        return this;
    }

    @Override
    public IListTag setCompound(int index, String name)
    {
        validateIndex(index);
        getData().set(index, new Tag_Compound(name));
        return this;
    }

    @Override
    public IListTag setFloat(int index, String name, float value)
    {
        validateIndex(index);
        getData().set(index, new Tag_Float(name, value));
        return this;
    }

    @Override
    public IListTag setByte(int index, String name, byte value)
    {
        validateIndex(index);
        getData().set(index, new Tag_Byte(name, value));
        return this;
    }

    @Override
    public IListTag setShort(int index, String name, short value)
    {
        validateIndex(index);
        getData().set(index, new Tag_Short(name, value));
        return this;
    }

    @Override
    public IListTag setLong(int index, String name, long value)
    {
        validateIndex(index);
        getData().set(index, new Tag_Long(name, value));
        return this;
    }

    @Override
    public IListTag setTag(int index, ITag<?> tag)
    {
        validateIndex(index);
        if (tag.getId() != this.listTypeID)
        {
            throw new IllegalArgumentException(String.format("Tag type mismatch! Expected %d but got %d", this.listTypeID, tag.getId()));
        }
        getData().set(index, (Tag<?>) tag);
        return this;
    }

    /*
     * ========== DELETE METHODS ==========
     */

    @Override
    public IListTag removeAt(int index)
    {
        validateIndex(index);
        getData().remove(index);
        return this;
    }

    @Override
    public IListTag remove(ITag<?> tag)
    {
        getData().remove(tag);
        return this;
    }

    @Override
    public IListTag clear()
    {
        getData().clear();
        return this;
    }

    /*
     * ========== UTILITY METHODS ==========
     */

    @Override
    public ITag<?> get(int index)
    {
        validateIndex(index);
        return getData().get(index);
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
     * Validates that the given index is within bounds.
     *
     * @param index the index to validate
     * @throws IndexOutOfBoundsException if index is invalid
     */
    private void validateIndex(int index)
    {
        if (index < 0 || index >= getData().size())
        {
            throw new IndexOutOfBoundsException(String.format("Index %d out of bounds for list size %d", index, getData().size()));
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
