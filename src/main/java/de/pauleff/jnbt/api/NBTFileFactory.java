package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.formats.binary.NBTFileHandler;
import de.pauleff.jnbt.formats.binary.NBTReader;
import de.pauleff.jnbt.formats.binary.NBTWriter;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
     * @param nbtFile     The {@link java.io.File} to write NBT data to
     * @param compression The {@link Compression_Types} to apply
     * @return New {@link INBTWriter} ready to write compressed data
     */
    public static INBTWriter createWriter(File nbtFile, Compression_Types compression)
    {
        return new NBTWriter(nbtFile, compression);
    }

    /*
     * ========== CONVENIENCE METHODS FOR COMMON OPERATIONS ==========
     */

    /**
     * Reads an NBT file completely and returns the root compound tag.
     * Automatically handles compression detection and resource cleanup.
     *
     * @param nbtFile The {@link java.io.File} to read
     * @return The root {@link ICompoundTag} containing all NBT data
     * @throws IOException If the file cannot be read or parsed
     */
    public static ICompoundTag readNBTFile(File nbtFile) throws IOException
    {
        try (INBTReader reader = createReader(nbtFile))
        {
            return reader.read();
        }
    }

    /**
     * Writes an NBT compound tag to file with automatic compression detection.
     * If the file exists, preserves its original compression format.
     *
     * @param nbtFile The {@link java.io.File} to write to
     * @param root    The {@link ICompoundTag} to write
     * @throws IOException If the file cannot be written
     */
    public static void writeNBTFile(File nbtFile, ICompoundTag root) throws IOException
    {
        try (INBTWriter writer = createWriter(nbtFile))
        {
            writer.write(root);
        } catch (FileNotFoundException e)
        {
            // If the file doesn't exist, create a new one with default compression = NONE
            try (INBTWriter writer = createWriter(nbtFile, Compression_Types.NONE))
            {
                writer.write(root);
            }
        }
    }

    /**
     * Writes an NBT compound tag to a new file with specified compression.
     *
     * @param nbtFile     The {@link java.io.File} to write to
     * @param root        The {@link ICompoundTag} to write
     * @param compression The {@link Compression_Types} to use
     * @throws IOException If the file cannot be written
     */
    public static void writeNBTFile(File nbtFile, ICompoundTag root, Compression_Types compression) throws IOException
    {
        try (INBTWriter writer = createWriter(nbtFile, compression))
        {
            writer.write(root);
        }
    }

    /**
     * Copies an NBT file to a new location, preserving compression format.
     *
     * @param source      The source {@link java.io.File} to copy from
     * @param destination The destination {@link java.io.File} to copy to
     * @throws IOException If the copy operation fails
     */
    public static void copyNBTFile(File source, File destination) throws IOException
    {
        ICompoundTag data = readNBTFile(source);
        Compression_Types compression = NBTFileHandler.getCompressionType(source);
        writeNBTFile(destination, data, compression);
    }

    /**
     * Checks if a file appears to be a valid NBT file by attempting to read its header.
     *
     * @param nbtFile The {@link java.io.File} to validate
     * @return {@code true} if the file appears to be valid NBT, {@code false} otherwise
     */
    public static boolean isValidNBTFile(File nbtFile)
    {
        try
        {
            readNBTFile(nbtFile);
            return true;
        } catch (Exception e)
        {
            return false;
        }
    }

    /*
     * ========== UPDATE OPERATIONS ==========
     */

    /**
     * Updates a specific tag in an NBT file without loading the entire structure.
     * Performs a read-modify-write operation preserving original compression.
     *
     * @param nbtFile The {@link java.io.File} to update
     * @param tagPath The path to the tag (e.g., "Data.Player.Level")
     * @param newValue The new value to set
     * @param <T> The type of the new value
     * @throws IOException If the file operation fails
     */
    public static <T> void updateTag(File nbtFile, String tagPath, T newValue) throws IOException
    {
        ICompoundTag root = readNBTFile(nbtFile);
        updateTagInCompound(root, tagPath, newValue);
        writeNBTFile(nbtFile, root);
    }

    /**
     * Removes a specific tag from an NBT file.
     * Performs a read-modify-write operation preserving original compression.
     *
     * @param nbtFile The {@link java.io.File} to update
     * @param tagPath The path to the tag to remove (e.g., "Data.Player.OldField")
     * @throws IOException If the file operation fails
     */
    public static void removeTag(File nbtFile, String tagPath) throws IOException
    {
        ICompoundTag root = readNBTFile(nbtFile);
        removeTagFromCompound(root, tagPath);
        writeNBTFile(nbtFile, root);
    }

    /**
     * Creates a backup of an NBT file before modification.
     *
     * @param original The original {@link java.io.File}
     * @return The backup {@link java.io.File}
     * @throws IOException If the backup operation fails
     */
    public static File createBackup(File original) throws IOException
    {
        File backup = new File(original.getParent(), original.getName() + ".backup");
        copyNBTFile(original, backup);
        return backup;
    }

    /**
     * Merges two NBT files, with the second file's data overriding the first.
     *
     * @param baseFile The base {@link java.io.File}
     * @param mergeFile The file to merge into the base
     * @param outputFile The output {@link java.io.File}
     * @throws IOException If the merge operation fails
     */
    public static void mergeNBTFiles(File baseFile, File mergeFile, File outputFile) throws IOException
    {
        ICompoundTag base = readNBTFile(baseFile);
        ICompoundTag merge = readNBTFile(mergeFile);
        
        mergeCompounds(base, merge);
        
        Compression_Types compression = NBTFileHandler.getCompressionType(baseFile);
        writeNBTFile(outputFile, base, compression);
    }

    /*
     * ========== HELPER METHODS ==========
     */

    /**
     * Updates a tag value deep within a compound structure using dot notation.
     */
    private static <T> void updateTagInCompound(ICompoundTag compound, String tagPath, T newValue)
    {
        String[] pathParts = tagPath.split("\\.");
        ICompoundTag current = compound;
        
        // Navigate to the parent compound
        for (int i = 0; i < pathParts.length - 1; i++)
        {
            ICompoundTag next = current.getCompound(pathParts[i]);
            if (next == null)
            {
                throw new IllegalArgumentException("Tag path not found: " + tagPath);
            }
            current = next;
        }
        
        // Update the final tag
        String finalTagName = pathParts[pathParts.length - 1];
        if (newValue instanceof String)
        {
            current.setString(finalTagName, (String) newValue);
        } else if (newValue instanceof Integer)
        {
            current.setInt(finalTagName, (Integer) newValue);
        } else if (newValue instanceof Double)
        {
            current.setDouble(finalTagName, (Double) newValue);
        } else if (newValue instanceof Float)
        {
            current.setFloat(finalTagName, (Float) newValue);
        } else if (newValue instanceof Byte)
        {
            current.setByte(finalTagName, (Byte) newValue);
        } else if (newValue instanceof Short)
        {
            current.setShort(finalTagName, (Short) newValue);
        } else if (newValue instanceof Long)
        {
            current.setLong(finalTagName, (Long) newValue);
        } else
        {
            throw new IllegalArgumentException("Unsupported value type: " + newValue.getClass());
        }
    }

    /**
     * Removes a tag deep within a compound structure using dot notation.
     */
    private static void removeTagFromCompound(ICompoundTag compound, String tagPath)
    {
        String[] pathParts = tagPath.split("\\.");
        ICompoundTag current = compound;
        
        // Navigate to the parent compound
        for (int i = 0; i < pathParts.length - 1; i++)
        {
            ICompoundTag next = current.getCompound(pathParts[i]);
            if (next == null)
            {
                throw new IllegalArgumentException("Tag path not found: " + tagPath);
            }
            current = next;
        }
        
        // Remove the final tag
        String finalTagName = pathParts[pathParts.length - 1];
        current.removeTag(finalTagName);
    }

    /**
     * Merges the contents of the source compound into the target compound.
     * Source values override target values for matching keys.
     */
    private static void mergeCompounds(ICompoundTag target, ICompoundTag source)
    {
        if (source instanceof Tag_Compound sourceCompound)
        {
            for (Tag<?> sourceTag : sourceCompound.getData())
            {
                if (sourceTag instanceof Tag_Compound && target.hasTag(sourceTag.getName()))
                {
                    // Recursively merge nested compounds
                    ICompoundTag targetNested = target.getCompound(sourceTag.getName());
                    if (targetNested != null)
                    {
                        mergeCompounds(targetNested, (ICompoundTag) sourceTag);
                    } else
                    {
                        target.setTag(sourceTag);
                    }
                } else
                {
                    // Replace or add the tag
                    target.setTag(sourceTag);
                }
            }
        }
    }
}