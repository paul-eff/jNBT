package de.pauleff.api;

import de.pauleff.formats.binary.Compression_Types;

import java.io.IOException;

/**
 * Interface for reading NBT data from files with automatic format detection.
 * Automatically detects and handles compression (GZIP, ZLIB, or uncompressed) and properly decodes the binary NBT format.
 *
 * @author Paul Ferlitz
 * @see ICompoundTag
 * @see Compression_Types
 */
public interface INBTReader extends AutoCloseable
{
    /**
     * Reads and parses the entire NBT file into a compound tag structure.
     * The method automatically detects compression and handles all the binary format complexities.
     * Since NBT files always have compound tags as roots in Java Edition, this returns an {@link ICompoundTag}.
     *
     * @return The root {@link ICompoundTag} containing the complete NBT structure
     * @throws IOException If the file cannot be read, is corrupted, or uses an unsupported format
     */
    ICompoundTag read() throws IOException;

    /**
     * Closes this reader and releases any associated system resources.
     *
     * @throws IOException If an error occurs during resource cleanup
     */
    @Override
    void close() throws IOException;
}