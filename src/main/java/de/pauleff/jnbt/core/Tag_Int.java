package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 *  An NBT int tag.
 *
 * @since 0.1
 */
public class Tag_Int extends Tag<Integer>
{
    /** Creates a tag initialized to zero. */
    public Tag_Int()
    {
        super(NBTTags.Tag_Int.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name the tag name
     */
    public Tag_Int(String name)
    {
        super(NBTTags.Tag_Int.getId(), name);
    }

    /**
     * Creates an int tag with name and data.
     *
     * @param name the tag name
     * @param data the initial value
     */
    public Tag_Int(String name, int data)
    {
        super(NBTTags.Tag_Int.getId(), name, data);
    }
}
