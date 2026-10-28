package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * An NBT string tag.
 *
 * @since 0.1
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
     * @param name the tag name
     */
    public Tag_String(String name)
    {
        super(NBTTags.Tag_String.getId(), name);
    }

    /**
     * Creates a string tag with name and data.
     *
     * @param name the tag name
     * @param data the non-null initial value
     * @throws IllegalArgumentException if data is null
     */
    public Tag_String(String name, String data)
    {
        super(NBTTags.Tag_String.getId(), name, data);
    }
}
