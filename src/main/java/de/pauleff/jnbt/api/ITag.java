package de.pauleff.jnbt.api;

import java.util.function.Consumer;

/**
 * Base interface for all NBT (Named Binary Tag) structures.
 * NBT tags are the fundamental building blocks of Minecraft's data format,
 * each carrying a name, type, and payload according to the NBT specification.
 *
 * @param <T> The {@link java.lang.Object} type this tag stores

 * @see <a href="https://minecraft.wiki/w/NBT_format">NBT Format Specification</a>
 */
public interface ITag<T>
{
    /**
     * Returns the NBT type identifier for this tag.
     * Each tag type has a unique ID as defined in the NBT specification.
     *
     * @return The NBT type ID (0-12)
     */
    int getId();

    /**
     * Returns the name of this tag.
     * Root names and compound keys may be empty or contain whitespace.
     * List element names are not stored in binary NBT.
     *
     * @return The tag's name as {@link java.lang.String}
     */
    String getName();

    /**
     * Updates the name of this tag.
     *
     * @param name The new tag name
     * @throws IllegalArgumentException if the name duplicates a sibling key in a containing compound
     */
    void setName(String name);

    /**
     * Returns the actual data payload stored in this tag.
     *
     * @return The tag's data of type T; structural END markers without metadata have no payload
     */
    T getData();

    /**
     * Initializes or replaces the payload with a non-null value of the declared NBT type.
     *
     * @param data The new data to store
     * @throws IllegalArgumentException if data is null or incompatible with the declared type
     */
    void setData(T data);

    /**
     * Replaces this tag's content with data from another compatible tag.
     * Both tags must be of the same NBT type, and the source payload must be non-null.
     * Invalid input leaves the destination name and payload unchanged.
     *
     * @param newTag The source tag to copy from
     * @throws IllegalArgumentException if the source, payload, or proposed name is invalid
     */
    void editTag(ITag<?> newTag);

    /**
     * Applies a custom transformation or inspection operation to this tag.
     * Useful for batch operations or complex data processing.
     *
     * @param operation The {@link java.util.function.Consumer} to apply
     */
    void applyOperation(Consumer<ITag<T>> operation);

    /**
     * Returns a human-readable representation of this tag.
     *
     * @return A formatted string showing the tag's name, type, and data
     */
    @Override
    String toString();
}