package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 *  An NBT byte tag.
 *
 * @since 0.1
 */
public class Tag_Byte extends Tag<Byte>
{
    /** Creates a tag initialized to zero. */
    public Tag_Byte()
    {
        super(NBTTags.Tag_Byte.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name the tag name
     */
    public Tag_Byte(String name)
    {
        super(NBTTags.Tag_Byte.getId(), name);
    }

    /**
     * Creates a byte tag with name and data.
     *
     * @param name the tag name
     * @param data the initial value
     */
    public Tag_Byte(String name, Byte data)
    {
        super(NBTTags.Tag_Byte.getId(), name, data);
    }
}
