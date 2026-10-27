package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Class representing a NBT int tag.
 *
 */
public class Tag_Int extends Tag<Integer>
{
    /**
     * Creates a tag initialized to zero.
     */
    public Tag_Int()
    {
        super(NBTTags.Tag_Int.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name Name of the tag.
     */
    public Tag_Int(String name)
    {
        super(NBTTags.Tag_Int.getId(), name);
    }

    /**
     * Create a int tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Int(String name, int data)
    {
        super(NBTTags.Tag_Int.getId(), name, data);
    }
}
