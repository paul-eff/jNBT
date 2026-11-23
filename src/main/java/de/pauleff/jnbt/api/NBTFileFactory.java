package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.formats.binary.NBTReader;
import de.pauleff.jnbt.formats.binary.NBTWriter;
import de.pauleff.jnbt.internal.NBTInput;
import de.pauleff.jnbt.internal.NBTModel;

import java.io.*;
import java.nio.file.Files;

/**
 * Reads, writes, and updates binary NBT files.
 *
 * @since 1.3
 */
public class NBTFileFactory
{
    /** Static factory. */
    private NBTFileFactory()
    {
    }

    /**
     * Opens buffered, decompressed input; caller closes it. Reading to EOF
     * validates one GZIP member or ZLIB stream and rejects trailing bytes.
     * Closing alone does not validate integrity.
     *
     * @param file the source file
     * @return the decompressed input stream
     * @throws IOException if opening, detection, or initialization fails
     */
    public static DataInputStream loadNBTToReader(File file) throws IOException
    {
        return NBTInput.open(new FileInputStream(file));
    }

    /**
     * Detects compression from up to two bytes without validating the body.
     * Empty files and unknown prefixes return {@code NONE}. ZLIB dictionaries are unsupported.
     *
     * @param file the source file
     * @return the detected compression
     * @throws IOException if reading fails or a recognized header is invalid or unsupported
     */
    public static Compression_Types getCompressionType(File file) throws IOException
    {
        try (InputStream input = new FileInputStream(file))
        {
            return NBTInput.detect(input);
        }
    }

    /**
     * Creates a standalone-file reader that automatically detects compression format.
     * Reading requires exactly one root compound and validates the complete file.
     *
     * @param nbtFile the file to read NBT data from
     * @return new {@link INBTReader} ready to parse the file
     * @throws IOException if opening or decompression initialization fails
     */
    public static INBTReader createReader(File nbtFile) throws IOException
    {
        return new NBTReader(nbtFile);
    }

    /**
     * Creates a reader from an existing decompressed data stream. Reads one root compound
     * without checking for trailing data. The stream is closed after reading.
     *
     * @param dis the stream containing NBT data
     * @return new {@link INBTReader} ready to parse the stream
     */
    public static INBTReader createReader(DataInputStream dis)
    {
        return new NBTReader(dis);
    }

    /**
     * Creates a writer with compression detected from an existing file.
     *
     * @param nbtFile the existing destination file
     * @return the file writer
     * @throws IOException if opening or compression detection fails
     * @see NBTWriter#write(ICompoundTag)
     */
    public static INBTWriter createWriter(File nbtFile) throws IOException
    {
        return new NBTWriter(nbtFile);
    }

    /**
     * Creates a file writer with explicit compression.
     *
     * @param nbtFile     the destination file
     * @param compression the output compression
     * @return the file writer
     * @see NBTWriter#write(ICompoundTag)
     */
    public static INBTWriter createWriter(File nbtFile, Compression_Types compression)
    {
        return new NBTWriter(nbtFile, compression);
    }

    /**
     * Reads exactly one root compound and validates the complete standalone file.
     * Trailing bytes, additional roots, and additional compression members are rejected.
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
     * Saves a root compound, preserving existing compression or using no compression
     * for a new file. Retains {@code .bak} on failure and deletes it on success.
     *
     * @param nbtFile the file to write to
     * @param root    the root compound
     * @throws NBTSaveCleanupException if saving succeeded but backup deletion failed
     * @throws IOException if writing fails
     * @see NBTWriter#write(ICompoundTag)
     */
    public static void writeNBTFile(File nbtFile, ICompoundTag root) throws IOException
    {
        try (INBTWriter writer = Files.notExists(nbtFile.toPath())
                ? createWriter(nbtFile, Compression_Types.NONE) : createWriter(nbtFile))
        {
            writer.write(root);
        }
    }

    /**
     * Saves a root compound with explicit compression. Retains {@code .bak} on
     * failure and deletes it on success.
     *
     * @param nbtFile     the file to write to
     * @param root        the root compound
     * @param compression the output compression
     * @throws NBTSaveCleanupException if saving succeeded but backup deletion failed
     * @throws IOException if writing fails
     * @see NBTWriter#write(ICompoundTag)
     */
    public static void writeNBTFile(File nbtFile, ICompoundTag root, Compression_Types compression) throws IOException
    {
        try (INBTWriter writer = createWriter(nbtFile, compression))
        {
            writer.write(root);
        }
    }

    /**
     * Checks whether the whole file can be read as NBT.
     *
     * @param nbtFile the file to validate
     * @return {@code true} if the complete standalone file is valid, {@code false} otherwise
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
     * @param nbtFile  the file to update
     * @param tagPath  the path to the tag (e.g., "Data.Player.Level")
     * @param newValue the new value to set
     * @param <T>      the type of the new value
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
     * Merges files and keeps the base file's compression. Matching compound children are merged
     * recursively. Other matching children are replaced.
     *
     * @param baseFile   the base {@link java.io.File}
     * @param mergeFile  the file to merge into the base
     * @param outputFile the output {@link java.io.File}
     * @throws IOException if the merge operation fails
     */
    public static void mergeNBTFiles(File baseFile, File mergeFile, File outputFile) throws IOException
    {
        ICompoundTag base = readNBTFile(baseFile);
        ICompoundTag merge = readNBTFile(mergeFile);

        mergeCompounds(base, merge);

        Compression_Types compression = getCompressionType(baseFile);
        writeNBTFile(outputFile, base, compression);
    }

    /**
     * Updates a tag value deep within a compound structure using dot notation.
     */
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
        switch (newValue)
        {
            case String s -> current.setString(finalTagName, s);
            case Integer i -> current.setInt(finalTagName, i);
            case Double v -> current.setDouble(finalTagName, v);
            case Float v -> current.setFloat(finalTagName, v);
            case Byte b -> current.setByte(finalTagName, b);
            case Short i -> current.setShort(finalTagName, i);
            case Long l -> current.setLong(finalTagName, l);
            default -> throw new IllegalArgumentException("Unsupported value type: " + newValue.getClass());
        }
    }

    /**
     * Removes a tag deep within a compound structure using dot notation.
     */
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

    /**
     * Merges the contents of the source compound into the target compound.
     */
    private static void mergeCompounds(ICompoundTag target, ICompoundTag source)
    {
        NBTModel.requireCompound(target);
        Tag_Compound sourceCompound = NBTModel.requireCompound(source);
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
