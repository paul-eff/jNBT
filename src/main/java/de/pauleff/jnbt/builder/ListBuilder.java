package de.pauleff.jnbt.builder;

import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.util.NBTTags;

import java.util.ArrayList;

/**
 * Builds lists whose elements all have the declared NBT type.
 *
 * @since 1.3
 */
public class ListBuilder extends NBTBuilder
{
    private final Tag_List list;
    private final NBTTags listType;

    /**
     * Creates a list builder with a fixed element type.
     *
     * @param name     the list tag name
     * @param listType the NBT type all elements must match
     * @param parent   the parent builder, or null for a root list
     * @throws IllegalArgumentException if listType is null
     */
    public ListBuilder(String name, NBTTags listType, NBTBuilder parent)
    {
        super(name, parent);
        validateName(name);
        if (listType == null)
        {
            throw new IllegalArgumentException("List type cannot be null");
        }
        this.listType = listType;
        this.list = new Tag_List(name, listType.getId(), new ArrayList<>());
    }

    /**
     * Adds a string element to the list.
     *
     * @param name  the element name
     * @param value the string value
     * @return this builder
     * @throws IllegalArgumentException if this list doesn't accept strings
     */
    public ListBuilder addString(String name, String value)
    {
        validateListType(NBTTags.Tag_String);
        validateName(name);
        list.addString(name, value);
        return this;
    }

    /**
     * Adds an integer element to the list.
     *
     * @param name  the element name
     * @param value the integer value
     * @return this builder
     * @throws IllegalArgumentException if this list doesn't accept integers
     */
    public ListBuilder addInt(String name, int value)
    {
        validateListType(NBTTags.Tag_Int);
        validateName(name);
        list.addInt(name, value);
        return this;
    }

    /**
     * Adds a double element to the list.
     *
     * @param name  the element name
     * @param value the double value
     * @return this builder
     * @throws IllegalArgumentException if this list doesn't accept doubles
     */
    public ListBuilder addDouble(String name, double value)
    {
        validateListType(NBTTags.Tag_Double);
        validateName(name);
        list.addDouble(name, value);
        return this;
    }

    /**
     * Adds a float element to the list.
     *
     * @param name  the element name
     * @param value the float value
     * @return this builder
     * @throws IllegalArgumentException if this list doesn't accept floats
     */
    public ListBuilder addFloat(String name, float value)
    {
        validateListType(NBTTags.Tag_Float);
        validateName(name);
        list.addTag(new Tag_Float(name, value));
        return this;
    }

    /**
     * Adds a byte element to the list.
     *
     * @param name  the element name
     * @param value the byte value
     * @return this builder
     * @throws IllegalArgumentException if this list doesn't accept bytes
     */
    public ListBuilder addByte(String name, byte value)
    {
        validateListType(NBTTags.Tag_Byte);
        validateName(name);
        list.addTag(new Tag_Byte(name, value));
        return this;
    }

    /**
     * Adds a short element to the list.
     *
     * @param name  the element name
     * @param value the short value
     * @return this builder
     * @throws IllegalArgumentException if this list doesn't accept shorts
     */
    public ListBuilder addShort(String name, short value)
    {
        validateListType(NBTTags.Tag_Short);
        validateName(name);
        list.addTag(new Tag_Short(name, value));
        return this;
    }

    /**
     * Adds a long element to the list.
     *
     * @param name  the element name
     * @param value the long value
     * @return this builder
     * @throws IllegalArgumentException if this list doesn't accept longs
     */
    public ListBuilder addLong(String name, long value)
    {
        validateListType(NBTTags.Tag_Long);
        validateName(name);
        list.addTag(new Tag_Long(name, value));
        return this;
    }

    /**
     * Begins building a compound element within this list.
     *
     * @param name the compound element name
     * @return a {@link CompoundBuilder} for defining the compound's contents
     * @throws IllegalArgumentException if this list doesn't accept compounds
     */
    public CompoundBuilder addCompound(String name)
    {
        validateListType(NBTTags.Tag_Compound);
        validateName(name);
        return new CompoundBuilder(name, this);
    }

    /**
     * Begins building a nested list element within this list.
     *
     * @param name           the nested list element name
     * @param nestedListType the NBT type for elements in the nested list
     * @return a new {@link ListBuilder} for defining the nested list's contents
     * @throws IllegalArgumentException if this list doesn't accept nested lists
     */
    public ListBuilder addList(String name, NBTTags nestedListType)
    {
        validateListType(NBTTags.Tag_List);
        validateName(name);
        return new ListBuilder(name, nestedListType, this);
    }

    /**
     * Adds a completed tag from a nested builder.
     *
     * @param tag the completed tag to add
     * @throws IllegalArgumentException if tag is null or type doesn't match
     */
    protected void addBuiltTag(Tag<?> tag)
    {
        if (tag == null)
        {
            throw new IllegalArgumentException("Tag cannot be null");
        }

        NBTTags tagType = NBTTags.getById(tag.getId());
        if (tagType != listType)
        {
            throw new IllegalArgumentException(String.format(
                    "Tag type mismatch! List expects %s but got %s",
                    listType.getName(), tagType == null ? "unknown type ID " + tag.getId() : tagType.getName()));
        }

        list.addTag(tag);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public IListTag build()
    {
        if (parent != null)
        {
            throw new IllegalStateException("Cannot build nested list - call end() first to return to parent");
        }
        return list;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException {@inheritDoc}
     * @since 1.3.0
     */
    @Override
    public NBTBuilder end()
    {
        switch (parent)
        {
            case null -> throw new IllegalStateException("Cannot end root builder - call build() instead");
            case CompoundBuilder parentCompound ->
            {
                parentCompound.getCompound().addTag(list);
                return parentCompound;
            }
            case ListBuilder parentList ->
            {
                parentList.addBuiltTag(list);
                return parentList;
            }
            default -> throw new IllegalStateException("Unknown parent builder type");
        }

    }

    /**
     * Ensures the operation matches this list's required element type.
     *
     * @param expectedType the NBT type being added
     * @throws IllegalArgumentException if types don't match
     */
    private void validateListType(NBTTags expectedType)
    {
        if (listType != expectedType)
        {
            throw new IllegalArgumentException(String.format(
                    "List type mismatch! Expected %s but list is of type %s",
                    expectedType.getName(), listType.getName()));
        }
    }

    /**
     * Adds this tag to its parent and returns the compound parent builder.
     *
     * @return the parent as a CompoundBuilder
     * @throws IllegalStateException if parent is not a CompoundBuilder
     * @since 1.5.0
     */
    public CompoundBuilder endCompound()
    {
        NBTBuilder parent = end();
        if (parent instanceof CompoundBuilder compound)
        {
            return compound;
        }
        throw new IllegalStateException("Parent is not a CompoundBuilder");
    }

    /**
     * Adds this tag to its parent and returns the list parent builder.
     *
     * @return the parent as a ListBuilder
     * @throws IllegalStateException if parent is not a ListBuilder
     * @since 1.5.0
     */
    public ListBuilder endList()
    {
        NBTBuilder parent = end();
        if (parent instanceof ListBuilder parentList)
        {
            return parentList;
        }
        throw new IllegalStateException("Parent is not a ListBuilder");
    }

    /**
     * Returns the NBT type this list accepts for all elements.
     *
     * @return the enforced element type
     */
    public NBTTags getListType()
    {
        return listType;
    }

    /**
     * Returns the list being built.
     *
     * @return the list being constructed
     */
    protected Tag_List getList()
    {
        return list;
    }
}
