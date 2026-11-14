package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.NBTFileFactory;
import de.pauleff.jnbt.api.NBTLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import static org.junit.jupiter.api.Assertions.*;

class CompressionIntegrityTest
{
    @TempDir
    Path tempDir;

    // Encode NBT and compression independently of the library reader/writer.
    private static byte[] root(int payloadLength) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes))
        {
            out.writeByte(10);
            out.writeUTF("");
            out.writeByte(3);
            out.writeUTF("x");
            out.writeInt(42);
            out.writeByte(7);
            out.writeUTF("b");
            out.writeInt(payloadLength);
            byte[] payload = new byte[payloadLength];
            new Random(17).nextBytes(payload);
            out.write(payload);
            out.writeByte(0);
        }
        return bytes.toByteArray();
    }

    private static byte[] encode(byte[] raw, Compression_Types compression) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (OutputStream out = switch (compression)
        {
            case NONE -> bytes;
            case GZIP -> new GZIPOutputStream(bytes);
            case ZLIB -> new DeflaterOutputStream(bytes);
            default -> throw new IllegalArgumentException();
        })
        {
            out.write(raw);
        }
        return bytes.toByteArray();
    }

    private static byte[] concat(byte[] first, byte[] second) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(first);
        bytes.write(second);
        return bytes.toByteArray();
    }

    private File file(byte[] bytes) throws IOException
    {
        return Files.write(tempDir.resolve("input.dat"), bytes).toFile();
    }

    private IOException assertRejected(byte[] bytes) throws IOException
    {
        File input = file(bytes);
        assertFalse(NBTFileFactory.isValidNBTFile(input));
        IOException error = assertThrows(IOException.class, () -> NBTFileFactory.readNBTFile(input));
        assertThrows(IOException.class, () -> NBTFileFactory.createReader(input).read());
        assertArrayEquals(bytes, Files.readAllBytes(input.toPath()));
        assertFalse(Files.exists(tempDir.resolve("input.dat.bak")));
        return error;
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void acceptsCompleteIndependentFixturesAndExactByteLimit(Compression_Types compression) throws IOException
    {
        for (int size : new int[]{0, 8192, 65536})
        {
            byte[] raw = root(size);
            File input = file(encode(raw, compression));
            assertEquals(compression, NBTFileFactory.getCompressionType(input));
            assertTrue(NBTFileFactory.isValidNBTFile(input));
            assertEquals(42, NBTFileFactory.readNBTFile(input).getInt("x"));
            NBTLimits limits = new NBTLimits(512, raw.length, 1_000_000, 100, 100_000, 100);
            assertEquals(42, new NBTReader(input, limits).read().getInt("x"));
            try (DataInputStream in = NBTFileFactory.loadNBTToReader(input))
            {
                assertArrayEquals(raw, in.readAllBytes());
                assertEquals(-1, in.read());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 7, 6, 5, 4, 3, 2, 1})
    void rejectsEachCorruptGzipTrailerByte(int fromEnd) throws IOException
    {
        byte[] bytes = encode(root(0), Compression_Types.GZIP);
        bytes[bytes.length - fromEnd] ^= 1;
        IOException error = assertRejected(bytes);
        assertInstanceOf(ZipException.class, error);
        assertTrue(error.getMessage().contains(fromEnd > 4 ? "CRC" : "ISIZE"), error::getMessage);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
    void rejectsEachMissingGzipTrailerSuffix(int count) throws IOException
    {
        byte[] bytes = encode(root(0), Compression_Types.GZIP);
        IOException error = assertRejected(Arrays.copyOf(bytes, bytes.length - count));
        assertInstanceOf(EOFException.class, error);
        assertTrue(error.getMessage().contains("GZIP trailer"), error::getMessage);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void rejectsCorruptZlibChecksum(int fromEnd) throws IOException
    {
        byte[] bytes = encode(root(0), Compression_Types.ZLIB);
        bytes[bytes.length - fromEnd] ^= 1;
        IOException error = assertRejected(bytes);
        assertTrue(hasCause(error, ZipException.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void rejectsMissingZlibChecksum(int count) throws IOException
    {
        byte[] bytes = encode(root(0), Compression_Types.ZLIB);
        assertRejected(Arrays.copyOf(bytes, bytes.length - count));
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void rejectsTruncatedPayloads(Compression_Types compression) throws IOException
    {
        byte[] bytes = encode(root(8192), compression);
        assertRejected(Arrays.copyOf(bytes, bytes.length / 2));
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void rejectsDataAfterRootInsideCompression(Compression_Types compression) throws IOException
    {
        for (byte[] extra : new byte[][]{{0}, {1, 2, 3}, root(0), new byte[1_000_000]})
        {
            IOException error = assertRejected(encode(concat(root(0), extra), compression));
            assertTrue(error.getMessage().contains("Trailing data after NBT root"), error::getMessage);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"GZIP", "ZLIB"})
    void rejectsPhysicalTrailingBytesAndAdditionalCompressionMembers(Compression_Types compression) throws IOException
    {
        for (int size : new int[]{0, 8192})
        {
            byte[] bytes = encode(root(size), compression);
            for (byte[] extra : new byte[][]{{0}, {1, 2, 3}, encode(root(0), compression),
                    encode(new byte[0], compression), {(byte) 0x1f, (byte) 0x8b}})
            {
                IOException error = assertRejected(concat(bytes, extra));
                assertTrue(error.getMessage().contains("Trailing data after compressed stream"), error::getMessage);
            }
        }
    }

    @Test
    void suppliedStreamParsesOnlyOneRoot() throws IOException
    {
        byte[] raw = root(0);
        ByteArrayInputStream bytes = new ByteArrayInputStream(concat(raw, raw));
        assertEquals(42, NBTFileFactory.createReader(new DataInputStream(bytes)).read().getInt("x"));
        assertEquals(raw.length, bytes.available());
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"GZIP", "ZLIB"})
    void decompressionAdapterValidatesOnlyWhenReadToEnd(Compression_Types compression) throws IOException
    {
        byte[] bytes = encode(root(0), compression);
        File input = file(Arrays.copyOf(bytes, bytes.length - 1));
        try (DataInputStream in = NBTFileFactory.loadNBTToReader(input))
        {
            assertEquals(10, in.readUnsignedByte());
        }
        try (DataInputStream in = NBTFileFactory.loadNBTToReader(input))
        {
            assertThrows(IOException.class, in::readAllBytes);
        }
    }

    @Test
    void acceptsOptionalGzipHeaderFieldsAndValidatesHeaderCrc() throws IOException
    {
        byte[] standard = encode(root(0), Compression_Types.GZIP);
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(new byte[]{0x1f, (byte) 0x8b, 8, 31, 0, 0, 0, 0, 0, (byte) 255});
        header.write(new byte[]{3, 0, 5, 6, 7}); // FEXTRA with little-endian length
        header.write(new byte[]{'n', 'b', 't', 0}); // FNAME
        header.write(new byte[]{'o', 'k', 0}); // FCOMMENT
        CRC32 crc = new CRC32();
        crc.update(header.toByteArray());
        header.write((int) crc.getValue() & 0xff);
        header.write((int) (crc.getValue() >>> 8) & 0xff);
        byte[] bytes = concat(header.toByteArray(), Arrays.copyOfRange(standard, 10, standard.length));
        assertTrue(NBTFileFactory.isValidNBTFile(file(bytes)));
        assertEquals(42, NBTFileFactory.readNBTFile(file(bytes)).getInt("x"));
        bytes[header.size() - 1] ^= 1;
        File corrupt = file(bytes);
        assertFalse(NBTFileFactory.isValidNBTFile(corrupt));
        assertTrue(assertThrows(ZipException.class, () -> NBTFileFactory.loadNBTToReader(corrupt))
                .getMessage().contains("header CRC"));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4, 8, 10, 11})
    void rejectsTruncatedGzipHeaders(int length) throws IOException
    {
        byte[] bytes = encode(root(0), Compression_Types.GZIP);
        bytes[3] = 8; // Require a zero-terminated filename after the fixed header.
        Arrays.fill(bytes, 10, bytes.length, (byte) 'x');
        File input = file(Arrays.copyOf(bytes, length));
        assertFalse(NBTFileFactory.isValidNBTFile(input));
        assertThrows(EOFException.class, () -> NBTFileFactory.loadNBTToReader(input));
    }

    @ParameterizedTest
    @ValueSource(ints = {32, 64, 128})
    void rejectsReservedGzipFlags(int flag) throws IOException
    {
        byte[] bytes = encode(root(0), Compression_Types.GZIP);
        bytes[3] = (byte) flag;
        File input = file(bytes);
        assertFalse(NBTFileFactory.isValidNBTFile(input));
        assertThrows(ZipException.class, () -> NBTFileFactory.loadNBTToReader(input));
    }

    private static boolean hasCause(Throwable error, Class<? extends Throwable> type)
    {
        for (Throwable current = error; current != null; current = current.getCause())
        {
            if (type.isInstance(current)) return true;
        }
        return false;
    }
}
