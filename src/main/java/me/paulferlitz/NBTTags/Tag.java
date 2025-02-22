package me.paulferlitz.NBTTags;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Class representing a basic NBT tag with generic data type.
 *
 * @author Paul Ferlitz
 */
public abstract class Tag<T>
{
    private final int id;
    private String name;
    protected T data;

    /**
     * Create a NBT tag with ID.
     *
     * @param id ID of the tag.
     */
    public Tag(int id)
    {
        this(id, "null", null);
    }

    /**
     * Create a NBT tag with ID and name and data set to null.
     *
     * @param id ID of the tag.
     * @param name Name of the tag.
     */
    public Tag(int id, String name)
    {
        this(id, name, null);
    }

    /**
     * Create a NBT tag with ID, name and data.
     *
     * @param id ID of the tag.
     * @param name Name of the tag. Can be null or empty.
     * @param data The initial data of the tag.
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
     * Method returning the tag's ID.
     *
     * @return The ID of this tag.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Method returning the tag's name.
     *
     * @return The name of this tag.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method for setting the tag's name.
     *
     * @param name Name of the tag. Can be null or empty.
     */
    public void setName(String name)
    {
        this.name = (name == null || name.isEmpty()) ? "null" : name;
    }

    /**
     * Method returning the tag's data.
     *
     * @return The data/payload of the tag.
     */
    public T getData()
    {
        return data;
    }

    /**
     * Method for setting the tag's data.
     *
     * @param data The data/payload of the tag.
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
     * Method for editing the tag as a whole.
     *
     * @param newTag The new tag to replace this tag with.
     */
    public void editTag(Tag<?> newTag) {
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
     * Method for applying a custom operation to the tag.
     *
     * @param operation Operation to apply to the tag.
     */
    public void applyOperation(Consumer<Tag<T>> operation)
    {
        operation.accept(this);
    }

    /**
     * Method for checking if the tag is equal to another object.
     *
     * @param obj Object to compare to.
     * @return True if the tag is equal to the object.
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
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representation of tag.
     */
    @Override
    public String toString()
    {
        return NBTTags.getById(getId()).getName() + "(\"" + getName() + "\"): " + getData();
    }
}
