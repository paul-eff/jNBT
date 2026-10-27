package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Class representing a NBT float tag.
 *
 */
public class Tag_Float extends Tag<Float>
{
    /**
     * Creates a tag initialized to zero.
     */
    public Tag_Float()
    {
        super(NBTTags.Tag_Float.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name Name of the tag.
     */
    public Tag_Float(String name)
    {
        super(NBTTags.Tag_Float.getId(), name);
    }

    /**
     * Create a float tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Float(String name, float data)
    {
        super(NBTTags.Tag_Float.getId(), name, data);
    }
}
