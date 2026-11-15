package de.pauleff.jnbt.formats.binary;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Wraps a data input stream and counts consumed bytes, including partial reads for error messages.
 *
 * @since 1.5
 */
class PositionTrackingDataInputStream
{
    private final DataInputStream dataStream;
    private final long maxBytes;
    private long bytesRead;
    private int tagDepth;
    private String currentTagName;
    private int currentTagType;
    private final ArrayList<String> listPath = new ArrayList<>();

    /**
     * Creates a position tracking input stream.
     *
     * @param in the input stream to wrap
     */
    PositionTrackingDataInputStream(InputStream in)
    {
        this(in, Long.MAX_VALUE);
    }

    /**
     * Creates a position tracking stream with a limit on uncompressed bytes read.
     *
     * @param in       the already-decompressed input stream to wrap
     * @param maxBytes the maximum bytes that may be read
     * @throws IllegalArgumentException if maxBytes is not positive
     */
    PositionTrackingDataInputStream(InputStream in, long maxBytes)
    {
        if (maxBytes < 1) throw new IllegalArgumentException("Byte limit must be positive");
        this.maxBytes = maxBytes;
        this.dataStream = new DataInputStream(new FilterInputStream(Objects.requireNonNull(in, "in"))
        {
            @Override
            public int read() throws IOException
            {
                int value = in.read();
                if (value >= 0) bytesRead++;
                return value;
            }

            @Override
            public int read(byte[] bytes, int offset, int length) throws IOException
            {
                int count = in.read(bytes, offset, length);
                if (count > 0) bytesRead += count;
                return count;
            }
        });
        this.bytesRead = 0;
        this.tagDepth = 0;
        this.currentTagName = "";
        this.currentTagType = -1;
    }

    /**
     * Fills the requested byte range and updates the byte count.
     *
     * @param b the destination array
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    void readFully(byte[] b) throws IOException
    {
        checkAvailable(b.length);
        dataStream.readFully(b);
    }

    /**
     * Fills the requested byte range and updates the byte count.
     *
     * @param b   the destination array
     * @param off the starting index
     * @param len the number of bytes to read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    void readFully(byte[] b, int off, int len) throws IOException
    {
        checkAvailable(len);
        dataStream.readFully(b, off, len);
    }

    /**
     * Reads a byte and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    byte readByte() throws IOException
    {
        checkAvailable(1);
        byte result = dataStream.readByte();
        return result;
    }

    /**
     * Reads an unsigned byte and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    int readUnsignedByte() throws IOException
    {
        checkAvailable(1);
        int result = dataStream.readUnsignedByte();
        return result;
    }

    /**
     * Reads a short and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    short readShort() throws IOException
    {
        checkAvailable(2);
        short result = dataStream.readShort();
        return result;
    }

    /**
     * Reads an unsigned short and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    int readUnsignedShort() throws IOException
    {
        checkAvailable(2);
        int result = dataStream.readUnsignedShort();
        return result;
    }

    /**
     * Reads a Java modified UTF-8 string with its unsigned 16-bit byte length.
     *
     * @return the decoded string
     * @throws IOException if the field is truncated, malformed, or exceeds the remaining byte budget
     */
    String readUTF() throws IOException
    {
        return readUTF(readUnsignedShort());
    }

    /**
     * Decodes an already-read UTF length after checking the remaining byte budget.
     *
     * @param length the encoded byte length from the unsigned 16-bit prefix
     * @return the decoded modified UTF-8 string
     * @throws IOException if the field is truncated, malformed, or exceeds the remaining byte budget
     */
    String readUTF(int length) throws IOException
    {
        checkAvailable(length);
        // Retain the length prefix so Java's UTF decoder can validate the field.
        byte[] encoded = new byte[length + 2];
        encoded[0] = (byte) (length >>> 8);
        encoded[1] = (byte) length;
        readFully(encoded, 2, length);
        return new DataInputStream(new ByteArrayInputStream(encoded)).readUTF();
    }

    /**
     * Reads an int and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    int readInt() throws IOException
    {
        checkAvailable(4);
        int result = dataStream.readInt();
        return result;
    }

    /**
     * Reads a long and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    long readLong() throws IOException
    {
        checkAvailable(8);
        long result = dataStream.readLong();
        return result;
    }

    /**
     * Reads a float and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    float readFloat() throws IOException
    {
        checkAvailable(4);
        float result = dataStream.readFloat();
        return result;
    }

    /**
     * Reads a double and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read or the byte limit is exceeded
     */
    double readDouble() throws IOException
    {
        checkAvailable(8);
        double result = dataStream.readDouble();
        return result;
    }

    /**
     * Rejects a declared payload that cannot fit in the remaining byte budget.
     *
     * @param count the payload bytes to check without consuming them
     * @throws IOException if count is negative or exceeds the remaining byte budget
     */
    void checkAvailable(long count) throws IOException
    {
        if (count < 0 || count > maxBytes - bytesRead)
        {
            throw new IOException(createContextualError("NBT byte limit exceeded: " + maxBytes));
        }
    }

    /**
     * Probes EOF after a standalone root, forcing compression trailer validation.
     * The probe permits EOF at the exact byte limit and never drains trailing payloads.
     *
     * @throws IOException if trailing data exists or compression validation fails
     */
    void requireEnd() throws IOException
    {
        if (dataStream.read() != -1)
        {
            throw new IOException(createContextualError("Trailing data after NBT root"));
        }
    }

    /**
     * Closes the wrapped input stream.
     *
     * @throws IOException if closing the stream fails
     */
    void close() throws IOException
    {
        dataStream.close();
    }

    /**
     * Returns the current position in the stream.
     *
     * @return Number of bytes read so far
     */
    long getBytesRead()
    {
        return bytesRead;
    }

    /**
     * Returns the current nesting depth in the NBT structure.
     *
     * @return Current tag depth (0 = root)
     */
    int getTagDepth()
    {
        return tagDepth;
    }

    /**
     * Sets the current tag context for error reporting.
     *
     * @param depth   the current nesting depth
     * @param tagName the tag name
     * @param tagType the NBT type ID
     */
    void setCurrentContext(int depth, String tagName, int tagType)
    {
        this.tagDepth = depth;
        this.currentTagName = tagName;
        this.currentTagType = tagType;
    }

    /**
     * Adds a zero-based list element to the current parse path.
     *
     * @param name the list name, empty for unnamed nested lists
     * @param index the element index
     */
    void enterListElement(String name, int index)
    {
        listPath.add((name.isEmpty() ? "" : "'" + name + "'") + "[" + index + "]");
    }

    /** Removes a successfully parsed list element from the current path. */
    void exitListElement()
    {
        listPath.removeLast();
    }

    /**
     * Creates a detailed error message with current parsing context.
     *
     * @param baseMessage the base error message
     * @return the message with stream position and tag context
     */
    String createContextualError(String baseMessage)
    {
        StringBuilder error = new StringBuilder(baseMessage);
        error.append(String.format(" [Position: %d bytes", bytesRead));

        if (tagDepth > 0)
        {
            error.append(String.format(", Depth: %d", tagDepth));
        }

        if (!currentTagName.isEmpty())
        {
            error.append(String.format(", Tag: '%s'", currentTagName));
        }

        if (!listPath.isEmpty())
        {
            error.append(", List path: ").append(String.join("", listPath));
        }

        if (currentTagType >= 0)
        {
            error.append(String.format(", Type: %d", currentTagType));
        }

        error.append("]");
        return error.toString();
    }
}
