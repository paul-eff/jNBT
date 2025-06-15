package de.pauleff.jnbt.api;

import java.util.function.Consumer;

/**
 * Base interface for all NBT (Named Binary Tag) structures.
 * NBT tags are the fundamental building blocks of Minecraft's data format,
 * each carrying a name, type, and payload according to the NBT specification.
 *
 * @param <T> The {@link java.lang.Object} type this tag stores
 * @author Paul Ferlitz
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
     * In NBT format, every tag has a name except the root tag which may have an empty name.
     *
     * @return The tag's name as {@link java.lang.String}
     */
    String getName();

    /**
     * Updates the name of this tag.
     *
     * @param name The new tag name
     */
    void setName(String name);

    /**
     * Returns the actual data payload stored in this tag.
     *
     * @return The tag's data of type T
     */
    T getData();

    /**
     * Updates the data payload of this tag.
     *
     * @param data The new data to store
     */
    void setData(T data);

    /**
     * Replaces this tag's content with data from another compatible tag.
     * Both tags must be of the same NBT type.
     *
     * @param newTag The source tag to copy from
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