package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.formats.binary.NBTReader;
import de.pauleff.jnbt.formats.binary.NBTWriter;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

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
     * Opens a decompressed NBT input stream. The caller must close it.
     *
     * @param file the source file
     * @return the decompressed input stream
     * @throws IOException if opening or decompression initialization fails
     */
    public static DataInputStream loadNBTToReader(File file) throws IOException
    {
        if (Files.notExists(file.toPath()))
        {
            throw new FileNotFoundException(String.format("The file %s doesn't exist!", file.getPath()));
        }

        InputStream fileStream = switch (getCompressionType(file))
        {
            case NONE ->
            {
                System.out.printf("The file %s was uncompressed.%n", file.getName());
                yield new FileInputStream(file);
            }
            case GZIP ->
            {
                System.out.printf("The file %s was compressed with %s, decompressing...%n", file.getName(), Compression_Types.GZIP.getName());
                yield new GZIPInputStream(new FileInputStream(file));
            }
            case ZLIB ->
            {
                System.out.printf("The file %s was compressed with %s, decompressing...%n", file.getName(), Compression_Types.ZLIB.getName());
                yield new InflaterInputStream(new FileInputStream(file));
            }
            default -> throw new IllegalArgumentException("Unsupported compression: " + file.getName());
        };
        return new DataInputStream(fileStream);
    }

    /**
     * Detects compression from the file header. Returns {@code NONE} if detection fails.
     *
     * @param file the source file
     * @return the detected compression
     */
    public static Compression_Types getCompressionType(File file)
    {
        if (isGzipped(file)) return Compression_Types.GZIP;
        if (isZlibed(file)) return Compression_Types.ZLIB;
        return Compression_Types.NONE;
    }

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
     * Creates a writer with compression detected from an existing file.
     *
     * @param nbtFile the existing destination file
     * @return the file writer
     * @throws FileNotFoundException if the destination does not exist
     * @see NBTWriter#write(ICompoundTag)
     */
    public static INBTWriter createWriter(File nbtFile) throws FileNotFoundException
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
        INBTWriter selectedWriter;
        try
        {
            selectedWriter = createWriter(nbtFile);
        } catch (FileNotFoundException e)
        {
            selectedWriter = createWriter(nbtFile, Compression_Types.NONE);
        }
        try (INBTWriter writer = selectedWriter)
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

    /**
     * Checks for a GZIP header. Returns false on read errors.
     *
     * @param file the target file to check
     * @return {@code true} if the file has GZIP magic bytes, {@code false} otherwise
     */
    private static boolean isGzipped(File file)
    {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            if (raf.length() < 2) return false;
            int magic = raf.read() & 0xff | (raf.read() << 8) & 0xff00;
            return magic == GZIPInputStream.GZIP_MAGIC;
        } catch (IOException e)
        {
            System.err.printf("Warning: Could not check GZIP magic for file %s: %s%n",
                    file.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Checks for a ZLIB header. Returns false on read errors.
     *
     * @param file the target file to check
     * @return {@code true} if the file has ZLIB magic bytes, {@code false} otherwise
     */
    private static boolean isZlibed(File file)
    {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            if (raf.length() < 2) return false;

            byte[] header = new byte[2];
            raf.readFully(header);

            // Check zlib header: first two bytes form a 16-bit value that must be divisible by 31
            int headerValue = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);

            if (headerValue % 31 != 0)
            {
                return false;
            }

            // The low four bits hold the compression method. Method 8 is DEFLATE.
            int compressionMethod = header[0] & 0x0F;
            return compressionMethod == 8;
        } catch (IOException e)
        {
            return false;
        }
    }
}
