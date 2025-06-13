package de.pauleff.core;

import de.pauleff.util.NBTTags;

/**
 * Class representing a NBT float tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Float extends Basic_Tag<Float>
{
    /**
     * Create an empty float tag.
     */
    public Tag_Float()
    {
        super(NBTTags.Tag_Float.getId());
    }

    /**
     * Create a float tag with name and data set to null.
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
