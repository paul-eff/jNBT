package de.paulferlitz.core;

import de.paulferlitz.util.NBTTags;

/**
 * Class representing a NBT string tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_String extends Basic_Tag<String>
{
    /**
     * Create an empty string tag.
     */
    public Tag_String()
    {
        super(NBTTags.Tag_String.getId());
    }

    /**
     * Create a string tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_String(String name)
    {
        super(NBTTags.Tag_String.getId(), name);
    }

    /**
     * Create a string tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_String(String name, String data)
    {
        super(NBTTags.Tag_String.getId(), name, data);
    }
}
