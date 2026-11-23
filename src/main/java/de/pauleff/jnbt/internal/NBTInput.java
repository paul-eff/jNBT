package de.pauleff.jnbt.internal;

import de.pauleff.jnbt.formats.binary.Compression_Types;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;

/**
 * Compression detection and buffered input.
 *
 * @hidden
 */
public final class NBTInput
{
    /** Static input helper. */
    private NBTInput()
    {
    }

    /**
     * Consumes up to two bytes to detect compression; leaves input open.
     *
     * @param input the source, owned by the caller
     * @return GZIP, ZLIB, or NONE for an unrecognized prefix
     * @throws IOException if reading fails or a recognized header is invalid or unsupported
     */
    public static Compression_Types detect(InputStream input) throws IOException
    {
        int first = input.read();
        if (first == -1) return Compression_Types.NONE;
        int second = input.read();
        if (second == -1)
        {
            if (first == 0x1f || (first & 0x0f) == 8)
            {
                throw new EOFException("Truncated compression header");
            }
            return Compression_Types.NONE;
        }
        if (first == 0x1f && second == 0x8b) return Compression_Types.GZIP;
        if ((first & 0x0f) != 8) return Compression_Types.NONE;

        // RFC 1950: DEFLATE windows are 256 bytes through 32 KiB, with FCHECK modulo 31.
        if ((first >>> 4) > 7) throw new ZipException("Invalid ZLIB window size");
        if (((first << 8) | second) % 31 != 0) throw new ZipException("Invalid ZLIB header check");
        if ((second & 0x20) != 0) throw new ZipException("ZLIB preset dictionary is not supported");
        return Compression_Types.ZLIB;
    }

    /**
     * Opens buffered, decompressed input, taking ownership of the source.
     * Closes it on failure; reading to EOF validates compressed data.
     *
     * @param source the raw file input
     * @return the decompressed stream, owned by the caller
     * @throws IOException if detection or initialization fails
     */
    public static DataInputStream open(InputStream source) throws IOException
    {
        BufferedInputStream input = new BufferedInputStream(source);
        Compression_Types compression;
        try
        {
            input.mark(2);
            compression = detect(input);
            input.reset();
        } catch (IOException | RuntimeException | Error failure)
        {
            try
            {
                input.close();
            } catch (IOException | RuntimeException | Error closeFailure)
            {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
        // The compression adapter owns input and closes it if its constructor fails.
        return new DataInputStream(switch (compression)
        {
            case GZIP -> new StandaloneCompressedInputStream(input, true);
            case ZLIB -> new StandaloneCompressedInputStream(input, false);
            case NONE -> input;
            default -> throw new AssertionError(compression);
        });
    }
}
