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
     * <p><b>When to use {@code end()}:</b></p>
     * <ul>
     *   <li>Terminal operations - when finishing the current branch without further chaining</li>
     *   <li>Simple single-level nesting where parent type is obvious</li>
     *   <li>Variable storage patterns where builders are stored in variables</li>
     *   <li>Legacy code compatibility</li>
     * </ul>
     *
     * <p><b>Consider using type-safe alternatives:</b></p>
     * <ul>
     *   <li>{@link #endCompound()} - when continuing to build on a compound parent</li>
     *   <li>{@link #endList()} - when continuing to build on a list parent</li>
     * </ul>
     *
     * @return the parent {@link NBTBuilder} to continue building
     * @throws IllegalStateException if called on a root builder (use {@link #build()} instead)
     * @since 1.3
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
            return parentCompound;
        }
        else if (parent instanceof ListBuilder parentList)
        {
            parentList.addBuiltTag(compound);
            return parentList;
        }
        else
        {
            throw new IllegalStateException("Unknown parent builder type");
        }
    }

    /**
     * Type-safe method to return to a CompoundBuilder parent.
     * Provides compile-time safety for fluent method chaining when you know the parent is a compound.
     *
     * <p><b>When to use {@code endCompound()}:</b></p>
     * <ul>
     *   <li>Complex fluent chaining where you need to continue adding to a compound parent</li>
     *   <li>When you want compile-time type safety instead of runtime casting</li>
     *   <li>Building deeply nested structures with multiple compound levels</li>
     * </ul>
     *
     * <p><b>Example usage:</b></p>
     * <pre>{@code
     * NBTBuilder.compound("Player")
     *     .addCompound("Stats")
     *         .addInt("level", 50)
     *     .endCompound()  // ← Type-safe return to Player compound
     *     .addString("name", "Steve");  // ← Can continue building Player
     * }</pre>
     *
     * @return the parent as a CompoundBuilder
     * @throws IllegalStateException if parent is not a CompoundBuilder
     * @since 1.5
     */
    public CompoundBuilder endCompound()
    {
        NBTBuilder parent = end();
        if (parent instanceof CompoundBuilder compound) {
            return compound;
        }
        throw new IllegalStateException("Parent is not a CompoundBuilder");
    }

    /**
     * Type-safe method to return to a ListBuilder parent.
     * Provides compile-time safety for fluent method chaining when you know the parent is a list.
     *
     * <p><b>When to use {@code endList()}:</b></p>
     * <ul>
     *   <li>Adding multiple compound elements to a list with continued chaining</li>
     *   <li>When you want compile-time type safety for list operations</li>
     *   <li>Building arrays of complex objects</li>
     * </ul>
     *
     * <p><b>Example usage:</b></p>
     * <pre>{@code
     * NBTBuilder.list("inventory", NBTTags.Tag_Compound)
     *     .addCompound("item1")
     *         .addString("type", "sword")
     *     .endList()  // ← Type-safe return to inventory list
     *     .addCompound("item2")  // ← Can continue adding to list
     *         .addString("type", "potion")
     *     .endList();
     * }</pre>
     *
     * @return the parent as a ListBuilder
     * @throws IllegalStateException if parent is not a ListBuilder
     * @since 1.5
     */
    public ListBuilder endList()
    {
        NBTBuilder parent = end();
        if (parent instanceof ListBuilder list) {
            return list;
        }
        throw new IllegalStateException("Parent is not a ListBuilder");
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