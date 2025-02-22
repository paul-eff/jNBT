package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT byte tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Byte extends Tag<Byte>
{
    /**
     * Create an empty byte tag.
     */
    public Tag_Byte()
    {
        super(NBTTags.Tag_Byte.getId());
    }

    /**
     * Create a byte tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Byte(String name)
    {
        super(NBTTags.Tag_Byte.getId(), name);
    }

    /**
     * Create a byte tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Byte(String name, Byte data)
    {
        super(NBTTags.Tag_Byte.getId(), name, data);
    }
}
