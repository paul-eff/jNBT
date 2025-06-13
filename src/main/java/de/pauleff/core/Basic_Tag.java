package de.pauleff.core;

public class Basic_Tag<T> extends Tag<T>
{
    /**
     * Create a NBT tag with ID.
     *
     * @param id ID of the tag.
     */
    public Basic_Tag(int id)
    {
        super(id);
    }

    /**
     * Create a NBT tag with ID and name and data set to null.
     *
     * @param id   ID of the tag.
     * @param name Name of the tag.
     */
    public Basic_Tag(int id, String name)
    {
        super(id, name);
    }

    /**
     * Create a NBT tag with ID, name and data.
     *
     * @param id   ID of the tag.
     * @param name Name of the tag. Can be null or empty.
     * @param data The initial data of the tag.
     */
    public Basic_Tag(int id, String name, T data)
    {
        super(id, name, data);
    }
}
