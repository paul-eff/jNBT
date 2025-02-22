package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT long tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Long extends Basic_Tag<Long>
{
    /**
     * Create an empty long tag.
     */
    public Tag_Long()
    {
        super(NBTTags.Tag_Long.getId());
    }

    /**
     * Create a long tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Long(String name)
    {
        super(NBTTags.Tag_Long.getId(), name);
    }

    /**
     * Create a long tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Long(String name, long data)
    {
        super(NBTTags.Tag_Long.getId(), name, data);
    }
}
