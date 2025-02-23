package me.paulferlitz.IO;

import me.paulferlitz.NBTTags.NBTTags;

public enum Compression_Types
{
    NONE(0, "NONE"),
    GZIP(1, "GZIP"),
    ZLIB(2, "ZLIB"),
    LZ4(3, "LZ4");

    private final int id;
    private final String compressionType;

    /**
     * Created a compression type.
     *
     * @param id   The tag's ID.
     * @param compressionType The tag's name.
     */
    Compression_Types(int id, String compressionType)
    {
        this.id = id;
        this.compressionType = compressionType;
    }

    /**
     * Method to get a compression type's ID.
     *
     * @return The compression type's ID.
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * Method to get the compression type.
     *
     * @return The compression type
     */
    public String getName()
    {
        return this.compressionType;
    }

    /**
     * Method to fetch a compression type by supplying an ID.
     *
     * @param id The target compression type's ID.
     * @return The compression type corresponding to the ID.
     */
    public static Compression_Types getById(int id)
    {
        for (Compression_Types type : Compression_Types.values())
        {
            if (type.getId() == id) return type;
        }
        return null;
    }
}
