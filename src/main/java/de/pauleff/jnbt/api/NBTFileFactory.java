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
 * Reads, writes, copies, and updates binary NBT files.
 *
 * @since 1.3
 */
public class NBTFileFactory
{
    /**
     * Creates a reader that automatically detects compression format.
     *
     * @param nbtFile the file to read NBT data from
     * @return new {@link INBTReader} ready to parse the file
     */
    public static INBTReader createReader(File nbtFile)
    {
        return new NBTReader(nbtFile);
    }

    /**
     * Creates a reader from an existing data stream.
     *
     * @param dis the stream containing NBT data
     * @return new {@link INBTReader} ready to parse the stream
     */
    public static INBTReader createReader(DataInputStream dis)
    {
        return new NBTReader(dis);
    }

    /**
     * Creates a writer that preserves the original file's compression format. File must exist to detect
     * compression type.
     *
     * @param nbtFile the file to write NBT data to
     * @return new {@link INBTWriter} ready to write data
     * @throws FileNotFoundException if the target file doesn't exist for compression detection
     */
    public static INBTWriter createWriter(File nbtFile) throws FileNotFoundException
    {
        return new NBTWriter(nbtFile);
    }

    /**
     * Creates a writer with explicit compression settings.
     *
     * @param nbtFile     the file to write NBT data to
     * @param compression the {@link Compression_Types} to apply
     * @return new {@link INBTWriter} ready to write compressed data
     */
    public static INBTWriter createWriter(File nbtFile, Compression_Types compression)
    {
        return new NBTWriter(nbtFile, compression);
    }

    /**
     * Reads an NBT file completely and returns the root compound tag.
     *
     * @param nbtFile the file to read
     * @return the root {@link ICompoundTag} containing all NBT data
     * @throws IOException if the file cannot be read or parsed
     */
    public static ICompoundTag readNBTFile(File nbtFile) throws IOException
    {
        try (INBTReader reader = createReader(nbtFile))
        {
            return reader.read();
        }
    }

    /**
     * Writes a compound to a file. Existing files are backed up. Without an explicit compression type,
     * keeps the existing compression or uses no compression for a new file.
     *
     * @param nbtFile the file to write to
     * @param root    the {@link ICompoundTag} to write
     * @throws IOException if the file cannot be written
     */
    public static void writeNBTFile(File nbtFile, ICompoundTag root) throws IOException
    {
        try (INBTWriter writer = createWriter(nbtFile))
        {
            writer.write(root);
        } catch (FileNotFoundException e)
        {
            try (INBTWriter writer = createWriter(nbtFile, Compression_Types.NONE))
            {
                writer.write(root);
            }
        }
    }

    /**
     * Writes a compound to a file. Existing files are backed up. Without an explicit compression type,
     * keeps the existing compression or uses no compression for a new file.
     *
     * @param nbtFile     the file to write to
     * @param root        the {@link ICompoundTag} to write
     * @param compression the {@link Compression_Types} to use
     * @throws IOException if the file cannot be written
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
     * @param source      the source {@link java.io.File} to copy from
     * @param destination the destination {@link java.io.File} to copy to
     * @throws IOException if the copy operation fails
     */
    public static void copyNBTFile(File source, File destination) throws IOException
    {
        ICompoundTag data = readNBTFile(source);
        Compression_Types compression = NBTFileHandler.getCompressionType(source);
        writeNBTFile(destination, data, compression);
    }

    /**
     * Checks whether the whole file can be read as NBT.
     *
     * @param nbtFile the file to validate
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

    /**
     * Updates a tag using a dot-separated path and keeps the file compression. Each parent path part must
     * be a direct compound child.
     *
     * @param nbtFile the file to update
     * @param tagPath the path to the tag (e.g., "Data.Player.Level")
     * @param newValue the new value to set
     * @param <T> the type of the new value
     * @throws IOException if the file operation fails
     */
    public static <T> void updateTag(File nbtFile, String tagPath, T newValue) throws IOException
    {
        ICompoundTag root = readNBTFile(nbtFile);
        updateTagInCompound(root, tagPath, newValue);
        writeNBTFile(nbtFile, root);
    }

    /**
     * Removes a tag using a dot-separated path and keeps the file compression. Each parent path part must
     * be a direct compound child. A missing final child is ignored.
     *
     * @param nbtFile the file to update
     * @param tagPath the path to the tag to remove (e.g., "Data.Player.OldField")
     * @throws IOException if the file operation fails
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
     * @param original the original {@link java.io.File}
     * @return the backup {@link java.io.File}
     * @throws IOException if the backup operation fails
     */
    public static File createBackup(File original) throws IOException
    {
        File backup = new File(original.getParent(), original.getName() + ".backup");
        copyNBTFile(original, backup);
        return backup;
    }

    /**
     * Merges files and keeps the base file's compression. Matching compound children are merged
     * recursively. Other matching children are replaced.
     *
     * @param baseFile the base {@link java.io.File}
     * @param mergeFile the file to merge into the base
     * @param outputFile the output {@link java.io.File}
     * @throws IOException if the merge operation fails
     */
    public static void mergeNBTFiles(File baseFile, File mergeFile, File outputFile) throws IOException
    {
        ICompoundTag base = readNBTFile(baseFile);
        ICompoundTag merge = readNBTFile(mergeFile);

        mergeCompounds(base, merge);

        Compression_Types compression = NBTFileHandler.getCompressionType(baseFile);
        writeNBTFile(outputFile, base, compression);
    }

    /** Updates a tag value deep within a compound structure using dot notation. */
    private static <T> void updateTagInCompound(ICompoundTag compound, String tagPath, T newValue)
    {
        String[] pathParts = tagPath.split("\\.");
        ICompoundTag current = compound;

        for (int i = 0; i < pathParts.length - 1; i++)
        {
            ICompoundTag next = current.getCompound(pathParts[i]);
            if (next == null)
            {
                throw new IllegalArgumentException("Tag path not found: " + tagPath);
            }
            current = next;
        }

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

    /** Removes a tag deep within a compound structure using dot notation. */
    private static void removeTagFromCompound(ICompoundTag compound, String tagPath)
    {
        String[] pathParts = tagPath.split("\\.");
        ICompoundTag current = compound;

        for (int i = 0; i < pathParts.length - 1; i++)
        {
            ICompoundTag next = current.getCompound(pathParts[i]);
            if (next == null)
            {
                throw new IllegalArgumentException("Tag path not found: " + tagPath);
            }
            current = next;
        }

        String finalTagName = pathParts[pathParts.length - 1];
        current.removeTag(finalTagName);
    }

    /** Merges the contents of the source compound into the target compound. */
    private static void mergeCompounds(ICompoundTag target, ICompoundTag source)
    {
        if (source instanceof Tag_Compound sourceCompound)
        {
            for (Tag<?> sourceTag : sourceCompound.getData())
            {
                if (sourceTag instanceof Tag_Compound && target.hasTag(sourceTag.getName()))
                {
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
                    target.setTag(sourceTag);
                }
            }
        }
    }
}
