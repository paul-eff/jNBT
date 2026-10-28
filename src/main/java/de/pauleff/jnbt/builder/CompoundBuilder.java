package de.pauleff.jnbt.builder;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.util.NBTTags;

/**
 * Builds compound tags with named children. Add methods reject duplicate names. Set methods replace
 * direct children with the same name.
 *
 * @since 1.3
 */
public class CompoundBuilder extends NBTBuilder
{
    private final Tag_Compound compound;

    /**
     * Creates a compound builder ready for use.
     *
     * @param name   the compound name
     * @param parent the parent builder, or null for a root compound
     */
    public CompoundBuilder(String name, NBTBuilder parent)
    {
        super(name, parent);
        validateName(name);
        this.compound = new Tag_Compound(name);
    }

    /**
     * Creates a compound builder from an existing compound tag for modification.
     *
     * @param existingCompound the existing compound to modify
     * @param parent           the parent builder, or null for a root compound
     */
    public CompoundBuilder(ICompoundTag existingCompound, NBTBuilder parent)
    {
        super(existingCompound.getName(), parent);
        if (existingCompound instanceof Tag_Compound)
        {
            this.compound = (Tag_Compound) existingCompound;
        } else
        {
            throw new IllegalArgumentException("Cannot build from non-Tag_Compound implementation");
        }
    }

    /**
     * Stores a string value.
     *
     * @param name  the tag name
     * @param value the string content
     * @return this builder
     */
    public CompoundBuilder addString(String name, String value)
    {
        validateName(name);
        compound.addString(name, value);
        return this;
    }

    /**
     * Stores an integer value.
     *
     * @param name  the tag name
     * @param value the numeric content
     * @return this builder
     */
    public CompoundBuilder addInt(String name, int value)
    {
        validateName(name);
        compound.addInt(name, value);
        return this;
    }

    /**
     * Stores a double value.
     *
     * @param name  the tag name
     * @param value the floating-point content
     * @return this builder
     */
    public CompoundBuilder addDouble(String name, double value)
    {
        validateName(name);
        compound.addDouble(name, value);
        return this;
    }

    /**
     * Stores a float.
     *
     * @param name  the tag name
     * @param value the floating-point content
     * @return this builder
     */
    public CompoundBuilder addFloat(String name, float value)
    {
        validateName(name);
        compound.addFloat(name, value);
        return this;
    }

    /**
     * Stores a byte value.
     *
     * @param name  the tag name
     * @param value the byte content
     * @return this builder
     */
    public CompoundBuilder addByte(String name, byte value)
    {
        validateName(name);
        compound.addByte(name, value);
        return this;
    }

    /**
     * Stores a short integer.
     *
     * @param name  the tag name
     * @param value the numeric content
     * @return this builder
     */
    public CompoundBuilder addShort(String name, short value)
    {
        validateName(name);
        compound.addShort(name, value);
        return this;
    }

    /**
     * Stores a long integer.
     *
     * @param name  the tag name
     * @param value the numeric content
     * @return this builder
     */
    public CompoundBuilder addLong(String name, long value)
    {
        validateName(name);
        compound.addLong(name, value);
        return this;
    }

    /**
     * Stores a byte array.
     *
     * @param name  the tag name
     * @param value the byte array content
     * @return this builder
     */
    public CompoundBuilder addByteArray(String name, byte[] value)
    {
        validateName(name);
        compound.addByteArray(name, value);
        return this;
    }

    /**
     * Stores an integer array.
     *
     * @param name  the tag name
     * @param value the integer array content
     * @return this builder
     */
    public CompoundBuilder addIntArray(String name, int[] value)
    {
        validateName(name);
        compound.addIntArray(name, value);
        return this;
    }

    /**
     * Stores a long array.
     *
     * @param name  the tag name
     * @param value the long array content
     * @return this builder
     */
    public CompoundBuilder addLongArray(String name, long[] value)
    {
        validateName(name);
        compound.addLongArray(name, value);
        return this;
    }

