package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT double tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Double extends Tag<Double>
{
    /**
     * Create an empty double tag.
     */
    public Tag_Double()
    {
        super(NBTTags.Tag_Double.getId());
    }

    /**
     * Create a double tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Double(String name)
    {
        super(NBTTags.Tag_Double.getId(), name);
    }

    /**
     * Create a double tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Double(String name, double data)
    {
        super(NBTTags.Tag_Double.getId(), name, data);
    }
}
