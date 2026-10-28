package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * An NBT short tag.
 *
 * @since 0.1
 */
public class Tag_Short extends Tag<Short>
{
    /**
     * Creates a tag initialized to zero.
     */
    public Tag_Short()
    {
        super(NBTTags.Tag_Short.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name the tag name
     */
    public Tag_Short(String name)
    {
        super(NBTTags.Tag_Short.getId(), name);
    }

    /**
     * Creates a short tag with name and data.
     *
     * @param name the tag name
     * @param data the initial value
     */
    public Tag_Short(String name, short data)
    {
        super(NBTTags.Tag_Short.getId(), name, data);
    }
}
