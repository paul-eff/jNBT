package me.paulferlitz.NBTTags;

/**
 * Class representing a NBT int array tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Int_Array extends Basic_Tag<int[]>
{
    /**
     * Create an empty int array tag.
     */
    public Tag_Int_Array()
    {
        super(NBTTags.Tag_Int_Array.getId());
    }

    /**
     * Create an int array tag with name and data set to null.
     *
     * @param name Name of the tag.
     */
    public Tag_Int_Array(String name)
    {
        super(NBTTags.Tag_Int_Array.getId(), name);
    }

    /**
     * Create an int array tag with name and data.
     *
     * @param name Name of the tag.
     * @param data The initial data of the tag.
     */
    public Tag_Int_Array(String name, int[] data)
    {
        super(NBTTags.Tag_Int_Array.getId(), name, data);
    }
}
