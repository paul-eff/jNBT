package de.pauleff.jnbt.util;

/**
 * NBT type IDs and names.
 *
 * @since 1.0
 */
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

    private static final NBTTags[] ID_LOOKUP = new NBTTags[13];

    static
    {
        for (NBTTags tag : values())
        {
            ID_LOOKUP[tag.id] = tag;
        }
    }

    private final int id;
    private final String name;

    /**
     * Creates an NBT tag type entry.
     *
     * @param id   the NBT type ID (0-12)
     * @param name the tag type name used by jNBT
     */
    NBTTags(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * Looks up an NBT tag type by its numeric ID.
     *
     * @param id the NBT type ID to find (0-12)
     * @return the corresponding {@link NBTTags} enum, or {@code null} if ID is invalid
     */
    public static NBTTags getById(int id)
    {
        return (id >= 0 && id < ID_LOOKUP.length) ? ID_LOOKUP[id] : null;
    }

    /**
     * Returns the NBT type ID.
     *
     * @return the numeric type ID (0-12)
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns the tag type name used by jNBT.
     *
     * @return the tag type name
     */
    public String getName()
    {
        return name;
    }
}
