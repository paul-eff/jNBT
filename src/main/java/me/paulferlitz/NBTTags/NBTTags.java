package me.paulferlitz.NBTTags;

public enum NBTTags
{
    Tag_End(0, "Tag_End"),
    Tag_Byte(1, "Tag_Byte"),
    Tag_Short(2, "Tag_Short"),
    Tag_Int(3, "Tag_Int"),
    Tag_Long(4, "Tag_Long"),
    Tag_Float(5, "Tag_Float"),
    Tag_Double(6, "Tag_Double"),
    Tag_Byte_Array(7, "Tag_Byte_Array"),
    Tag_String(8, "Tag_String"),
    Tag_List(9, "Tag_List"),
    Tag_Compound(10, "Tag_Compound"),
    Tag_Int_Array(11, "Tag_Int_Array"),
    Tag_Long_Array(12, "Tag_Long_Array");

    private final int id;
    private final String name;
    
    private static final NBTTags[] ID_LOOKUP = new NBTTags[13];
    
    static
    {
        for (NBTTags tag : values())
        {
            ID_LOOKUP[tag.id] = tag;
        }
    }

    /**
     * Created a NBT tag.
     *
     * @param id   The tag's ID.
     * @param name The tag's name.
     */
    NBTTags(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * Method to get a tags ID.
     *
     * @return The tag's ID.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Method to get a tags name.
     *
     * @return The tag's name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Method to fetch a tag by supplying an ID.
     *
     * @param id The target tag's ID.
     * @return The tag corresponding to the ID.
     */
    public static NBTTags getById(int id)
    {
        return (id >= 0 && id < ID_LOOKUP.length) ? ID_LOOKUP[id] : null;
    }
}