    /**
     * Adds an existing tag into this compound.
     *
     * @param tag the existing tag to include
     * @return this builder
     * @throws IllegalArgumentException if tag is null
     */
    public CompoundBuilder addTag(Tag<?> tag)
    {
        if (tag == null)
        {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        validateName(tag.getName());
        compound.addTag(tag);
        return this;
    }

    /**
     * Starts a nested list builder.
     *
     * @param name     the tag name
     * @param listType the element type this list will contain
     * @return a {@link ListBuilder} for adding list elements
     */
    public ListBuilder addList(String name, NBTTags listType)
    {
        validateName(name);
        return new ListBuilder(name, listType, this);
    }

    /**
     * Starts a nested compound builder.
     *
     * @param name the tag name
     * @return a new {@code CompoundBuilder} for the nested structure
     */
    public CompoundBuilder addCompound(String name)
    {
        validateName(name);
        return new CompoundBuilder(name, this);
    }

    /**
     * Updates or creates a string value.
     *
     * @param name  the tag name
     * @param value the string content
     * @return this builder
     */
    public CompoundBuilder setString(String name, String value)
    {
        validateName(name);
        compound.setString(name, value);
        return this;
    }

    /**
     * Updates or creates an integer value.
     *
     * @param name  the tag name
     * @param value the numeric content
     * @return this builder
     */
    public CompoundBuilder setInt(String name, int value)
    {
        validateName(name);
        compound.setInt(name, value);
        return this;
    }

    /**
     * Updates or creates a double value.
     *
     * @param name  the tag name
     * @param value the floating-point content
     * @return this builder
     */
    public CompoundBuilder setDouble(String name, double value)
    {
        validateName(name);
        compound.setDouble(name, value);
        return this;
    }

    /**
     * Updates or creates a float value.
     *
     * @param name  the tag name
     * @param value the floating-point content
     * @return this builder
     */
    public CompoundBuilder setFloat(String name, float value)
    {
        validateName(name);
        compound.setFloat(name, value);
        return this;
    }

    /**
     * Updates or creates a byte value.
     *
     * @param name  the tag name
     * @param value the byte content
     * @return this builder
     */
    public CompoundBuilder setByte(String name, byte value)
    {
        validateName(name);
        compound.setByte(name, value);
        return this;
    }

    /**
     * Updates or creates a short integer.
     *
     * @param name  the tag name
     * @param value the numeric content
     * @return this builder
     */
    public CompoundBuilder setShort(String name, short value)
    {
        validateName(name);
        compound.setShort(name, value);
        return this;
    }

    /**
     * Updates or creates a long integer.
     *
     * @param name  the tag name
     * @param value the numeric content
     * @return this builder
     */
    public CompoundBuilder setLong(String name, long value)
    {
        validateName(name);
        compound.setLong(name, value);
        return this;
    }

    /**
     * Updates or creates a byte array.
     *
     * @param name  the tag name
     * @param value the byte array content
     * @return this builder
     */
    public CompoundBuilder setByteArray(String name, byte[] value)
    {
        validateName(name);
        compound.setByteArray(name, value);
        return this;
    }

    /**
     * Updates or creates an integer array.
     *
     * @param name  the tag name
     * @param value the integer array content
     * @return this builder
     */
    public CompoundBuilder setIntArray(String name, int[] value)
    {
        validateName(name);
        compound.setIntArray(name, value);
        return this;
    }

    /**
     * Updates or creates a long array.
     *
     * @param name  the tag name
     * @param value the long array content
     * @return this builder
     */
    public CompoundBuilder setLongArray(String name, long[] value)
    {
        validateName(name);
        compound.setLongArray(name, value);
        return this;
    }

    /**
     * Updates or replaces an existing tag.
     *
     * @param tag the tag to set
     * @return this builder
     */
    public CompoundBuilder setTag(Tag<?> tag)
    {
        if (tag == null)
        {
            throw new IllegalArgumentException("Tag cannot be null");
        }
        validateName(tag.getName());
        compound.setTag(tag);
        return this;
    }

    /**
     * Removes a tag by name.
     *
     * @param name the tag name to remove
     * @return this builder
     */
    public CompoundBuilder removeTag(String name)
    {
        compound.removeTag(name);
        return this;
    }

    /**
     * Removes multiple tags by name.
     *
     * @param names the tag names to remove
     * @return this builder
     */
    public CompoundBuilder removeTags(String... names)
    {
        compound.removeTags(names);
        return this;
    }

    /**
     * Removes all tags from this compound.
     *
     * @return this builder
     */
    public CompoundBuilder clear()
    {
        compound.clear();
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public ICompoundTag build()
    {
        if (parent != null)
        {
            throw new IllegalStateException("Cannot build nested compound - call end() first to return to parent");
        }
        return compound;
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
        if (parent == null)
        {
            throw new IllegalStateException("Cannot end root builder - call build() instead");
        }

        if (parent instanceof CompoundBuilder parentCompound)
        {
            parentCompound.compound.addTag(compound);
            return parentCompound;
        } else if (parent instanceof ListBuilder parentList)
        {
            parentList.addBuiltTag(compound);
            return parentList;
        } else
        {
            throw new IllegalStateException("Unknown parent builder type");
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
        if (parent instanceof CompoundBuilder parentCompound)
        {
            return parentCompound;
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
        if (parent instanceof ListBuilder list)
        {
            return list;
        }
        throw new IllegalStateException("Parent is not a ListBuilder");
    }

    /**
     * Returns the compound being built.
     *
     * @return the compound tag under construction
     */
    protected Tag_Compound getCompound()
    {
        return compound;
    }
}
