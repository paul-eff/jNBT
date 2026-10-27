package de.pauleff.jnbt.formats.binary;

/**
 * Compression types for binary NBT. LZ4 is not implemented.
 *
 * @since 1.0
 */
public enum Compression_Types
{
    NONE(0, "NONE"),
    GZIP(1, "GZIP"),
    ZLIB(2, "ZLIB"),
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
     * @param id              the ID for this compression type
     * @param compressionType the readable compression name
     */
    Compression_Types(int id, String compressionType)
    {
        this.id = id;
        this.compressionType = compressionType;
    }

    /**
     * Looks up a compression type by its ID.
     *
     * @param id the compression type ID to find
     * @return the corresponding {@link Compression_Types} enum, or {@code null} if ID is invalid
     */
    public static Compression_Types getById(int id)
    {
        return (id >= 0 && id < ID_LOOKUP.length) ? ID_LOOKUP[id] : null;
    }

    /**
     * Returns the ID for this compression type.
     *
     * @return the compression type ID
     */
    public int getId()
    {
        return this.id;
    }

    /**
     * Returns the readable name of this compression format.
     *
     * @return the compression type name (e.g., "GZIP", "ZLIB")
     */
    public String getName()
    {
        return this.compressionType;
    }
}
