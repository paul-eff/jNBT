package me.paulferlitz.api;

import me.paulferlitz.formats.binary.Compression_Types;

import java.io.IOException;

/**
 * Interface for writing NBT data to files with automatic compression handling.
 * Handles the complexities of NBT file format including compression detection and proper binary encoding.
 *
 * @author Paul Ferlitz
 * @see ICompoundTag
 * @see Compression_Types
 */
public interface INBTWriter extends AutoCloseable
{
    /**
     * Writes the given compound tag as the root of an NBT file.
     * According to NBT specification, only compound tags can serve as file roots in Java Edition.
     * The writer handles compression and proper binary encoding automatically.
     *
     * @param root The {@link ICompoundTag} to write as the file root
     * @throws IOException If an error occurs during file writing
     * @throws IllegalArgumentException If root is null
     */
    void write(ICompoundTag root) throws IOException;

    /**
     * Closes this writer and releases any associated system resources.
     *
     * @throws IOException If an error occurs during resource cleanup
     */
    @Override
    void close() throws IOException;
}