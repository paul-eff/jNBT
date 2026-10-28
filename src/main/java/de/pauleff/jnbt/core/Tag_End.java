package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Marks the end of a binary NBT compound. It cannot be stored as a child tag.
 *
 * @since 0.1
 */
public class Tag_End extends Tag<Object>
{
    /**
     * Create an empty end tag.
     */
    public Tag_End()
    {
        super(NBTTags.Tag_End.getId());
    }

    /**
     * Creates an END marker retaining a legacy name in memory. Binary END markers have no name or payload
     * and cannot be collection values.
     *
     * @param name the tag name
     */
    public Tag_End(String name)
    {
        super(NBTTags.Tag_End.getId(), name);
    }

    /**
     * Creates an END marker retaining legacy metadata in memory. Binary END markers have no name or
     * payload and cannot be collection values.
     *
     * @param name the tag name
     * @param data the non-null legacy metadata of the tag.
     * @throws IllegalArgumentException if explicit metadata is null
     */
    public Tag_End(String name, Object data)
    {
        super(NBTTags.Tag_End.getId(), name, data);
    }

    /**
     * Returns the END marker name.
     *
     * @return the END marker string
     */
    @Override
    public String toString()
    {
        return NBTTags.Tag_End.getName() + "()";
    }
}
