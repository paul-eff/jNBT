package me.paulferlitz.core;

import java.util.Objects;
import java.util.function.Consumer;
import me.paulferlitz.api.ITag;
import me.paulferlitz.util.NBTTags;

/**
 * Foundation class for all NBT tags, providing core functionality for data storage,
 * identification, and manipulation within the NBT hierarchy.
 * 
 * <p>Each tag maintains a unique identifier, optional name, and typed data payload.
 * Tags support fluent operations, equality checking, and custom transformations.</p>
 *
 * @param <T> the data type stored within this tag
 * @author Paul Ferlitz
 */
public abstract class Tag<T> implements ITag<T>
{
    private final int id;
    private String name;
    protected T data;

    /**
     * Creates a new tag with the specified NBT type identifier.
     *
     * @param id the NBT type identifier
     * @throws IllegalArgumentException if the ID is not a valid NBT tag type
     */
    public Tag(int id)
    {
        this(id, "null", null);
    }

    /**
     * Creates a new named tag with the specified NBT type identifier.
     *
     * @param id the NBT type identifier
     * @param name the tag name (null or empty becomes "null")
     * @throws IllegalArgumentException if the ID is not a valid NBT tag type
     */
    public Tag(int id, String name)
    {
        this(id, name, null);
    }

    /**
     * Creates a new tag with complete initialization.
     *
     * @param id the NBT type identifier
     * @param name the tag name (null or empty becomes "null")
     * @param data the initial payload for this tag
     * @throws IllegalArgumentException if the ID is not a valid NBT tag type
     */
    public Tag(int id, String name, T data)
    {
        if (NBTTags.getById(id) == null) {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
        this.id = id;
        this.name = (name == null || name.isEmpty()) ? "null" : name;
        this.data = data;
    }

    /**
     * Returns the NBT type identifier for this tag.
     *
     * @return the NBT type ID
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns the name assigned to this tag.
     *
     * @return the tag name (never null)
     */
    public String getName()
    {
        return name;
    }

    /**
     * Updates the name assigned to this tag.
     *
     * @param name the new tag name (null or empty becomes "null")
     */
    public void setName(String name)
    {
        this.name = (name == null || name.isEmpty()) ? "null" : name;
    }

    /**
     * Returns the data payload stored within this tag.
     *
     * @return the tag's data content
     */
    public T getData()
    {
        return data;
    }

    /**
     * Updates the data payload with type validation.
     *
     * @param data the new data content
     * @throws IllegalArgumentException if the data type doesn't match the existing type
     */
    public void setData(T data)
    {
        if (this.data.getClass() == data.getClass())
        {
            this.data = data;
        }else
        {
            throw new IllegalArgumentException(String.format("Tag type mismatch! Expected %s, got %s", this.data.getClass(), data.getClass()));
        }
    }

    /**
     * Replaces this tag's content with data from another compatible tag.
     *
     * @param newTag the source tag to copy from
     * @throws IllegalArgumentException if tag types don't match
     * @see #setData(Object)
     * @see #setName(String)
     */
    public void editTag(ITag<?> newTag) {
        if (this.getId() == newTag.getId())
        {
            this.setName(newTag.getName());
            this.setData((T) newTag.getData());
        } else
        {
            throw new IllegalArgumentException(String.format("Tag type mismatch! Expected %d, got %d", this.getId(), newTag.getId()));
        }
    }

    /**
     * Applies a custom transformation or operation to this tag.
     * 
     * <p>Enables functional-style tag manipulation and processing.</p>
     *
     * @param operation the operation to execute on this tag
     */
    public void applyOperation(Consumer<ITag<T>> operation)
    {
        operation.accept(this);
    }

    /**
     * Determines equality based on tag ID, name, and data content.
     *
     * @param obj the object to compare against
     * @return true if tags are equivalent in all aspects
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Tag<?> tag = (Tag<?>) obj;
        return id == tag.id &&
                Objects.equals(name, tag.name) &&
                Objects.equals(data, tag.data);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, data);
    }

    /**
     * Generates a human-readable representation showing tag type, name, and data.
     *
     * @return formatted string representation
     */
    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + getData();
    }
}
