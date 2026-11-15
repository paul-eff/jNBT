package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.*;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.internal.StandaloneCompressedInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.ZipException;

import static org.junit.jupiter.api.Assertions.*;

class StreamErrorTest
{
    @TempDir
    Path tempDir;

    @FunctionalInterface
    private interface InputRead
    {
        void read(PositionTrackingDataInputStream in) throws IOException;
    }

    private static Stream<Arguments> fields()
    {
        return Stream.of(
                Arguments.of("byte", (InputRead) PositionTrackingDataInputStream::readByte, new byte[1]),
                Arguments.of("unsigned byte", (InputRead) PositionTrackingDataInputStream::readUnsignedByte, new byte[1]),
                Arguments.of("short", (InputRead) PositionTrackingDataInputStream::readShort, new byte[2]),
                Arguments.of("unsigned short", (InputRead) PositionTrackingDataInputStream::readUnsignedShort, new byte[2]),
                Arguments.of("int", (InputRead) PositionTrackingDataInputStream::readInt, new byte[4]),
                Arguments.of("long", (InputRead) PositionTrackingDataInputStream::readLong, new byte[8]),
                Arguments.of("float", (InputRead) PositionTrackingDataInputStream::readFloat, new byte[4]),
                Arguments.of("double", (InputRead) PositionTrackingDataInputStream::readDouble, new byte[8]),
                Arguments.of("array", (InputRead) in -> in.readFully(new byte[7]), new byte[7]),
                Arguments.of("array slice", (InputRead) in -> in.readFully(new byte[9], 2, 7), new byte[7]),
                Arguments.of("UTF", (InputRead) PositionTrackingDataInputStream::readUTF,
                        new byte[]{0, 4, 'a', 'b', 'c', 'd'}));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fields")
    void countsPartialFieldsBeforeEof(String name, InputRead operation, byte[] field) throws IOException
    {
        for (int length = 0; length < field.length; length++)
        {
            ObservedInput raw = new ObservedInput(Arrays.copyOf(field, length));
            PositionTrackingDataInputStream in = new PositionTrackingDataInputStream(raw);
            try
            {
                assertThrows(EOFException.class, () -> operation.read(in), name + " at " + length);
                assertEquals(length, in.getBytesRead(), name + " at " + length);
            } finally
            {
                in.close();
            }
        }
        PositionTrackingDataInputStream full = new PositionTrackingDataInputStream(new ObservedInput(field));
        try
        {
            operation.read(full);
            assertEquals(field.length, full.getBytesRead());
        } finally
        {
            full.close();
        }
    }

    // Independent wire fixture: all payload kinds inside a named root compound.
    private static byte[] fixture(int type) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            out.writeUTF("r\u0000");
            out.writeByte(type);
            out.writeUTF("value");
            switch (type)
            {
                case 1 -> out.writeByte(42);
                case 2 -> out.writeShort(42);
                case 3 -> out.writeInt(42);
                case 4 -> out.writeLong(42);
                case 5 -> out.writeFloat(42.5f);
                case 6 -> out.writeDouble(42.5);
                case 7 -> { out.writeInt(3); out.write(new byte[]{1, 2, 3}); }
                case 8 -> out.writeUTF("A\u0000\uD83D\uDE00Z");
                case 9 -> { out.writeByte(3); out.writeInt(2); out.writeInt(1); out.writeInt(2); }
                case 10 -> { out.writeByte(3); out.writeUTF("inner"); out.writeInt(42); out.writeByte(0); }
                case 11 -> { out.writeInt(2); out.writeInt(1); out.writeInt(2); }
                case 12 -> { out.writeInt(2); out.writeLong(1); out.writeLong(2); }
                default -> throw new IllegalArgumentException();
            }
            out.writeByte(0);
        }
        return bytes.toByteArray();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
    void truncationAtEveryWireBoundaryRetainsEofAndActualPosition(int type) throws IOException
    {
        byte[] bytes = fixture(type);
        for (int length = 0; length < bytes.length; length++)
        {
            ObservedInput raw = new ObservedInput(Arrays.copyOf(bytes, length));
            try (NBTReader reader = new NBTReader(new DataInputStream(raw)))
            {
                IOException error = assertThrows(IOException.class, reader::read, "prefix " + length);
                assertTrue(hasCause(error, EOFException.class), error::toString);
                assertTrue(error.getMessage().contains("Position: " + length + " bytes"), error::getMessage);
                assertFalse(error.getMessage().contains("null"), error::getMessage);
            }
            assertEquals(1, raw.closes);
        }
        ObservedInput raw = new ObservedInput(bytes);
        try (INBTReader reader = NBTFileFactory.createReader(new DataInputStream(raw)))
        {
            assertNotNull(reader.read().getTag("value"));
            assertThrows(IOException.class, reader::read);
        }
        assertEquals(1, raw.closes);
        assertEquals(bytes.length, raw.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 127, 255})
    void rejectsNonCompoundRootsBeforeReadingTheirNamesOrPayloads(int type)
    {
        ObservedInput raw = new ObservedInput(new byte[]{(byte) type, 127, (byte) 255});
        IOException error = assertThrows(IOException.class,
                () -> new NBTReader(new DataInputStream(raw)).read());
        assertTrue(error.getMessage().contains("root must be a compound"), error::getMessage);
        assertTrue(error.getMessage().contains("found type " + type), error::getMessage);
        assertEquals(1, raw.position);
        assertEquals(1, raw.closes);
        assertFalse(hasCause(error, ClassCastException.class));
    }

