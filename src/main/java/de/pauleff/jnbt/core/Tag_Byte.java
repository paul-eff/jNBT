package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Class representing a NBT byte tag.
 *
 */
public class Tag_Byte extends Tag<Byte>
{
    /**
     * Creates a tag initialized to zero.
     */
    public Tag_Byte()
    {
        super(NBTTags.Tag_Byte.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name Name of the tag.
     */
    public Tag_Byte(String name)
    {
        super(NBTTags.Tag_Byte.getId(), name);
    }

    /**
     * Create a byte tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Byte(String name, Byte data)
    {
        super(NBTTags.Tag_Byte.getId(), name, data);
    }
}
