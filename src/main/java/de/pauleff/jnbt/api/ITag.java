package de.pauleff.jnbt.api;

import java.util.function.Consumer;

/**
 * An NBT tag with a name, type ID, and payload.
 *
 * @param <T> the object type this tag stores
 * @since 1.3
 */
public interface ITag<T>
{
    /**
     * Returns the NBT type ID for this tag.
     *
     * @return the NBT type ID (0-12)
     */
    int getId();

    /**
     * Returns the tag name. Empty names and whitespace are allowed.
     * Binary NBT does not store list element names.
     *
     * @return the tag's name as string
     */
    String getName();

    /**
     * Updates the name of this tag.
     *
     * @param name the new tag name
     * @throws IllegalArgumentException if the name duplicates a sibling key in a containing compound
     */
    void setName(String name);

    /**
     * Returns the payload. An END marker without metadata has no payload.
     *
     * @return the tag's data of type T. END markers without metadata have no payload
     */
    T getData();

    /**
     * Sets the payload with a non-null value of the declared NBT type.
     *
     * @param data the new data to store
     * @throws IllegalArgumentException if data is null or incompatible with the declared type
     */
    void setData(T data);

    /**
     * Copies the name and payload from a tag of the same type.
     * Invalid input leaves this tag unchanged.
     *
     * @param newTag the source tag to copy from
     * @throws IllegalArgumentException if the source, payload, or proposed name is invalid
     */
    void editTag(ITag<?> newTag);

    /**
     * Calls the given operation with this tag.
     *
     * @param operation the {@link java.util.function.Consumer} to apply
     */
    void applyOperation(Consumer<ITag<T>> operation);

    /**
     * Returns a readable representation of this tag.
     *
     * @return a formatted string showing the tag's name, type, and data
     */
    @Override
    String toString();
}
