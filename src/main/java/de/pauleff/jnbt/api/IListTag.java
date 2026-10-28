package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag;

import java.util.ArrayList;

/**
 * An ordered list of tags with one fixed element type. Element names are kept in memory but are not
 * stored in binary NBT.
 *
 * @since 1.3
 */
public interface IListTag extends ITag<ArrayList<Tag<?>>>
{
    /**
     * Returns a shallow copy of the elements. Element objects are shared.
     *
     * @return a copy of the element collection
     */
    @Override
    ArrayList<Tag<?>> getData();

    /**
     * Returns the fixed NBT type ID for list elements.
     *
     * @return the required element type ID (0-12)
     */
    int getListTypeID();

    /**
     * Adds a string element to this list.
     *
     * @param name  the element name
     * @param value the string value to store
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store string tags
     */
    IListTag addString(String name, String value);

    /**
     * Adds an integer element to this list.
     *
     * @param name  the element name
     * @param value the integer value to store
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store integer tags
     */
    IListTag addInt(String name, int value);

    /**
     * Adds a double element to this list.
     *
     * @param name  the element name
     * @param value the double value to store
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store double tags
     */
    IListTag addDouble(String name, double value);

    /**
     * Adds an empty compound element to this list.
     *
     * @param name the element name
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store compound tags
     */
    IListTag addCompound(String name);

    /**
     * Adds a float element to this list.
     *
     * @param name  the element name
     * @param value the float value to store
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store float tags
     */
    IListTag addFloat(String name, float value);

    /**
     * Adds a byte element to this list.
     *
     * @param name  the element name
     * @param value the byte value to store
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store byte tags
     */
    IListTag addByte(String name, byte value);

    /**
     * Adds a short element to this list.
     *
     * @param name  the element name
     * @param value the short value to store
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store short tags
     */
    IListTag addShort(String name, short value);

    /**
     * Adds a long element to this list.
     *
     * @param name  the element name
     * @param value the long value to store
     * @return This list
     * @throws IllegalArgumentException if this list doesn't store long tags
     */
    IListTag addLong(String name, long value);

    /**
     * Updates an element at the index with a new string value.
     *
     * @param index the index to update
     * @param name  the new element name
     * @param value the new string value
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store string tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setString(int index, String name, String value);

    /**
     * Updates an element at the index with a new integer value.
     *
     * @param index the index to update
     * @param name  the new element name
     * @param value the new integer value
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store integer tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setInt(int index, String name, int value);

    /**
     * Updates an element at the index with a new double value.
     *
     * @param index the index to update
     * @param name  the new element name
     * @param value the new double value
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store double tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setDouble(int index, String name, double value);

    /**
     * Updates an element at the index with a new compound.
     *
     * @param index the index to update
     * @param name  the new element name
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store compound tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setCompound(int index, String name);

    /**
     * Updates an element at the index with a new float value.
     *
     * @param index the index to update
     * @param name  the new element name
     * @param value the new float value
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store float tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setFloat(int index, String name, float value);

    /**
     * Updates an element at the index with a new byte value.
     *
     * @param index the index to update
     * @param name  the new element name
     * @param value the new byte value
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store byte tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setByte(int index, String name, byte value);

    /**
     * Updates an element at the index with a new short value.
     *
     * @param index the index to update
     * @param name  the new element name
     * @param value the new short value
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store short tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setShort(int index, String name, short value);

    /**
     * Updates an element at the index with a new long value.
     *
     * @param index the index to update
     * @param name  the new element name
     * @param value the new long value
     * @return This list
     * @throws IllegalArgumentException  if this list doesn't store long tags
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setLong(int index, String name, long value);

    /**
     * Replaces an element at the index with a new tag.
     *
     * @param index the index to update
     * @param tag   the new tag to set
     * @return This list
     * @throws IllegalArgumentException  if tag type doesn't match list type
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag setTag(int index, ITag<?> tag);

    /**
     * Removes an element at the index.
     *
     * @param index the index to remove
     * @return This list
     * @throws IndexOutOfBoundsException if index is invalid
     */
    IListTag removeAt(int index);

    /**
     * Removes the first element that matches the given tag.
     *
     * @param tag the tag to remove
     * @return This list
     */
    IListTag remove(ITag<?> tag);

    /**
     * Removes all elements from this list.
     *
     * @return This list
     */
    IListTag clear();

    /**
     * Gets the element at the index.
     *
     * @param index the index to retrieve
     * @return the element at the index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    ITag<?> get(int index);

    /**
     * Gets the number of elements in this list.
     *
     * @return the number of elements
     */
    int size();

    /**
     * Checks if this list is empty (has no elements).
     *
     * @return {@code true} if empty, {@code false} otherwise
     */
    boolean isEmpty();
}
