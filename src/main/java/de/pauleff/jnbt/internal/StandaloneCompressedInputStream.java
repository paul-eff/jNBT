package de.pauleff.jnbt.internal;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

/**
 * Reads exactly one GZIP member or ZLIB stream and verifies its physical end at EOF.
 * GZIP framing follows RFC 1952; ZLIB framing and Adler-32 are checked by the inflater.
 * Unlike {@code GZIPInputStream}, trailing garbage and concatenated members are rejected.
 *
 * @hidden
 */
public final class StandaloneCompressedInputStream extends InflaterInputStream
{
    private final boolean gzip;
    private final CRC32 crc = new CRC32();
    private boolean verified;
    private int remainingOffset;

    /**
     * Opens a single compressed stream. The caller owns input cleanup if construction fails.
     *
     * @param input the compressed source
     * @param gzip true for GZIP, false for ZLIB
     * @throws IOException if the GZIP header is invalid or truncated
     */
    public StandaloneCompressedInputStream(InputStream input, boolean gzip) throws IOException
    {
        super(input, new Inflater(gzip));
        this.gzip = gzip;
        try
        {
            if (gzip) readGzipHeader();
        } catch (IOException | RuntimeException | Error e)
        {
            inf.end();
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException
    {
        int count = super.read(bytes, offset, length);
        if (count > 0 && gzip) crc.update(bytes, offset, count);
        if (count == -1 && !verified)
        {
            if (!inf.finished()) throw new ZipException("Incomplete compressed stream");
            // The inflater can read ahead into the trailer or trailing data.
            remainingOffset = len - inf.getRemaining();
            if (gzip)
            {
                long expectedCrc = readTrailerInt();
                long expectedSize = readTrailerInt();
                if (expectedCrc != crc.getValue()) throw new ZipException("Corrupt GZIP CRC");
                if (expectedSize != (inf.getBytesWritten() & 0xffffffffL))
                {
                    throw new ZipException("Corrupt GZIP ISIZE");
                }
            }
            if (readRemaining() != -1) throw new ZipException("Trailing data after compressed stream");
            verified = true;
        }
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException
    {
        try
        {
            super.close();
        } finally
        {
            // InflaterInputStream does not end an explicitly supplied inflater.
            inf.end();
        }
    }

    /**
     * Reads the next compressed byte, including any inflater read-ahead.
     *
     * @return an unsigned byte or -1 at EOF
     * @throws IOException if the source cannot be read
     */
    private int readRemaining() throws IOException
    {
        return remainingOffset < len ? buf[remainingOffset++] & 0xff : in.read();
    }

    /**
     * Reads a little-endian unsigned GZIP trailer field.
     *
     * @return the 32-bit value
     * @throws IOException if the trailer is truncated or unreadable
     */
    private long readTrailerInt() throws IOException
    {
        long value = 0;
        for (int shift = 0; shift < 32; shift += 8)
        {
            int next = readRemaining();
            if (next < 0) throw new EOFException("Truncated GZIP trailer");
            value |= (long) next << shift;
        }
        return value;
    }

    /**
     * Reads a header byte while accumulating the optional header checksum.
     *
     * @return the unsigned byte
     * @throws IOException if the header is truncated or unreadable
     */
    private int readHeaderByte() throws IOException
    {
        int value = in.read();
        if (value < 0) throw new EOFException("Truncated GZIP header");
        crc.update(value);
        return value;
    }

    /**
     * Checks the fixed and optional GZIP header fields without allocating header-sized buffers.
     *
     * @throws IOException if the header is invalid, truncated, or unreadable
     */
    private void readGzipHeader() throws IOException
    {
        if (readHeaderByte() != 0x1f || readHeaderByte() != 0x8b || readHeaderByte() != 8)
        {
            throw new ZipException("Invalid GZIP header");
        }
        int flags = readHeaderByte();
        if ((flags & 0xe0) != 0) throw new ZipException("Reserved GZIP header flags");
        for (int i = 0; i < 6; i++) readHeaderByte(); // MTIME, XFL, OS
        if ((flags & 4) != 0)
        {
            int length = readHeaderByte() | readHeaderByte() << 8;
            for (int i = 0; i < length; i++) readHeaderByte();
        }
        if ((flags & 8) != 0) while (readHeaderByte() != 0) { }
        if ((flags & 16) != 0) while (readHeaderByte() != 0) { }
        if ((flags & 2) != 0)
        {
            int expected = (int) crc.getValue() & 0xffff;
            int actual = readHeaderByte() | readHeaderByte() << 8;
            if (actual != expected) throw new ZipException("Corrupt GZIP header CRC");
        }
        crc.reset();
    }
}
