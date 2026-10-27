package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag;

import java.util.ArrayList;

/**
 * A collection of named child tags. Names must be unique, including after a rename. Add methods reject
 * duplicate names. Set methods replace direct children. Lookups and removals by name only use direct
 * children. getData() returns a shallow copy with shared child objects.
 *
 * @since 1.3
 */
public interface ICompoundTag extends ITag<ArrayList<Tag<?>>>
{
    /**
     * Checks whether this compound contains a tag.
     *
     * @param tagName the name to search for
     * @return {@code true} if a tag with this name exists, {@code false} otherwise
     */
    boolean hasTag(String tagName);

    /**
     * Adds a string tag.
     *
     * @param name  the tag name
     * @param value the string value to store
     * @return This compound
     */
    ICompoundTag addString(String name, String value);

    /**
     * Adds an integer tag.
     *
     * @param name  the tag name
     * @param value the integer value to store
     * @return This compound
     */
    ICompoundTag addInt(String name, int value);

    /**
     * Adds a double tag.
     *
     * @param name  the tag name
     * @param value the double value to store
     * @return This compound
     */
    ICompoundTag addDouble(String name, double value);

    /**
     * Adds a float tag.
     *
     * @param name  the tag name
     * @param value the float value to store
     * @return This compound
     */
    ICompoundTag addFloat(String name, float value);

    /**
     * Adds a byte tag.
     *
     * @param name  the tag name
     * @param value the byte value to store
     * @return This compound
     */
    ICompoundTag addByte(String name, byte value);

    /**
     * Adds a short integer tag.
     *
     * @param name  the tag name
     * @param value the short value to store
     * @return This compound
     */
    ICompoundTag addShort(String name, short value);

    /**
     * Adds a long integer tag.
     *
     * @param name  the tag name
     * @param value the long value to store
     * @return This compound
     */
    ICompoundTag addLong(String name, long value);

    /**
     * Adds a byte array tag.
     *
     * @param name  the tag name
     * @param value the byte array value to store
     * @return This compound
     */
    ICompoundTag addByteArray(String name, byte[] value);

    /**
     * Adds an integer array tag.
     *
     * @param name  the tag name
     * @param value the integer array value to store
     * @return This compound
     */
    ICompoundTag addIntArray(String name, int[] value);

    /**
     * Adds a long array tag.
     *
     * @param name  the tag name
     * @param value the long array value to store
     * @return This compound
     */
    ICompoundTag addLongArray(String name, long[] value);

    /**
     * Returns a child tag by name without requiring casts.
     *
     * @param name the name of the tag to find
     * @return the tag if found, {@code null} otherwise
     */
    ITag<?> getTag(String name);

    /**
     * Returns a string value directly from a child tag.
     *
     * @param name the name of the string tag
     * @return the string value, or {@code null} if tag doesn't exist or isn't a string
     */
    String getString(String name);

    /**
     * Returns an integer value directly from a child tag.
     *
     * @param name the name of the integer tag
     * @return the integer value, or 0 if tag doesn't exist or isn't an integer
     */
    int getInt(String name);

    /**
     * Returns a double value directly from a child tag.
     *
     * @param name the name of the double tag
     * @return the double value, or 0.0 if tag doesn't exist or isn't a double
     */
    double getDouble(String name);

    /**
     * Returns a byte value directly from a child tag.
     *
     * @param name the name of the byte tag
     * @return the byte value, or 0 if tag doesn't exist or isn't a byte
     */
    byte getByte(String name);

    /**
     * Returns a float value directly from a child tag.
     *
     * @param name the name of the float tag
     * @return the float value, or 0.0f if tag doesn't exist or isn't a float
     */
    float getFloat(String name);

    /**
     * Returns a short value directly from a child tag.
     *
     * @param name the name of the short tag
     * @return the short value, or 0 if tag doesn't exist or isn't a short
     */
    short getShort(String name);

