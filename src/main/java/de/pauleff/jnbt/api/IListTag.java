package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag;

import java.util.ArrayList;

/**
 * Interface for NBT list tags - ordered collections of same-type elements.
 * Lists enforce type homogeneity, meaning all elements must be of the same NBT type.
 * They're perfect for storing arrays of data like inventory items, coordinates, or any sequential information.
 *
 * @author Paul Ferlitz
 * @see ITag
 * @see ICompoundTag
 */
public interface IListTag extends ITag<ArrayList<Tag<?>>>
{
    /**
     * Returns the NBT type ID that all elements in this list must have.
     * Once set, this cannot be changed and all added elements must match this type.
     *
     * @return The required element type ID (0-12)
     */
    int getListTypeID();

    /**
     * Adds a string element to this list.
     * Only works if this list stores string tags.
     *
     * @param name  The element name
     * @param value The {@link java.lang.String} value to store
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store string tags
     */
    IListTag addString(String name, String value);

    /**
     * Adds an integer element to this list.
     * Only works if this list stores integer tags.
     *
     * @param name  The element name
     * @param value The {@link java.lang.Integer} value to store
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store integer tags
     */
    IListTag addInt(String name, int value);

    /**
     * Adds a double-precision floating point element to this list.
     * Only works if this list stores double tags.
     *
     * @param name  The element name
     * @param value The {@link java.lang.Double} value to store
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store double tags
     */
    IListTag addDouble(String name, double value);

    /**
     * Adds an empty compound element to this list.
     * Only works if this list stores compound tags.
     *
     * @param name The element name
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store compound tags
     */
    IListTag addCompound(String name);

    /**
     * Adds a float element to this list.
     * Only works if this list stores float tags.
     *
     * @param name  The element name
     * @param value The {@link java.lang.Float} value to store
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store float tags
     */
    IListTag addFloat(String name, float value);

    /**
     * Adds a byte element to this list.
     * Only works if this list stores byte tags.
     *
     * @param name  The element name
     * @param value The {@link java.lang.Byte} value to store
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store byte tags
     */
    IListTag addByte(String name, byte value);

    /**
     * Adds a short element to this list.
     * Only works if this list stores short tags.
     *
     * @param name  The element name
     * @param value The {@link java.lang.Short} value to store
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store short tags
     */
    IListTag addShort(String name, short value);

    /**
     * Adds a long element to this list.
     * Only works if this list stores long tags.
     *
     * @param name  The element name
     * @param value The {@link java.lang.Long} value to store
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store long tags
     */
    IListTag addLong(String name, long value);

    /*
     * ========== UPDATE METHODS (INDEX-BASED) ==========
     */

    /**
     * Updates an element at the specified index with a new string value.
     * Only works if this list stores string tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @param value The new {@link java.lang.String} value
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store string tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setString(int index, String name, String value);

    /**
     * Updates an element at the specified index with a new integer value.
     * Only works if this list stores integer tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @param value The new {@link java.lang.Integer} value
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store integer tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setInt(int index, String name, int value);

    /**
     * Updates an element at the specified index with a new double value.
     * Only works if this list stores double tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @param value The new {@link java.lang.Double} value
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store double tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setDouble(int index, String name, double value);

    /**
     * Updates an element at the specified index with a new compound.
     * Only works if this list stores compound tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store compound tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setCompound(int index, String name);

    /**
     * Updates an element at the specified index with a new float value.
     * Only works if this list stores float tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @param value The new {@link java.lang.Float} value
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store float tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setFloat(int index, String name, float value);

    /**
     * Updates an element at the specified index with a new byte value.
     * Only works if this list stores byte tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @param value The new {@link java.lang.Byte} value
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store byte tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setByte(int index, String name, byte value);

    /**
     * Updates an element at the specified index with a new short value.
     * Only works if this list stores short tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @param value The new {@link java.lang.Short} value
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store short tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setShort(int index, String name, short value);

    /**
     * Updates an element at the specified index with a new long value.
     * Only works if this list stores long tags.
     *
     * @param index The index to update
     * @param name  The new element name
     * @param value The new {@link java.lang.Long} value
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if this list doesn't store long tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setLong(int index, String name, long value);

    /**
     * Replaces an element at the specified index with a new tag.
     *
     * @param index The index to update
     * @param tag   The new tag to set
     * @return This list for fluent method chaining
     * @throws IllegalArgumentException if tag type doesn't match list type
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setTag(int index, ITag<?> tag);

    /*
     * ========== DELETE METHODS ==========
     */

    /**
     * Removes an element at the specified index.
     *
     * @param index The index to remove
     * @return This list for fluent method chaining
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag removeAt(int index);

    /**
     * Removes the first element that matches the given tag.
     *
     * @param tag The tag to remove
     * @return This list for fluent method chaining
     */
    IListTag remove(ITag<?> tag);

    /**
     * Removes all elements from this list.
     *
     * @return This list for fluent method chaining
     */
    IListTag clear();

    /*
     * ========== UTILITY METHODS ==========
     */

    /**
     * Gets the element at the specified index.
     *
     * @param index The index to retrieve
     * @return The element at the specified index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    ITag<?> get(int index);

    /**
     * Gets the number of elements in this list.
     *
     * @return The number of elements
     */
    int size();

    /**
     * Checks if this list is empty (has no elements).
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();
}