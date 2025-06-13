package de.paulferlitz.core;

import de.paulferlitz.util.NBTTags;

/**
 * Class representing a NBT int tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Int extends Basic_Tag<Integer>
{
    /**
     * Create an empty int tag.
     */
    public Tag_Int()
    {
        super(NBTTags.Tag_Int.getId());
    }

    /**
     * Create a int tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Int(String name)
    {
        super(NBTTags.Tag_Int.getId(), name);
    }

    /**
     * Create a int tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Int(String name, int data)
    {
        super(NBTTags.Tag_Int.getId(), name, data);
    }
}
