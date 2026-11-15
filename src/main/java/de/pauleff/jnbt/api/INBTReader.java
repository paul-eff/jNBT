package de.pauleff.jnbt.api;

import java.io.IOException;

/**
 * Reads binary NBT with GZIP, ZLIB, or no compression. Readers take ownership of
 * supplied streams after successful construction and support one read operation.
 *
 * @since 1.3
 */
public interface INBTReader extends AutoCloseable
{
    /**
     * Reads the root compound and closes the reader, including on failure.
     *
     * @return the root {@link ICompoundTag} containing the complete NBT structure
     * @throws IOException if the reader is closed or reading, parsing, or cleanup fails
     */
    ICompoundTag read() throws IOException;

    /**
     * Closes the reader and its input stream.
     *
     * @throws IOException if an error occurs during resource cleanup
     */
    @Override
    void close() throws IOException;
}
