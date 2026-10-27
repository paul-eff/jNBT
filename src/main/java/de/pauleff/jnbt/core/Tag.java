package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.util.NBTTags;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Foundation class for all NBT tags, providing core functionality for data storage,
 * identification, and manipulation within the NBT hierarchy.
 * <p>Each tag maintains a unique identifier, optional name, and typed data payload.
 * Tags support fluent operations, equality checking, and custom transformations.</p>
 * <p>Explicit names, including empty strings and whitespace, are preserved exactly.
 * For compatibility, null names and constructors without a name use the literal
 * string {@code "null"}.</p>
 *
 * @param <T> the data type stored within this tag

 */
public abstract class Tag<T> implements ITag<T>
{
    private final int id;
    protected T data;
    private String name;
    // Weak references let shared children validate renames without retaining old trees.
    private ArrayList<WeakReference<Tag_Compound>> compoundParents;

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
     * @param id   the NBT type identifier
     * @param name the tag name (null becomes "null"; empty strings are preserved)
     * @throws IllegalArgumentException if the ID is not a valid NBT tag type
     */
    public Tag(int id, String name)
    {
        this(id, name, null);
    }

    /**
     * Creates a new tag with complete initialization.
     *
     * @param id   the NBT type identifier
     * @param name the tag name (null becomes "null"; empty strings are preserved)
     * @param data the initial payload for this tag
     * @throws IllegalArgumentException if the ID is not a valid NBT tag type
     */
    public Tag(int id, String name, T data)
    {
        if (NBTTags.getById(id) == null)
        {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
        this.id = id;
        this.name = name == null ? "null" : name;
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
     * @param name the new tag name (null becomes "null"; empty strings are preserved)
     * @throws IllegalArgumentException if the name duplicates a key in a containing compound
     */
    public void setName(String name)
    {
        validateNameChange(name);
        this.name = name == null ? "null" : name;
    }

    /**
     * Checks a proposed name against every compound currently containing this tag.
     *
     * @param name the proposed name (null becomes "null")
     * @throws IllegalArgumentException if the name would duplicate a sibling key
     */
    protected final void validateNameChange(String name)
    {
        if (compoundParents == null) return;
        String normalized = name == null ? "null" : name;
        compoundParents.removeIf(reference -> reference.get() == null);
        for (WeakReference<Tag_Compound> reference : compoundParents)
        {
            Tag_Compound parent = reference.get();
            if (parent != null) parent.validateChildRename(this, normalized);
        }
    }

    final void registerCompoundParent(Tag_Compound parent)
    {
        if (compoundParents == null) compoundParents = new ArrayList<>();
        compoundParents.removeIf(reference -> reference.get() == null);
        for (WeakReference<Tag_Compound> reference : compoundParents)
        {
            if (reference.get() == parent) return;
        }
        compoundParents.add(new WeakReference<>(parent));
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
        } else
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
    public void editTag(ITag<?> newTag)
    {
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
