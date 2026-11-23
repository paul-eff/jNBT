package de.pauleff.jnbt.internal;

import de.pauleff.jnbt.formats.binary.Compression_Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class NBTInputTest
{
    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void fragmentedNonMarkableSourceReplaysPrefixAndClosesOnce(Compression_Types compression) throws IOException
    {
        byte[] payload = new byte[32768];
        new Random(42).nextBytes(payload);
        payload[0] = 10; // Uncompressed data must not resemble a compression header.
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (OutputStream out = switch (compression)
        {
            case NONE -> bytes;
            case GZIP -> new GZIPOutputStream(bytes);
            case ZLIB -> new DeflaterOutputStream(bytes);
            default -> throw new AssertionError(compression);
        })
        {
            out.write(payload);
        }
        ObservedInput source = new ObservedInput(bytes.toByteArray());
        source.maxChunk = 1;
        assertFalse(source.markSupported());
        try (DataInputStream input = NBTInput.open(source))
        {
            assertArrayEquals(payload, input.readAllBytes());
            assertEquals(-1, input.read());
            input.close();
        }
        assertEquals(bytes.size(), source.position);
        assertEquals(1, source.closes);
    }

    @Test
    void primitiveReadsUseBufferedSourceReads() throws IOException
    {
        byte[] bytes = new byte[32768];
        bytes[0] = 10;
        ObservedInput source = new ObservedInput(bytes);
        try (DataInputStream input = NBTInput.open(source))
        {
            for (byte value : bytes) assertEquals(value, input.readByte());
            assertEquals(-1, input.read());
        }
        assertEquals(0, source.singleReads);
        assertTrue(source.bulkReads < 64, "Primitive reads should be batched at the raw source");
        assertEquals(1, source.closes);
    }

    @Test
    void detectionConsumesOnlyTwoBytesAndLeavesOwnershipWithCaller() throws IOException
    {
        ObservedInput source = new ObservedInput(new byte[]{0x78, (byte) 0x9c, 1, 2, 3});
        assertEquals(Compression_Types.ZLIB, NBTInput.detect(source));
        assertEquals(2, source.position);
        assertEquals(0, source.closes);
        assertEquals(1, source.read());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void detectionReadFailureRetainsExceptionAndSuppressedCloseFailure(int failAt)
    {
        ObservedInput source = new ObservedInput(new byte[]{10, 0, 0, 0});
        source.maxChunk = 1;
        source.failAt = failAt;
        source.readFailure = new IOException("reading header");
        source.closeFailure = new IOException("closing source");
        IOException error = assertThrows(IOException.class, () -> NBTInput.open(source));
        assertSame(source.readFailure, error);
        assertArrayEquals(new Throwable[]{source.closeFailure}, error.getSuppressed());
        assertEquals(1, source.closes);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void malformedHeaderCleanupClosesOnceAndRetainsCloseFailure(boolean gzip)
    {
        ObservedInput source = new ObservedInput(gzip ? new byte[]{0x1f, (byte) 0x8b}
                : new byte[]{0x78, (byte) 0x9d});
        source.closeFailure = new IOException("closing source");
        IOException error = assertThrows(IOException.class, () -> NBTInput.open(source));
        assertArrayEquals(new Throwable[]{source.closeFailure}, error.getSuppressed());
        assertEquals(1, source.closes);
    }

    @Test
    void detectionFailureDoesNotCloseCallerOwnedInput()
    {
        ObservedInput source = new ObservedInput(new byte[]{0x78, (byte) 0x9d});
        assertThrows(IOException.class, () -> NBTInput.detect(source));
        assertEquals(0, source.closes);
    }

    @Test
    void bodyReadFailureRetainsCauseAndClosesOnce()
    {
        ObservedInput source = new ObservedInput(new byte[]{10, 0, 0, 0});
        source.maxChunk = 1;
        source.failAt = 2;
        source.readFailure = new IOException("reading body");
        source.closeFailure = new IOException("closing source");
        IOException error = assertThrows(IOException.class, () -> {
            try (DataInputStream input = NBTInput.open(source))
            {
                input.readAllBytes();
            }
        });
        assertSame(source.readFailure, error);
        assertArrayEquals(new Throwable[]{source.closeFailure}, error.getSuppressed());
        assertEquals(1, source.closes);
    }

    private static final class ObservedInput extends InputStream
    {
        private final byte[] bytes;
        private int position;
        private int maxChunk = Integer.MAX_VALUE;
        private int failAt = Integer.MAX_VALUE;
        private int singleReads;
        private int bulkReads;
        private int closes;
        private IOException readFailure;
        private IOException closeFailure;

        private ObservedInput(byte[] bytes)
        {
            this.bytes = bytes;
        }

        @Override
        public int read() throws IOException
        {
            singleReads++;
            if (position >= failAt) throw readFailure;
            return position < bytes.length ? bytes[position++] & 255 : -1;
        }

        @Override
        public int read(byte[] target, int offset, int length) throws IOException
        {
            bulkReads++;
            if (length == 0) return 0;
            if (position >= failAt) throw readFailure;
            if (position == bytes.length) return -1;
            int count = Math.min(Math.min(length, maxChunk), Math.min(bytes.length, failAt) - position);
            System.arraycopy(bytes, position, target, offset, count);
            position += count;
            return count;
        }

        @Override
        public void close() throws IOException
        {
            closes++;
            if (closeFailure != null) throw closeFailure;
        }
    }
}
