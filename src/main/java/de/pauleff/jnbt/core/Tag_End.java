package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Structural marker terminating a binary NBT compound.
 *
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
     * Creates an END marker retaining a legacy name in memory.
     * Binary END markers have no name or payload and cannot be collection values.
     *
     * @param name Name of the tag.
     */
    public Tag_End(String name)
    {
        super(NBTTags.Tag_End.getId(), name);
    }

    /**
     * Creates an END marker retaining legacy metadata in memory.
     * Binary END markers have no name or payload and cannot be collection values.
     *
     * @param name Name of the tag.
     * @param data The non-null legacy metadata of the tag.
     * @throws IllegalArgumentException if explicit metadata is null
     */
    public Tag_End(String name, Object data)
    {
        super(NBTTags.Tag_End.getId(), name, data);
    }

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representatin of tag.
     */
    @Override
    public String toString()
    {
        return NBTTags.Tag_End.getName() + "()";
    }
}
