package me.paulferlitz.api;

import java.util.ArrayList;

/**
 * Interface for NBT compound tags - the workhorse of NBT data structures.
 * Compounds function like dictionaries or maps, storing named child tags in key-value pairs.
 * They're essential for organizing complex data like player inventories, world chunks, and entity properties.
 *
 * @author Paul Ferlitz
 * @see ITag
 * @see IListTag
 */
public interface ICompoundTag extends ITag<ArrayList<me.paulferlitz.core.Tag<?>>>
{
    /**
     * Checks whether this compound contains a tag with the specified name.
     *
     * @param tagName The name to search for
     * @return {@code true} if a tag with this name exists, {@code false} otherwise
     */
    boolean hasTag(String tagName);

    /**
     * Adds a string tag with the given name and value.
     *
     * @param name The tag name
     * @param value The {@link java.lang.String} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addString(String name, String value);

    /**
     * Adds an integer tag with the given name and value.
     *
     * @param name The tag name
     * @param value The {@link java.lang.Integer} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addInt(String name, int value);

    /**
     * Adds a double-precision floating point tag.
     *
     * @param name The tag name
     * @param value The {@link java.lang.Double} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addDouble(String name, double value);

    /**
     * Adds a single-precision floating point tag.
     *
     * @param name The tag name
     * @param value The {@link java.lang.Float} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addFloat(String name, float value);

    /**
     * Adds a byte tag with the given name and value.
     *
     * @param name The tag name
     * @param value The {@link java.lang.Byte} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addByte(String name, byte value);

    /**
     * Adds a short integer tag.
     *
     * @param name The tag name
     * @param value The {@link java.lang.Short} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addShort(String name, short value);

    /**
     * Adds a long integer tag.
     *
     * @param name The tag name
     * @param value The {@link java.lang.Long} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addLong(String name, long value);
}