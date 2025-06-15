package de.pauleff.jnbt.util;

/**
 * NBT tag type registry containing all 13 official tag types from the NBT specification.
 * Each enum represents a specific data type with its official ID and name as defined by Minecraft.
 * This enum serves as the authoritative mapping between numeric tag IDs and their corresponding types.
 *
 * @author Paul Ferlitz
 * @see <a href="https://minecraft.wiki/w/NBT_format">NBT Format Specification</a>
 */
public enum NBTTags
{
    /**
     * Marks the end of compound tag contents
     */
    Tag_End(0, "Tag_End"),
    /**
     * 8-bit signed integer
     */
    Tag_Byte(1, "Tag_Byte"),
    /**
     * 16-bit signed integer
     */
    Tag_Short(2, "Tag_Short"),
    /**
     * 32-bit signed integer
     */
    Tag_Int(3, "Tag_Int"),
    /**
     * 64-bit signed integer
     */
    Tag_Long(4, "Tag_Long"),
    /**
     * 32-bit floating point number
     */
    Tag_Float(5, "Tag_Float"),
    /**
     * 64-bit floating point number
     */
    Tag_Double(6, "Tag_Double"),
    /**
     * Array of unsigned bytes
     */
    Tag_Byte_Array(7, "Tag_Byte_Array"),
    /**
     * UTF-8 encoded string
     */
    Tag_String(8, "Tag_String"),
    /**
     * Ordered collection of same-type tags
     */
    Tag_List(9, "Tag_List"),
    /**
     * Named collection of heterogeneous tags
     */
    Tag_Compound(10, "Tag_Compound"),
    /**
     * Array of 32-bit signed integers
     */
    Tag_Int_Array(11, "Tag_Int_Array"),
    /**
     * Array of 64-bit signed integers
     */
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
     * Creates an NBT tag type entry with its official specification values.
     *
     * @param id   The official NBT type ID (0-12)
     * @param name The official tag type name
     */
    NBTTags(int id, String name)
    {
        this.id = id;
        this.name = name;
    }

    /**
     * Looks up an NBT tag type by its numeric ID.
     * Performs fast array-based lookup for efficient tag type resolution.
     *
     * @param id The NBT type ID to find (0-12)
     * @return The corresponding {@link NBTTags} enum, or {@code null} if ID is invalid
     */
    public static NBTTags getById(int id)
    {
        return (id >= 0 && id < ID_LOOKUP.length) ? ID_LOOKUP[id] : null;
    }

    /**
     * Returns the official NBT type ID for this tag type.
     *
     * @return The numeric ID as defined in the NBT specification
     */
    public int getId()
    {
        return id;
    }

    /**
     * Returns the official tag type name.
     *
     * @return The tag name as defined in the NBT specification
     */
    public String getName()
    {
        return name;
    }
}
