package de.pauleff.jnbt.builder;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.util.NBTTags;

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
 * @author Paul Ferlitz
 * @see NBTBuilder
 * @see ListBuilder
 * @see Tag_Compound
 */
public class CompoundBuilder extends NBTBuilder
{
    private final Tag_Compound compound;

    /**
     * Creates a new compound builder ready for tag assembly.
     *
     * @param name   the compound's identifier
     * @param parent the parent builder for nesting support, or null for root compounds
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
     * @param parent           the parent builder for nesting support, or null for root compounds
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

    /*
     * ========== FLUENT API METHODS ==========
     */

    /**
     * Stores a string value with the specified name.
     *
     * @param name  the tag identifier
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
     * @param name  the tag identifier
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
     * @param name  the tag identifier
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
     * @param name  the tag identifier
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
     * @param name  the tag identifier
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
     * @param name  the tag identifier
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
     * @param name  the tag identifier
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
     * Stores a byte array with the specified name.
     *
     * @param name  the tag identifier
     * @param value the byte array content
     * @return this builder for chaining
     */
    public CompoundBuilder addByteArray(String name, byte[] value)
    {
        validateName(name);
        compound.addByteArray(name, value);
        return this;
    }

    /**
     * Stores an integer array with the specified name.
     *
     * @param name  the tag identifier
     * @param value the integer array content
     * @return this builder for chaining
     */
    public CompoundBuilder addIntArray(String name, int[] value)
    {
        validateName(name);
        compound.addIntArray(name, value);
        return this;
    }

    /**
     * Stores a long array with the specified name.
     *
     * @param name  the tag identifier
     * @param value the long array content
     * @return this builder for chaining
     */
    public CompoundBuilder addLongArray(String name, long[] value)
    {
        validateName(name);
        compound.addLongArray(name, value);
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
     * @param name     the list identifier
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
     * ========== UPDATE/DELETE METHODS ==========
     */

    /**
     * Updates or creates a string value with the specified name.
     *
     * @param name  the tag identifier
     * @param value the string content
     * @return this builder for chaining
     */
    public CompoundBuilder setString(String name, String value)
    {
        validateName(name);
        compound.setString(name, value);
        return this;
    }

    /**
     * Updates or creates an integer value with the specified name.
     *
     * @param name  the tag identifier
     * @param value the numeric content
     * @return this builder for chaining
     */
    public CompoundBuilder setInt(String name, int value)
    {
        validateName(name);
        compound.setInt(name, value);
        return this;
    }

    /**
     * Updates or creates a double-precision value with the specified name.
     *
     * @param name  the tag identifier
     * @param value the floating-point content
     * @return this builder for chaining
     */
    public CompoundBuilder setDouble(String name, double value)
    {
        validateName(name);
        compound.setDouble(name, value);
        return this;
    }

    /**
     * Updates or creates a float value with the specified name.
     *
     * @param name  the tag identifier
     * @param value the floating-point content
     * @return this builder for chaining
     */
    public CompoundBuilder setFloat(String name, float value)
    {
        validateName(name);
        compound.setFloat(name, value);
        return this;
    }

    /**
     * Updates or creates a byte value with the specified name.
     *
     * @param name  the tag identifier
     * @param value the byte content
     * @return this builder for chaining
     */
    public CompoundBuilder setByte(String name, byte value)
    {
        validateName(name);
        compound.setByte(name, value);
        return this;
    }

    /**
     * Updates or creates a short integer with the specified name.
     *
     * @param name  the tag identifier
     * @param value the numeric content
     * @return this builder for chaining
     */
    public CompoundBuilder setShort(String name, short value)
    {
        validateName(name);
        compound.setShort(name, value);
        return this;
    }

    /**
     * Updates or creates a long integer with the specified name.
     *
     * @param name  the tag identifier
     * @param value the numeric content
     * @return this builder for chaining
     */
    public CompoundBuilder setLong(String name, long value)
    {
        validateName(name);
        compound.setLong(name, value);
        return this;
    }

    /**
     * Updates or creates a byte array with the specified name.
     *
     * @param name  the tag identifier
     * @param value the byte array content
     * @return this builder for chaining
     */
    public CompoundBuilder setByteArray(String name, byte[] value)
    {
        validateName(name);
        compound.setByteArray(name, value);
        return this;
    }

    /**
     * Updates or creates an integer array with the specified name.
     *
     * @param name  the tag identifier
     * @param value the integer array content
     * @return this builder for chaining
     */
    public CompoundBuilder setIntArray(String name, int[] value)
    {
        validateName(name);
        compound.setIntArray(name, value);
        return this;
    }

    /**
     * Updates or creates a long array with the specified name.
     *
     * @param name  the tag identifier
     * @param value the long array content
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * @return this builder for chaining
     */
    public CompoundBuilder removeTags(String... names)
    {
        compound.removeTags(names);
        return this;
    }

    /**
     * Removes all tags from this compound.
     *
     * @return this builder for chaining
     */
    public CompoundBuilder clear()
    {
        compound.clear();
        return this;
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
     * @since 1.3.0
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
     * Provides access to the underlying compound for framework integration.
     *
     * @return the compound tag under construction
     */
    protected Tag_Compound getCompound()
    {
        return compound;
    }
}