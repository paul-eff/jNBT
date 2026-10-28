package de.pauleff.jnbt.api;

import java.io.IOException;

/**
 * Reads binary NBT with GZIP, ZLIB, or no compression.
 *
 * @since 1.3
 */
public interface INBTReader extends AutoCloseable
{
    /**
     * Reads the root compound.
     *
     * @return the root {@link ICompoundTag} containing the complete NBT structure
     * @throws IOException if the file cannot be read, is corrupted, or uses an unsupported format
     */
    ICompoundTag read() throws IOException;

    /**
     * Closes the reader.
     *
     * @throws IOException if an error occurs during resource cleanup
     */
    @Override
    void close() throws IOException;
}
