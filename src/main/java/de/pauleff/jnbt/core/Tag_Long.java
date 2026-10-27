package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 *  An NBT long tag.
 *
 * @since 0.1
 */
public class Tag_Long extends Tag<Long>
{
    /** Creates a tag initialized to zero. */
    public Tag_Long()
    {
        super(NBTTags.Tag_Long.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name the tag name
     */
    public Tag_Long(String name)
    {
        super(NBTTags.Tag_Long.getId(), name);
    }

    /**
     * Creates a long tag with name and data.
     *
     * @param name the tag name
     * @param data the initial value
     */
    public Tag_Long(String name, long data)
    {
        super(NBTTags.Tag_Long.getId(), name, data);
    }
}
