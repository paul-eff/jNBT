package de.paulferlitz.builder;

import de.paulferlitz.api.IListTag;
import de.paulferlitz.core.*;
import de.paulferlitz.util.NBTTags;

import java.util.ArrayList;

/**
 * Fluent builder for creating type-safe NBT lists. All elements must share the same NBT type,
 * preventing common structural errors when building Minecraft NBT data.
 *
 * <p>Build complex inventory structures with confidence:</p>
 * <pre>{@code
 * Tag_List inventory = NBTBuilder.list("Inventory", NBTTags.Tag_Compound)
 *     .addCompound("item1")
 *         .addString("id", "minecraft:diamond_sword")
 *         .addInt("Count", 1)
 *     .end()
 *     .build();
 * }</pre>
 *
 * @author Paul Ferlitz
 * @see NBTBuilder
 * @see CompoundBuilder
 */
public class ListBuilder extends NBTBuilder
{
    private final Tag_List list;
    private final NBTTags listType;

    /**
     * Creates a new list builder with strict type enforcement.
     *
     * @param name     the list tag name
     * @param listType the NBT type all elements must match
     * @param parent   the parent builder for nesting support, or null for root lists
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

    /*
     * ========== TYPE-SAFE FLUENT API METHODS ==========
     */

    /**
     * Adds a string element to the list.
     *
     * @param name  the element name
     * @param value the string value
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * @return this builder for chaining
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
     * Adds a pre-built tag to this list. Used internally by nested builders.
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
                    listType.getName(), tagType.getName()));
        }

        list.addTag(tag);
    }

    /*
     * ========== BUILDER COMPLETION METHODS ==========
     */

    /**
     * Completes construction and returns the built list.
     *
     * @return the completed {@link IListTag}
     * @throws IllegalStateException if this is a nested builder (call {@link #end()} first)
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
     * Finishes this nested list and returns to the parent builder.
     * Automatically integrates the completed list into its parent structure.
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
     * @return the parent {@link NBTBuilder}
     * @throws IllegalStateException if this is a root builder (call {@link #build()} instead)
     * @since 1.3
     */
    @Override
    public NBTBuilder end()
    {
        if (parent == null)
        {
            throw new IllegalStateException("Cannot end root builder - call build() instead");
        }

        // Add this list to the parent
        if (parent instanceof CompoundBuilder parentCompound)
        {
            parentCompound.getCompound().addTag(list);
            return parentCompound;
        } else if (parent instanceof ListBuilder parentList)
        {
            parentList.addBuiltTag(list);
            return parentList;
        } else
        {
            throw new IllegalStateException("Unknown parent builder type");
        }
    }

    /*
     * ========== VALIDATION METHODS ==========
     */

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
     * Type-safe method to return to a CompoundBuilder parent.
     * Provides compile-time safety for fluent method chaining when you know the parent is a compound.
     *
     * <p><b>When to use {@code endCompound()}:</b></p>
     * <ul>
     *   <li>Lists nested within compounds where you need to continue building the compound</li>
     *   <li>When you want compile-time type safety instead of runtime casting</li>
     *   <li>Building complex structures with mixed container types</li>
     * </ul>
     *
     * <p><b>Example usage:</b></p>
     * <pre>{@code
     * NBTBuilder.compound("Player")
     *     .addList("inventory", NBTTags.Tag_String)
     *         .addString("item1", "sword")
     *         .addString("item2", "potion")
     *     .endCompound()  // ← Type-safe return to Player compound
     *     .addInt("level", 50);  // ← Can continue building Player
     * }</pre>
     *
     * @return the parent as a CompoundBuilder
     * @throws IllegalStateException if parent is not a CompoundBuilder
     * @since 1.5
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
     *   <li>Nested lists (lists within lists) where you need to continue building the parent list</li>
     *   <li>When you want compile-time type safety for complex list structures</li>
     *   <li>Building multi-dimensional array-like structures</li>
     * </ul>
     *
     * <p><b>Example usage:</b></p>
     * <pre>{@code
     * NBTBuilder.list("matrix", NBTTags.Tag_List)
     *     .addList("row1", NBTTags.Tag_Int)
     *         .addInt("col1", 1)
     *         .addInt("col2", 2)
     *     .endList()  // ← Type-safe return to matrix list
     *     .addList("row2", NBTTags.Tag_Int)  // ← Can continue adding rows
     *         .addInt("col1", 3)
     *         .addInt("col2", 4)
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
        if (parent instanceof ListBuilder list)
        {
            return list;
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
     * Provides access to the underlying list for parent builders.
     *
     * @return the list being constructed
     */
    protected Tag_List getList()
    {
        return list;
    }
}