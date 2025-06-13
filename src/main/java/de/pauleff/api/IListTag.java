package de.pauleff.api;

import de.pauleff.core.Tag;

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
}