package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Class representing a NBT string tag.
 *
 */
public class Tag_String extends Tag<String>
{
    /**
     * Creates a tag initialized to an empty string.
     */
    public Tag_String()
    {
        super(NBTTags.Tag_String.getId());
    }

    /**
     * Creates a named tag initialized to an empty string.
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
     * @param data The non-null initial data of the tag.
     * @throws IllegalArgumentException if data is null
     */
    public Tag_String(String name, String data)
    {
        super(NBTTags.Tag_String.getId(), name, data);
    }
}
