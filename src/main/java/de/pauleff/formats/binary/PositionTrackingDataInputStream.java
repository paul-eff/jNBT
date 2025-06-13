package de.pauleff.formats.binary;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Enhanced DataInputStream wrapper that tracks read position for better error reporting.
 * Provides detailed context about where in the stream parsing errors occur.
 *
 * @author Paul Ferlitz
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
     * @param in The underlying input stream to wrap
     */
    public PositionTrackingDataInputStream(InputStream in)
    {
        this.dataStream = (in instanceof DataInputStream) ? (DataInputStream) in : new DataInputStream(in);
        this.bytesRead = 0;
        this.tagDepth = 0;
        this.currentTagName = "";
        this.currentTagType = -1;
    }

    public void readFully(byte[] b) throws IOException
    {
        dataStream.readFully(b);
        bytesRead += b.length;
    }

    public void readFully(byte[] b, int off, int len) throws IOException
    {
        dataStream.readFully(b, off, len);
        bytesRead += len;
    }

    public byte readByte() throws IOException
    {
        byte result = dataStream.readByte();
        bytesRead++;
        return result;
    }

    public int readUnsignedByte() throws IOException
    {
        int result = dataStream.readUnsignedByte();
        bytesRead++;
        return result;
    }

    public short readShort() throws IOException
    {
        short result = dataStream.readShort();
        bytesRead += 2;
        return result;
    }

    public int readUnsignedShort() throws IOException
    {
        int result = dataStream.readUnsignedShort();
        bytesRead += 2;
        return result;
    }

    public int readInt() throws IOException
    {
        int result = dataStream.readInt();
        bytesRead += 4;
        return result;
    }

    public long readLong() throws IOException
    {
        long result = dataStream.readLong();
        bytesRead += 8;
        return result;
    }

    public float readFloat() throws IOException
    {
        float result = dataStream.readFloat();
        bytesRead += 4;
        return result;
    }

    public double readDouble() throws IOException
    {
        double result = dataStream.readDouble();
        bytesRead += 8;
        return result;
    }

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
     * @param depth   Current nesting depth
     * @param tagName Name of the tag being parsed
     * @param tagType Type ID of the tag being parsed
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
     * @param baseMessage The base error message
     * @return Enhanced error message with position and context
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