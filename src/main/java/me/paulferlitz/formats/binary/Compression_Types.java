package me.paulferlitz.formats.binary;

/**
 * Compression formats supported for NBT file storage.
 * NBT files can be stored with different compression schemes depending on their usage context.
 * Minecraft primarily uses GZIP compression, though other formats are supported for compatibility.
 *
 * @author Paul Ferlitz
 * @see NBTFileHandler
 */
public enum Compression_Types
{
    /**
     * No compression - raw NBT binary data
     */
    NONE(0, "NONE"),
    /**
     * GZIP compression - most common for Minecraft saves
     */
    GZIP(1, "GZIP"),
    /**
     * ZLIB compression - used by some Minecraft components
     */
    ZLIB(2, "ZLIB"),
    /**
     * LZ4 compression - planned support for high-performance scenarios
     */
    LZ4(3, "LZ4");

    private static final Compression_Types[] ID_LOOKUP = new Compression_Types[4];

    static
    {
        for (Compression_Types type : values())
        {
            ID_LOOKUP[type.id] = type;
        }
    }

    private final int id;
    private final String compressionType;

    /**
     * Creates a compression type entry with its identifier and name.
     *
     * @param id              The numeric identifier for this compression type
     * @param compressionType The human-readable compression name
     */
    Compression_Types(int id, String compressionType)
    {
        this.id = id;
        this.compressionType = compressionType;
    }

    /**
     * Looks up a compression type by its numeric identifier.
     *
     * @param id The compression type ID to find
     * @return The corresponding {@link Compression_Types} enum, or {@code null} if ID is invalid
     */
    public static Compression_Types getById(int id)
    {
        return (id >= 0 && id < ID_LOOKUP.length) ? ID_LOOKUP[id] : null;
    }

    /**
     * Returns the numeric identifier for this compression type.
     *
     * @return The compression type ID
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * Returns the human-readable name of this compression format.
     *
     * @return The compression type name (e.g., "GZIP", "ZLIB")
     */
    public String getName()
    {
        return this.compressionType;
    }
}
