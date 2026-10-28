package de.pauleff.jnbt.core;

import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.util.NBTTags;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for NBT tags. Null names become the string {@code "null"}. Empty names and whitespace are
 * kept. Default payloads are zero, an empty string, or a new empty array or collection. Explicit null
 * payloads are rejected. END markers have no default payload.
 *
 * @param <T> the data type stored within this tag
 * @since 0.1
 */
public abstract class Tag<T> implements ITag<T>
{
    private final int id;
    protected T data;
    private String name;
    // Weak references let shared children validate renames without retaining old trees.
    private ArrayList<WeakReference<Tag_Compound>> compoundParents;

    /**
     * Creates a tag with the NBT type ID and its default payload.
     *
     * @param id the NBT type ID
     * @throws IllegalArgumentException if the ID is not a valid NBT tag type
     */
    public Tag(int id)
    {
        this(id, "null");
    }

    /**
     * Creates a named tag with zero, an empty string, or a new empty array/collection.
     *
     * @param id   the NBT type ID
     * @param name the tag name, with null stored as "null"
     * @throws IllegalArgumentException if the ID is not a valid NBT tag type
     */
    public Tag(int id, String name)
    {
        this.id = id;
        this.name = name == null ? "null" : name;
        this.data = (T) defaultPayload(id);
    }

    /**
     * Creates a tag with the given type, name, and payload.
     *
     * @param id   the NBT type ID
     * @param name the tag name, with null stored as "null"
     * @param data the non-null initial payload
     * @throws IllegalArgumentException if the ID is invalid or data is null or incompatible
     */
    public Tag(int id, String name, T data)
    {
        if (NBTTags.getById(id) == null)
        {
            throw new IllegalArgumentException("Invalid ID: " + id);
        }
        this.id = id;
        this.name = name == null ? "null" : name;
        validatePayload(data);
        this.data = data;
    }

    /**
     * Returns the default payload for an NBT type ID.
     */
    private static Object defaultPayload(int id)
    {
        return switch (id)
        {
            case 0 -> null; // Structural END markers carry no payload.
            case 1 -> (byte) 0;
            case 2 -> (short) 0;
            case 3 -> 0;
            case 4 -> 0L;
            case 5 -> 0.0f;
            case 6 -> 0.0d;
            case 7 -> new byte[0];
            case 8 -> "";
            case 9, 10 -> new ArrayList<>();
            case 11 -> new int[0];
            case 12 -> new long[0];
            default -> throw new IllegalArgumentException("Invalid ID: " + id);
        };
    }

    /**
     * {@inheritDoc}
     */
    public int getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     *
     * @return the tag name, never null
     */
    public String getName()
    {
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @param name the new name, with null stored as "null"
     * @throws IllegalArgumentException {@inheritDoc}
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

    /**
     * Tracks a containing compound so renames can check for duplicate names.
     */
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
     * {@inheritDoc}
     */
    public T getData()
    {
        return data;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invalid input leaves the payload unchanged.</p>
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public void setData(T data)
    {
        validatePayload(data);
        this.data = data;
    }

    /**
     * Rejects null payloads and values that do not match the NBT type ID.
     */
    private void validatePayload(Object value)
    {
        Class<?> expected = switch (id)
        {
            // END accepts legacy metadata in memory but cannot be stored as a value.
            case 0 -> Object.class;
            case 1 -> Byte.class;
            case 2 -> Short.class;
            case 3 -> Integer.class;
            case 4 -> Long.class;
            case 5 -> Float.class;
            case 6 -> Double.class;
            case 7 -> byte[].class;
            case 8 -> String.class;
            case 9, 10 -> ArrayList.class;
            case 11 -> int[].class;
            case 12 -> long[].class;
            default -> throw new IllegalArgumentException("Invalid ID: " + id);
        };
        if (!expected.isInstance(value))
        {
            throw new IllegalArgumentException("Invalid payload for " + Objects.requireNonNull(NBTTags.getById(id), "Invalid NBT type ID").getName()
                    + ": expected " + expected.getTypeName() + ", got "
                    + (value == null ? "null" : value.getClass().getTypeName()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public void editTag(ITag<?> newTag)
    {
        if (newTag == null || this.getId() != newTag.getId())
        {
            throw new IllegalArgumentException("Source must be a non-null tag with type " + this.getId());
        }
        T replacement = (T) newTag.getData();
        validatePayload(replacement);
        validateNameChange(newTag.getName());
        this.setData(replacement);
        this.setName(newTag.getName());
    }

    /**
     * {@inheritDoc}
     */
    public void applyOperation(Consumer<ITag<T>> operation)
    {
        operation.accept(this);
    }

    /**
     * Compares the tag class, type ID, name, and data.
     *
     * @param obj the object to compare against
     * @return true if the tags match
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

    /**
     * Returns a hash of the tag type, name, and data.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return Objects.requireNonNull(NBTTags.getById(getId()), "Invalid NBT type ID").getName() + "(\"" + getName() + "\"): " + getData();
    }
}
