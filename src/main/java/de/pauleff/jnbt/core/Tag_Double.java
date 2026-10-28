package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * An NBT double tag.
 *
 * @since 0.1
 */
public class Tag_Double extends Tag<Double>
{
    /**
     * Creates a tag initialized to zero.
     */
    public Tag_Double()
    {
        super(NBTTags.Tag_Double.getId());
    }

    /**
     * Creates a named tag initialized to zero.
     *
     * @param name the tag name
     */
    public Tag_Double(String name)
    {
        super(NBTTags.Tag_Double.getId(), name);
    }

    /**
     * Creates a double tag with name and data.
     *
     * @param name the tag name
     * @param data the initial value
     */
    public Tag_Double(String name, double data)
    {
        super(NBTTags.Tag_Double.getId(), name, data);
    }
}
