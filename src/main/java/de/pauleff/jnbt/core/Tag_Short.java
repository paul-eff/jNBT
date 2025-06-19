package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Class representing a NBT short tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Short extends Tag<Short>
{
    /**
     * Create an empty short tag.
     */
    public Tag_Short()
    {
        super(NBTTags.Tag_Short.getId());
    }

    /**
     * Create a short tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Short(String name)
    {
        super(NBTTags.Tag_Short.getId(), name);
    }

    /**
     * Create a short tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Short(String name, short data)
    {
        super(NBTTags.Tag_Short.getId(), name, data);
    }
}
