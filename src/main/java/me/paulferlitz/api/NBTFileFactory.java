package me.paulferlitz.api;

import me.paulferlitz.io.Compression_Types;
import me.paulferlitz.io.NBTReader;
import me.paulferlitz.io.NBTWriter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Factory for creating NBT file I/O handlers with automatic format detection.
 * Simplifies the process of reading from and writing to NBT files by handling compression detection,
 * stream management, and format validation automatically.
 *
 * <p><strong>Quick start:</strong></p>
 * <pre>{@code
 * // Read an NBT file
 * INBTReader reader = NBTFileFactory.createReader(new File("level.dat"));
 * ICompoundTag levelData = reader.read();
 * reader.close();
 *
 * // Write back with same compression
 * INBTWriter writer = NBTFileFactory.createWriter(new File("level.dat"));
 * writer.write(levelData);
 * }</pre>
 *
 * @author Paul Ferlitz
 * @see INBTReader
 * @see INBTWriter
 * @see Compression_Types
 */
public class NBTFileFactory
{
    /**
     * Creates a reader that automatically detects compression format.
     *
     * @param nbtFile The {@link java.io.File} to read NBT data from
     * @return New {@link INBTReader} ready to parse the file
     */
    public static INBTReader createReader(File nbtFile)
    {
        return new NBTReader(nbtFile);
    }

    /**
     * Creates a reader from an existing data stream.
     *
     * @param dis The {@link java.io.DataInputStream} containing NBT data
     * @return New {@link INBTReader} ready to parse the stream
     */
    public static INBTReader createReader(DataInputStream dis)
    {
        return new NBTReader(dis);
    }

    /**
     * Creates a writer that preserves the original file's compression format.
     * File must exist to detect compression type.
     *
     * @param nbtFile The {@link java.io.File} to write NBT data to
     * @return New {@link INBTWriter} ready to write data
     * @throws FileNotFoundException If the target file doesn't exist for compression detection
     */
    public static INBTWriter createWriter(File nbtFile) throws FileNotFoundException
    {
        return new NBTWriter(nbtFile);
    }

    /**
     * Creates a writer with explicit compression settings.
     * Creates or overwrites the target file with the specified compression.
     *
     * @param nbtFile The {@link java.io.File} to write NBT data to
     * @param compression The {@link Compression_Types} to apply
     * @return New {@link INBTWriter} ready to write compressed data
     */
    public static INBTWriter createWriter(File nbtFile, Compression_Types compression)
    {
        return new NBTWriter(nbtFile, compression);
    }
}