    @Test
    void rejectsUnknownChildTypeWithoutAttemptingANameRead()
    {
        IOException error = assertThrows(IOException.class, () -> new NBTReader(
                NBTReader.byteArrayToDataInputStream(new byte[]{10, 0, 0, (byte) 255})).read());
        assertTrue(error.getMessage().contains("Unknown tag type: 255"), error::getMessage);
        assertTrue(error.getMessage().contains("Position: 4 bytes"), error::getMessage);
    }

    @Test
    void budgetPreflightDoesNotConsumePartialFields() throws IOException
    {
        ObservedInput raw = new ObservedInput(new byte[10]);
        PositionTrackingDataInputStream in = new PositionTrackingDataInputStream(raw, 3);
        try
        {
            assertThrows(IOException.class, in::readInt);
            assertEquals(0, raw.position);
            assertEquals(0, in.getBytesRead());
            in.readShort();
            assertThrows(IOException.class, in::readShort);
            assertEquals(2, raw.position);
            assertEquals(2, in.getBytesRead());
        } finally
        {
            in.close();
        }
    }

    @Test
    void nestedListErrorsIdentifyTheFullElementPath() throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.write(new byte[]{10, 0, 0, 9});
            out.writeUTF("outer");
            out.writeByte(9);
            out.writeInt(2);
            out.writeByte(3); out.writeInt(0); // First inner list is empty.
            out.writeByte(3); out.writeInt(2);
            out.writeInt(42);
            out.writeShort(7); // Truncated second integer of the second inner list.
        }
        IOException error = assertThrows(IOException.class, () -> new NBTReader(
                NBTReader.byteArrayToDataInputStream(bytes.toByteArray())).read());
        assertTrue(error.getMessage().contains("List path: 'outer'[1][1]"), error::getMessage);
        assertTrue(error.getMessage().contains("Depth: 3"), error::getMessage);
        assertTrue(error.getMessage().contains("Type: 3"), error::getMessage);
        assertTrue(error.getMessage().contains("Position: " + bytes.size() + " bytes"), error::getMessage);
    }

    @Test
    void completedListDoesNotLeakItsPathIntoLaterErrors() throws IOException
    {
        byte[] bytes = fixture(9);
        bytes[bytes.length - 1] = 3; // Replace root END with an int tag lacking a name.
        IOException error = assertThrows(IOException.class, () -> new NBTReader(
                NBTReader.byteArrayToDataInputStream(bytes)).read());
        assertFalse(error.getMessage().contains("List path:"), error::getMessage);
        assertFalse(error.getMessage().contains("Tag: 'value'"), error::getMessage);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void readFailureKeepsItsCauseAndSuppressesCloseFailure(boolean budgetFailure) throws IOException
    {
        ObservedInput raw = new ObservedInput(fixture(4));
        IOException reading = new IOException(); // Null messages must remain safe.
        IOException closing = new IOException("closing");
        raw.closeFailure = closing;
        if (!budgetFailure) { raw.readFailure = reading; raw.failAt = 18; }
        NBTLimits limits = budgetFailure ? oneTagLimit() : NBTLimits.DEFAULT;
        try (NBTReader reader = new NBTReader(new DataInputStream(raw), limits))
        {
            IOException error = assertThrows(IOException.class, reader::read);
            assertTrue(hasCause(error, budgetFailure ? IllegalArgumentException.class : IOException.class));
            if (!budgetFailure)
            {
                assertSame(reading, error.getCause());
                assertTrue(error.getMessage().contains("Position: 18 bytes"), error::getMessage);
                assertFalse(error.getMessage().contains("null"), error::getMessage);
            }
            assertArrayEquals(new Throwable[]{closing}, error.getSuppressed());
            assertThrows(IOException.class, reader::read);
        }
        assertEquals(1, raw.closes);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void readerCloseIsAttemptedExactlyOnceEvenWhenItFails(boolean useRead) throws IOException
    {
        ObservedInput raw = new ObservedInput(fixture(1));
        IOException closing = new IOException("closing");
        raw.closeFailure = closing;
        NBTReader reader = new NBTReader(new DataInputStream(raw));
        assertSame(closing, assertThrows(IOException.class, () -> { if (useRead) reader.read(); else reader.close(); }));
        reader.close();
        assertThrows(IOException.class, reader::read);
        assertEquals(1, raw.closes);
        if (!useRead) assertEquals(0, raw.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void writerFailureClosesOnceAndKeepsPrimaryException(int failure)
    {
        ObservedOutput raw = new ObservedOutput();
        IOException closing = new IOException("closing");
        raw.closeFailure = closing;
        if (failure == 0) raw.writeFailure = new IOException("writing");
        Tag_Compound root = new Tag_Compound("");
        root.addInt("x", 42);
        NBTWriter writer = new NBTWriter(new DataOutputStream(raw), failure == 1 ? oneTagLimit() : NBTLimits.DEFAULT);
        Throwable error = failure == 2
                ? assertThrows(IllegalArgumentException.class, () -> writer.write(null))
                : assertThrows(IOException.class, () -> writer.write(root));
        if (failure == 0) assertSame(raw.writeFailure, error);
        if (failure == 1) assertTrue(hasCause(error, IllegalArgumentException.class));
        assertArrayEquals(new Throwable[]{closing}, error.getSuppressed());
        assertDoesNotThrow(writer::close);
        assertThrows(IOException.class, () -> writer.write(root));
        assertEquals(1, raw.closes);
        if (failure != 0) assertEquals(0, raw.bytes.size());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void writerCloseIsAttemptedExactlyOnceEvenWhenItFails(boolean useWrite)
    {
        ObservedOutput raw = new ObservedOutput();
        IOException closing = new IOException("closing");
        raw.closeFailure = closing;
        NBTWriter writer = new NBTWriter(new DataOutputStream(raw));
        assertSame(closing, assertThrows(IOException.class, () -> { if (useWrite) writer.write(new Tag_Compound("")); else writer.close(); }));
        assertDoesNotThrow(writer::close);
        assertThrows(IOException.class, () -> writer.write(new Tag_Compound("")));
        assertEquals(1, raw.closes);
        if (!useWrite) assertEquals(0, raw.bytes.size());
    }

    @Test
    void successfulWriteClosesSuppliedStreamOnce() throws IOException
    {
        ObservedOutput raw = new ObservedOutput();
        try (NBTWriter writer = new NBTWriter(new DataOutputStream(raw)))
        {
            writer.write(new Tag_Compound(""));
            assertThrows(IOException.class, () -> writer.write(new Tag_Compound("")));
        }
        assertArrayEquals(new byte[]{10, 0, 0, 0}, raw.bytes.toByteArray());
        assertEquals(1, raw.closes);
    }

    @Test
    void failedConstructionDoesNotTakeOwnershipOfSuppliedStreams()
    {
        ObservedInput input = new ObservedInput(new byte[0]);
        ObservedOutput output = new ObservedOutput();
        assertThrows(NullPointerException.class, () -> new NBTReader(new DataInputStream(input), null));
        assertThrows(NullPointerException.class, () -> new NBTWriter(new DataOutputStream(output), null));
        assertEquals(0, input.closes);
        assertEquals(0, output.closes);
        assertThrows(NullPointerException.class, () -> new NBTReader((DataInputStream) null));
        assertThrows(NullPointerException.class, () -> new NBTWriter((DataOutputStream) null));
    }

    @Test
    void missingFileErrorsAreCheckedAcrossEntryPoints()
    {
        File missing = tempDir.resolve("missing.nbt").toFile();
        assertThrows(FileNotFoundException.class, () -> new NBTReader(missing));
        assertThrows(FileNotFoundException.class, () -> new NBTReader(missing, NBTLimits.DEFAULT));
        assertThrows(FileNotFoundException.class, () -> NBTFileFactory.createReader(missing));
        assertThrows(FileNotFoundException.class, () -> NBTFileFactory.readNBTFile(missing));
        assertFalse(NBTFileFactory.isValidNBTFile(missing));
    }

    @Test
    void gzipInitializationErrorsRemainCheckedAcrossEntryPoints() throws IOException
    {
        File input = Files.write(tempDir.resolve("broken.nbt"), new byte[]{0x1f, (byte) 0x8b}).toFile();
        assertThrows(EOFException.class, () -> new NBTReader(input));
        assertThrows(EOFException.class, () -> NBTFileFactory.createReader(input));
        assertThrows(EOFException.class, () -> NBTFileFactory.readNBTFile(input));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void gzipInitializationFailureClosesRawInputAndSuppressesCloseErrors(boolean invalidHeader)
    {
        ObservedInput raw = new ObservedInput(invalidHeader ? new byte[]{1, 2} : new byte[0]);
        raw.closeFailure = new IOException("closing");
        IOException error = assertThrows(IOException.class, () -> new StandaloneCompressedInputStream(raw, true));
        if (invalidHeader) assertInstanceOf(ZipException.class, error);
        else assertInstanceOf(EOFException.class, error);
        assertArrayEquals(new Throwable[]{raw.closeFailure}, error.getSuppressed());
        assertEquals(1, raw.closes);
    }

    @Test
    void gzipOutputInitializationFailureClosesRawOutputAndKeepsPrimaryError()
    {
        ObservedOutput raw = new ObservedOutput();
        raw.writeFailure = new IOException("header writing");
        raw.closeFailure = new IOException("closing");
        IOException error = assertThrows(IOException.class,
                () -> NBTFileHandler.wrapOutput(raw, Compression_Types.GZIP));
        assertSame(raw.writeFailure, error);
        assertArrayEquals(new Throwable[]{raw.closeFailure}, error.getSuppressed());
        assertEquals(1, raw.closes);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void compressionAdapterDoesNotRetryFailedClose(boolean gzip) throws IOException
    {
        ObservedInput raw = new ObservedInput(gzip
                ? new byte[]{0x1f, (byte) 0x8b, 8, 0, 0, 0, 0, 0, 0, 0}
                : new byte[0]);
        raw.closeFailure = new IOException("closing");
        StandaloneCompressedInputStream stream = new StandaloneCompressedInputStream(raw, gzip);
        assertSame(raw.closeFailure, assertThrows(IOException.class, stream::close));
        stream.close();
        assertThrows(IOException.class, stream::read);
        assertEquals(1, raw.closes);
    }

    private static NBTLimits oneTagLimit()
    {
        return new NBTLimits(512, 4096, 4096, 1, 100, 100);
    }

    private static boolean hasCause(Throwable error, Class<? extends Throwable> type)
    {
        for (Throwable cause = error; cause != null; cause = cause.getCause())
        {
            if (type.isInstance(cause)) return true;
        }
        return false;
    }

    // Deliberately non-idempotent close probes detect repeated ownership cleanup.
    private static final class ObservedInput extends InputStream
    {
        private final byte[] bytes;
        private int position;
        private int closes;
        private int failAt = -1;
        private IOException readFailure;
        private IOException closeFailure;

        private ObservedInput(byte[] bytes) { this.bytes = bytes; }

        @Override
        public int read() throws IOException
        {
            if (position == failAt) throw readFailure;
            return position < bytes.length ? bytes[position++] & 0xff : -1;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException
        {
            if (length == 0) return 0;
            if (position == failAt) throw readFailure;
            if (position == bytes.length) return -1;
            int count = Math.min(2, Math.min(length, bytes.length - position));
            if (failAt >= 0) count = Math.min(count, failAt - position);
            System.arraycopy(bytes, position, buffer, offset, count);
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

    private static final class ObservedOutput extends OutputStream
    {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private int closes;
        private IOException writeFailure;
        private IOException closeFailure;

        @Override
        public void write(int value) throws IOException
        {
            if (writeFailure != null) throw writeFailure;
            bytes.write(value);
        }

        @Override
        public void close() throws IOException
        {
            closes++;
            if (closeFailure != null) throw closeFailure;
        }
    }
}
