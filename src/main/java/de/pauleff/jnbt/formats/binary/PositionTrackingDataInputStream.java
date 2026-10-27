package de.pauleff.jnbt.formats.binary;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *  Wraps a data input stream and counts bytes from completed reads for error messages.
 *
 * @since 1.5
 */
public class PositionTrackingDataInputStream
{
    private final DataInputStream dataStream;
    private long bytesRead;
    private int tagDepth;
    private String currentTagName;
    private int currentTagType;

    /**
     * Creates a position tracking input stream.
     *
     * @param in the input stream to wrap
     */
    public PositionTrackingDataInputStream(InputStream in)
    {
        this.dataStream = (in instanceof DataInputStream) ? (DataInputStream) in : new DataInputStream(in);
        this.bytesRead = 0;
        this.tagDepth = 0;
        this.currentTagName = "";
        this.currentTagType = -1;
    }

    /**
     * Fills the requested byte range and updates the byte count.
     *
     * @param b the destination array
     * @throws IOException if the stream cannot be read
     */
    public void readFully(byte[] b) throws IOException
    {
        dataStream.readFully(b);
        bytesRead += b.length;
    }

    /**
     * Fills the requested byte range and updates the byte count.
     *
     * @param b the destination array
     * @param off the starting index
     * @param len the number of bytes to read
     * @throws IOException if the stream cannot be read
     */
    public void readFully(byte[] b, int off, int len) throws IOException
    {
        dataStream.readFully(b, off, len);
        bytesRead += len;
    }

    /**
     * Reads a byte and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read
     */
    public byte readByte() throws IOException
    {
        byte result = dataStream.readByte();
        bytesRead++;
        return result;
    }

    /**
     * Reads an unsigned byte and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read
     */
    public int readUnsignedByte() throws IOException
    {
        int result = dataStream.readUnsignedByte();
        bytesRead++;
        return result;
    }

    /**
     * Reads a short and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read
     */
    public short readShort() throws IOException
    {
        short result = dataStream.readShort();
        bytesRead += 2;
        return result;
    }

    /**
     * Reads an unsigned short and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read
     */
    public int readUnsignedShort() throws IOException
    {
        int result = dataStream.readUnsignedShort();
        bytesRead += 2;
        return result;
    }

    /**
     * Reads a Java modified UTF-8 string with its unsigned 16-bit byte length.
     *
     * @return the decoded string
     * @throws IOException if the field is truncated or its encoding is malformed
     */
    public String readUTF() throws IOException
    {
        int length = readUnsignedShort();
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
     * @throws IOException if the stream cannot be read
     */
    public int readInt() throws IOException
    {
        int result = dataStream.readInt();
        bytesRead += 4;
        return result;
    }

    /**
     * Reads a long and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read
     */
    public long readLong() throws IOException
    {
        long result = dataStream.readLong();
        bytesRead += 8;
        return result;
    }

    /**
     * Reads a float and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read
     */
    public float readFloat() throws IOException
    {
        float result = dataStream.readFloat();
        bytesRead += 4;
        return result;
    }

    /**
     * Reads a double and updates the byte count.
     *
     * @return the value read
     * @throws IOException if the stream cannot be read
     */
    public double readDouble() throws IOException
    {
        double result = dataStream.readDouble();
        bytesRead += 8;
        return result;
    }

    /**
     * Closes the wrapped input stream.
     *
     * @throws IOException if closing the stream fails
     */
    public void close() throws IOException
    {
        dataStream.close();
    }

    /**
     * Returns the current position in the stream.
     *
     * @return Number of bytes read so far
     */
    public long getBytesRead()
    {
        return bytesRead;
    }

    /**
     * Returns the current nesting depth in the NBT structure.
     *
     * @return Current tag depth (0 = root)
     */
    public int getTagDepth()
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
    public void setCurrentContext(int depth, String tagName, int tagType)
    {
        this.tagDepth = depth;
        this.currentTagName = tagName;
        this.currentTagType = tagType;
    }

    /**
     * Creates a detailed error message with current parsing context.
     *
     * @param baseMessage the base error message
     * @return the message with stream position and tag context
     */
    public String createContextualError(String baseMessage)
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

        if (currentTagType >= 0)
        {
            error.append(String.format(", Type: %d", currentTagType));
        }

        error.append("]");
        return error.toString();
    }
}
