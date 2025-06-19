package de.pauleff.jnbt.core;

import de.pauleff.jnbt.util.NBTTags;

/**
 * Class representing a NBT end tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_End extends Tag<Object>
{
    /**
     * Create an empty end tag.
     */
    public Tag_End()
    {
        super(NBTTags.Tag_End.getId());
    }

    /**
     * Create an end tag with name and data set to null.
     * WARNING: This constructor is here only for future proofing. As per NBT spec this tag should not have a name.
     *
     * @param name Name of the tag.
     */
    public Tag_End(String name)
    {
        super(NBTTags.Tag_End.getId(), name);
    }

    /**
     * Create an end tag with name and data.
     * WARNING: This constructor is here only for future proofing. As per NBT spec this tag should not have a name nor data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_End(String name, Object data)
    {
        super(NBTTags.Tag_Double.getId(), name, data);
    }

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representatin of tag.
     */
    @Override
    public String toString()
    {
        return NBTTags.Tag_End.getName() + "()";
    }
}