    /**
     * Returns a long value directly from a child tag.
     *
     * @param name the name of the long tag
     * @return the long value, or 0L if tag doesn't exist or isn't a long
     */
    long getLong(String name);

    /**
     * Returns a compound tag by name without casting.
     *
     * @param name the name of the compound tag
     * @return the {@link ICompoundTag} if found, {@code null} otherwise
     */
    ICompoundTag getCompound(String name);

    /**
     * Returns a list tag by name without casting.
     *
     * @param name the name of the list tag
     * @return the {@link IListTag} if found, {@code null} otherwise
     */
    IListTag getList(String name);

    /**
     * Returns a byte array value directly from a child tag.
     *
     * @param name the name of the byte array tag
     * @return the byte array value, or {@code null} if tag doesn't exist or isn't a byte array
     */
    byte[] getByteArray(String name);

    /**
     * Returns an integer array value directly from a child tag.
     *
     * @param name the name of the integer array tag
     * @return the integer array value, or {@code null} if tag doesn't exist or isn't an integer array
     */
    int[] getIntArray(String name);

    /**
     * Returns a long array value directly from a child tag.
     *
     * @param name the name of the long array tag
     * @return the long array value, or {@code null} if tag doesn't exist or isn't a long array
     */
    long[] getLongArray(String name);

    /**
     * Updates or creates a string tag.
     *
     * @param name  the tag name
     * @param value the string value to store
     * @return This compound
     */
    ICompoundTag setString(String name, String value);

    /**
     * Updates or creates an integer tag.
     *
     * @param name  the tag name
     * @param value the integer value to store
     * @return This compound
     */
    ICompoundTag setInt(String name, int value);

    /**
     * Updates or creates a double tag.
     *
     * @param name  the tag name
     * @param value the double value to store
     * @return This compound
     */
    ICompoundTag setDouble(String name, double value);

    /**
     * Updates or creates a float tag.
     *
     * @param name  the tag name
     * @param value the float value to store
     * @return This compound
     */
    ICompoundTag setFloat(String name, float value);

    /**
     * Updates or creates a byte tag.
     *
     * @param name  the tag name
     * @param value the byte value to store
     * @return This compound
     */
    ICompoundTag setByte(String name, byte value);

    /**
     * Updates or creates a short integer tag.
     *
     * @param name  the tag name
     * @param value the short value to store
     * @return This compound
     */
    ICompoundTag setShort(String name, short value);

    /**
     * Updates or creates a long integer tag.
     *
     * @param name  the tag name
     * @param value the long value to store
     * @return This compound
     */
    ICompoundTag setLong(String name, long value);

    /**
     * Updates or creates a byte array tag.
     *
     * @param name  the tag name
     * @param value the byte array value to store
     * @return This compound
     */
    ICompoundTag setByteArray(String name, byte[] value);

    /**
     * Updates or creates an integer array tag.
     *
     * @param name  the tag name
     * @param value the integer array value to store
     * @return This compound
     */
    ICompoundTag setIntArray(String name, int[] value);

    /**
     * Updates or creates a long array tag.
     *
     * @param name  the tag name
     * @param value the long array value to store
     * @return This compound
     */
    ICompoundTag setLongArray(String name, long[] value);

    /**
     * Updates or replaces an existing tag with a new one.
     *
     * @param tag the tag to set (replaces any existing tag with the same name)
     * @return This compound
     */
    ICompoundTag setTag(ITag<?> tag);

    /**
     * Removes a tag by name and returns this compound.
     *
     * @param name the name of the tag to remove
     * @return This compound
     */
    ICompoundTag removeTag(String name);

    /**
     * Removes multiple tags by name and returns this compound.
     *
     * @param names the names of the tags to remove
     * @return This compound
     */
    ICompoundTag removeTags(String... names);

    /**
     * Removes all tags from this compound.
     *
     * @return This compound
     */
    ICompoundTag clear();

    /**
     * Gets the number of child tags in this compound.
     *
     * @return the number of child tags
     */
    int size();

    /**
     * Checks if this compound is empty (has no child tags).
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();
}
