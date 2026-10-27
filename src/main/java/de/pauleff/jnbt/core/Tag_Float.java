package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 *  An NBT float tag.
 *
 * @since 0.1
 */
public class Tag_Float extends Tag<Float>
{
    /** Creates a tag initialized to zero. */
    public Tag_Float()
    {
        super(NBTTags.Tag_Float.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name the tag name
     */
    public Tag_Float(String name)
    {
        super(NBTTags.Tag_Float.getId(), name);
    }

    /**
     * Creates a float tag with name and data.
     *
     * @param name the tag name
     * @param data the initial value
     */
    public Tag_Float(String name, float data)
    {
        super(NBTTags.Tag_Float.getId(), name, data);
    }
}
