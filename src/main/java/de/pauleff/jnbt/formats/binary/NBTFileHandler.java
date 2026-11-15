package de.pauleff.jnbt.formats.binary;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Opens compressed binary output streams.
 *
 * @since 0.1
 */
class NBTFileHandler
{
    /** Static stream helper. */
    private NBTFileHandler()
    {
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
        return wrapOutput(new BufferedOutputStream(Files.newOutputStream(path)), compression);
    }

    /**
     * Wraps output with compression, taking ownership of the raw stream even on failure.
     *
     * @param raw the raw output
     * @param compression the output compression
     * @return the compressed data output, owned by the caller
     * @throws IOException if compression initialization fails
     */
    static DataOutputStream wrapOutput(OutputStream raw, Compression_Types compression) throws IOException
    {
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
            } catch (IOException | RuntimeException | Error closeFailure)
            {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
    }
}
