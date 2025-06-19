package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag;

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
public interface ICompoundTag extends ITag<ArrayList<Tag<?>>>
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
     * @param name  The tag name
     * @param value The {@link java.lang.String} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addString(String name, String value);

    /**
     * Adds an integer tag with the given name and value.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Integer} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addInt(String name, int value);

    /**
     * Adds a double-precision floating point tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Double} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addDouble(String name, double value);

    /**
     * Adds a single-precision floating point tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Float} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addFloat(String name, float value);

    /**
     * Adds a byte tag with the given name and value.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Byte} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addByte(String name, byte value);

    /**
     * Adds a short integer tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Short} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addShort(String name, short value);

    /**
     * Adds a long integer tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Long} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addLong(String name, long value);

    /**
     * Adds a byte array tag.
     *
     * @param name  The tag name
     * @param value The byte array value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addByteArray(String name, byte[] value);

    /**
     * Adds an integer array tag.
     *
     * @param name  The tag name
     * @param value The integer array value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addIntArray(String name, int[] value);

    /**
     * Adds a long array tag.
     *
     * @param name  The tag name
     * @param value The long array value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag addLongArray(String name, long[] value);

    /*
     * ========== CONVENIENCE GETTERS (REDUCE CASTING) ==========
     */

    /**
     * Retrieves a child tag by name without requiring casts.
     *
     * @param name The name of the tag to find
     * @return The tag if found, {@code null} otherwise
     */
    ITag<?> getTag(String name);

    /**
     * Retrieves a string value directly from a child tag.
     *
     * @param name The name of the string tag
     * @return The string value, or {@code null} if tag doesn't exist or isn't a string
     */
    String getString(String name);

    /**
     * Retrieves an integer value directly from a child tag.
     *
     * @param name The name of the integer tag
     * @return The integer value, or 0 if tag doesn't exist or isn't an integer
     */
    int getInt(String name);

    /**
     * Retrieves a double value directly from a child tag.
     *
     * @param name The name of the double tag
     * @return The double value, or 0.0 if tag doesn't exist or isn't a double
     */
    double getDouble(String name);

    /**
     * Retrieves a byte value directly from a child tag.
     *
     * @param name The name of the byte tag
     * @return The byte value, or 0 if tag doesn't exist or isn't a byte
     */
    byte getByte(String name);

    /**
     * Retrieves a float value directly from a child tag.
     *
     * @param name The name of the float tag
     * @return The float value, or 0.0f if tag doesn't exist or isn't a float
     */
    float getFloat(String name);

    /**
     * Retrieves a short value directly from a child tag.
     *
     * @param name The name of the short tag
     * @return The short value, or 0 if tag doesn't exist or isn't a short
     */
    short getShort(String name);

    /**
     * Retrieves a long value directly from a child tag.
     *
     * @param name The name of the long tag
     * @return The long value, or 0L if tag doesn't exist or isn't a long
     */
    long getLong(String name);

    /**
     * Retrieves a compound tag by name without casting.
     *
     * @param name The name of the compound tag
     * @return The {@link ICompoundTag} if found, {@code null} otherwise
     */
    ICompoundTag getCompound(String name);

    /**
     * Retrieves a list tag by name without casting.
     *
     * @param name The name of the list tag
     * @return The {@link IListTag} if found, {@code null} otherwise
     */
    IListTag getList(String name);

    /**
     * Retrieves a byte array value directly from a child tag.
     *
     * @param name The name of the byte array tag
     * @return The byte array value, or {@code null} if tag doesn't exist or isn't a byte array
     */
    byte[] getByteArray(String name);

    /**
     * Retrieves an integer array value directly from a child tag.
     *
     * @param name The name of the integer array tag
     * @return The integer array value, or {@code null} if tag doesn't exist or isn't an integer array
     */
    int[] getIntArray(String name);

    /**
     * Retrieves a long array value directly from a child tag.
     *
     * @param name The name of the long array tag
     * @return The long array value, or {@code null} if tag doesn't exist or isn't a long array
     */
    long[] getLongArray(String name);

    /*
     * ========== UPDATE METHODS (FLUENT API) ==========
     */

    /**
     * Updates or creates a string tag with the given name and value.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.String} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setString(String name, String value);

    /**
     * Updates or creates an integer tag with the given name and value.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Integer} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setInt(String name, int value);

    /**
     * Updates or creates a double-precision floating point tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Double} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setDouble(String name, double value);

    /**
     * Updates or creates a single-precision floating point tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Float} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setFloat(String name, float value);

    /**
     * Updates or creates a byte tag with the given name and value.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Byte} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setByte(String name, byte value);

    /**
     * Updates or creates a short integer tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Short} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setShort(String name, short value);

    /**
     * Updates or creates a long integer tag.
     *
     * @param name  The tag name
     * @param value The {@link java.lang.Long} value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setLong(String name, long value);

    /**
     * Updates or creates a byte array tag.
     *
     * @param name  The tag name
     * @param value The byte array value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setByteArray(String name, byte[] value);

    /**
     * Updates or creates an integer array tag.
     *
     * @param name  The tag name
     * @param value The integer array value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setIntArray(String name, int[] value);

    /**
     * Updates or creates a long array tag.
     *
     * @param name  The tag name
     * @param value The long array value to store
     * @return This compound for fluent method chaining
     */
    ICompoundTag setLongArray(String name, long[] value);

    /**
     * Updates or replaces an existing tag with a new one.
     *
     * @param tag The tag to set (replaces any existing tag with the same name)
     * @return This compound for fluent method chaining
     */
    ICompoundTag setTag(ITag<?> tag);

    /*
     * ========== DELETE METHODS (FLUENT API) ==========
     */

    /**
     * Removes a tag by name and returns this compound for chaining.
     *
     * @param name The name of the tag to remove
     * @return This compound for fluent method chaining
     */
    ICompoundTag removeTag(String name);

    /**
     * Removes multiple tags by name and returns this compound for chaining.
     *
     * @param names The names of the tags to remove
     * @return This compound for fluent method chaining
     */
    ICompoundTag removeTags(String... names);

    /**
     * Removes all tags from this compound.
     *
     * @return This compound for fluent method chaining
     */
    ICompoundTag clear();

    /**
     * Gets the number of child tags in this compound.
     *
     * @return The number of child tags
     */
    int size();

    /**
     * Checks if this compound is empty (has no child tags).
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();
}