package me.paulferlitz.builder;

import me.paulferlitz.api.ICompoundTag;
import me.paulferlitz.core.*;
import me.paulferlitz.util.NBTTags;

/**
 * Fluent builder for creating structured NBT compound tags with seamless nesting support.
 * Chain method calls to build complex hierarchical data structures effortlessly.
 *
 * <p>Build player data with nested inventory:</p>
 * <pre>{@code
 * Tag_Compound player = NBTBuilder.compound("Player")
 *     .addString("Name", "Steve")
 *     .addInt("Level", 50)
 *     .addList("Inventory", NBTTags.Tag_Compound)
 *         .addCompound("item1")
 *             .addString("id", "minecraft:diamond_sword")
 *         .end()
 *     .end()
 *     .build();
 * }</pre>
 *
 * @see NBTBuilder
 * @see ListBuilder
 * @see Tag_Compound
 * @author Paul Ferlitz
 */
public class CompoundBuilder extends NBTBuilder
{
    private final Tag_Compound compound;

    /**
     * Creates a new compound builder ready for tag assembly.
     *
     * @param name the compound's identifier
     * @param parent the parent builder for nesting support, or null for root compounds
     */
    public CompoundBuilder(String name, NBTBuilder parent)
    {
        super(name, parent);
        validateName(name);
        this.compound = new Tag_Compound(name);
    }

    /*
     * ========== FLUENT API METHODS ==========
     */

    /**
     * Stores a string value with the specified name.
     *
     * @param name the tag identifier
     * @param value the string content
     * @return this builder for chaining
     */
    public CompoundBuilder addString(String name, String value)
    {
        validateName(name);
        compound.addString(name, value);
        return this;
    }

    /**
     * Stores an integer value with the specified name.
     *
     * @param name the tag identifier
     * @param value the numeric content
     * @return this builder for chaining
     */
    public CompoundBuilder addInt(String name, int value)
    {
        validateName(name);
        compound.addInt(name, value);
        return this;
    }

    /**
     * Stores a double-precision value with the specified name.
     *
     * @param name the tag identifier
     * @param value the floating-point content
     * @return this builder for chaining
     */
    public CompoundBuilder addDouble(String name, double value)
    {
        validateName(name);
        compound.addDouble(name, value);
        return this;
    }

    /**
     * Stores a single-precision float with the specified name.
     *
     * @param name the tag identifier
     * @param value the floating-point content
     * @return this builder for chaining
     */
    public CompoundBuilder addFloat(String name, float value)
    {
        validateName(name);
        compound.addFloat(name, value);
        return this;
    }

    /**
     * Stores a byte value with the specified name.
     *
     * @param name the tag identifier
     * @param value the byte content
     * @return this builder for chaining
     */
    public CompoundBuilder addByte(String name, byte value)
    {
        validateName(name);
        compound.addByte(name, value);
        return this;
    }

    /**
     * Stores a short integer with the specified name.
     *
     * @param name the tag identifier
     * @param value the numeric content
     * @return this builder for chaining
     */
    public CompoundBuilder addShort(String name, short value)
    {
        validateName(name);
        compound.addShort(name, value);
        return this;
    }

    /**
     * Stores a long integer with the specified name.
     *
     * @param name the tag identifier
     * @param value the numeric content
     * @return this builder for chaining
     */
    public CompoundBuilder addLong(String name, long value)
    {
        validateName(name);
        compound.addLong(name, value);
        return this;
    }

    /**
     * Incorporates an existing tag into this compound.
     *
     * @param tag the pre-built tag to include
     * @return this builder for chaining
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
     * Creates a new list within this compound and switches to list-building mode.
     *
     * @param name the list identifier
     * @param listType the element type this list will contain
     * @return a {@link ListBuilder} for adding list elements
     * @see NBTTags
     */
    public ListBuilder addList(String name, NBTTags listType)
    {
        validateName(name);
        return new ListBuilder(name, listType, this);
    }

    /**
     * Creates a nested compound within this one and switches to nested-building mode.
     *
     * @param name the nested compound identifier
     * @return a new {@code CompoundBuilder} for the nested structure
     */
    public CompoundBuilder addCompound(String name)
    {
        validateName(name);
        return new CompoundBuilder(name, this);
    }

    /*
     * ========== BUILDER COMPLETION METHODS ==========
     */

    /**
     * Finalizes construction and returns the completed compound tag.
     * Only available for root-level compounds.
     *
     * @return the fully constructed {@link ICompoundTag}
     * @throws IllegalStateException if called on a nested builder (use {@link #end()} first)
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
     * Completes this nested compound and returns control to the parent builder.
     * Automatically integrates the completed compound into its parent structure.
     *
     * @return the parent {@link NBTBuilder} to continue building
     * @throws IllegalStateException if called on a root builder (use {@link #build()} instead)
     */
    @Override
    public NBTBuilder end()
    {
        if (parent == null)
        {
            throw new IllegalStateException("Cannot end root builder - call build() instead");
        }

        // Add this compound to the parent
        if (parent instanceof CompoundBuilder parentCompound)
        {
            parentCompound.compound.addTag(compound);
        }
        else if (parent instanceof ListBuilder parentList)
        {
            parentList.addBuiltTag(compound);
        }
        else
        {
            throw new IllegalStateException("Unknown parent builder type");
        }

        return parent;
    }

    /**
     * Provides access to the underlying compound for framework integration.
     *
     * @return the compound tag under construction
     */
    protected Tag_Compound getCompound()
    {
        return compound;
    }
}