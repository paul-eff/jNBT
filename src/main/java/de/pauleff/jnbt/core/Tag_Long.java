package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Class representing a NBT long tag.
 *
 */
public class Tag_Long extends Tag<Long>
{
    /**
     * Creates a tag initialized to zero.
     */
    public Tag_Long()
    {
        super(NBTTags.Tag_Long.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name Name of the tag.
     */
    public Tag_Long(String name)
    {
        super(NBTTags.Tag_Long.getId(), name);
    }

    /**
     * Create a long tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Long(String name, long data)
    {
        super(NBTTags.Tag_Long.getId(), name, data);
    }
}
