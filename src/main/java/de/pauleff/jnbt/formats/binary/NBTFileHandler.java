package de.pauleff.jnbt.formats.binary;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Opens binary NBT streams and detects compression from file headers.
 *
 * @since 0.1
 */
public class NBTFileHandler
{
    /**
     * Opens a binary NBT stream with automatic decompression. The caller must close it.
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
            default ->
                    throw new IllegalArgumentException(String.format("The file %s is compressed with an unsupported format!", file.getName()));
        };

        return new DataInputStream(fileStream);
    }

    /**
     * Opens direct file output, copying an existing file to {@code .bak} first.
     * Truncates the destination and retains the backup. The caller must close the
     * stream. Use {@link NBTWriter} for atomic file saves.
     *
     * @param file        the destination file
     * @param compression the output compression
     * @return the output stream
     * @throws IOException if backup creation or opening fails
     * @throws IllegalArgumentException if compression is unsupported
     */
    public static DataOutputStream loadNBTToWriter(File file, Compression_Types compression) throws IOException
    {
        if (Files.exists(file.toPath()))
        {
            File backupFile = new File(file.getPath() + ".bak");
            try
            {
                Files.copy(file.toPath(), backupFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.printf("Created backup of file %s -> %s%n",
                        file.getName(), backupFile.getName());
            } catch (IOException e)
            {
                throw new IOException("Failed to create backup file: " + e.getMessage(), e);
            }
        }

        return openOutput(file.toPath(), compression);
    }

    /**
     * Opens compressed output without creating a backup.
     *
     * @param path the file to open or truncate
     * @param compression the supported output compression
     * @return the output stream, owned by the caller
     * @throws IOException if opening or compression initialization fails
     */
    static DataOutputStream openOutput(Path path, Compression_Types compression) throws IOException
    {
        if (compression != Compression_Types.NONE && compression != Compression_Types.GZIP
                && compression != Compression_Types.ZLIB)
        {
            throw new IllegalArgumentException("Unsupported output compression: " + compression);
        }
        OutputStream raw = new BufferedOutputStream(Files.newOutputStream(path));
        try
        {
            return new DataOutputStream(switch (compression)
            {
                case NONE -> raw;
                case GZIP -> new GZIPOutputStream(raw);
                case ZLIB -> new DeflaterOutputStream(raw);
                default -> throw new IllegalArgumentException("Unsupported output compression: " + compression);
            });
        } catch (IOException | RuntimeException | Error e)
        {
            try
            {
                raw.close();
            } catch (IOException closeFailure)
            {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
    }

    /**
     * Detects compression from the file header. Defaults to NONE if detection fails.
     *
     * @param file the target file.
     * @return the compression type of the file.
     */
    public static Compression_Types getCompressionType(File file)
    {
        if (isGzipped(file)) return Compression_Types.GZIP;
        else if (isZlibed(file)) return Compression_Types.ZLIB;
        else return Compression_Types.NONE;
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
