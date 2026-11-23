package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.formats.binary.NBTWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import static org.junit.jupiter.api.Assertions.*;

class CompressionDetectionTest
{
    @TempDir
    Path directory;

    // An empty compound and a stored DEFLATE block, independently encoded.
    private static final byte[] ROOT = {10, 0, 0, 0};

    private File file(byte[] bytes) throws IOException
    {
        return Files.write(directory.resolve("input.nbt"), bytes).toFile();
    }

    private static byte[] zlib(int window, int flags) throws IOException
    {
        int cmf = window << 4 | 8;
        int flg = flags | (31 - ((cmf << 8 | flags) % 31)) % 31;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write(cmf);
        bytes.write(flg);
        bytes.write(new byte[]{1, 4, 0, (byte) 251, (byte) 255, 10, 0, 0, 0,
                0, 44, 0, 11}); // Adler-32 of ROOT.
        return bytes.toByteArray();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void acceptsAllZlibWindowSizesAndLevelFlags(int window) throws IOException
    {
        for (int level = 0; level < 4; level++)
        {
            File input = file(zlib(window, level << 6));
            assertEquals(Compression_Types.ZLIB, NBTFileFactory.getCompressionType(input));
            try (DataInputStream stream = NBTFileFactory.loadNBTToReader(input))
            {
                assertArrayEquals(ROOT, stream.readAllBytes());
            }
            assertTrue(NBTFileFactory.readNBTFile(input).isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9, 10, 11, 12, 13, 14, 15})
    void rejectsInvalidZlibWindowsBeforeReadingOrSaving(int window) throws IOException
    {
        assertRejectedBeforeSave(zlib(window, 0), "window");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void rejectsPresetDictionariesExplicitly(int level) throws IOException
    {
        // Valid FCHECK and FDICT, followed by a dictionary ID.
        byte[] bytes = zlib(7, level << 6 | 32);
        assertRejectedBeforeSave(bytes, "dictionary");
    }

    @Test
    void rejectsInvalidZlibHeaderCheck() throws IOException
    {
        byte[] bytes = zlib(7, 0);
        bytes[1] ^= 1;
        assertRejectedBeforeSave(bytes, "header check");
    }

    private void assertRejectedBeforeSave(byte[] bytes, String message) throws IOException
    {
        File input = file(bytes);
        Path backup = directory.resolve("input.nbt.bak");
        byte[] oldBackup = {9, 8, 7};
        Files.write(backup, oldBackup);
        ZipException error = assertThrows(ZipException.class, () -> NBTFileFactory.getCompressionType(input));
        assertTrue(error.getMessage().contains(message), error::getMessage);
        assertThrows(ZipException.class, () -> NBTFileFactory.loadNBTToReader(input));
        assertThrows(ZipException.class, () -> NBTFileFactory.readNBTFile(input));
        assertThrows(ZipException.class, () -> NBTFileFactory.createWriter(input));
        assertThrows(ZipException.class, () -> new NBTWriter(input, NBTLimits.DEFAULT));
        assertThrows(ZipException.class, () -> NBTFileFactory.writeNBTFile(input, new Tag_Compound("")));
        assertFalse(NBTFileFactory.isValidNBTFile(input));
        assertArrayEquals(bytes, Files.readAllBytes(input.toPath()));
        assertArrayEquals(oldBackup, Files.readAllBytes(backup));
    }

    @ParameterizedTest
    @ValueSource(ints = {31, 8, 24, 40, 56, 72, 88, 104, 120})
    void rejectsTruncatedRecognizablePrefixes(int first) throws IOException
    {
        File input = file(new byte[]{(byte) first});
        assertThrows(EOFException.class, () -> NBTFileFactory.getCompressionType(input));
        assertThrows(EOFException.class, () -> NBTFileFactory.loadNBTToReader(input));
        assertThrows(EOFException.class, () -> NBTFileFactory.createWriter(input));
    }

    @Test
    void leavesEmptyShortAndUnknownUncompressedDataIntact() throws IOException
    {
        for (byte[] bytes : new byte[][]{{}, {0}, {10}, {(byte) 255}, {31, 0}, {7, 0}, ROOT})
        {
            File input = file(bytes);
            assertEquals(Compression_Types.NONE, NBTFileFactory.getCompressionType(input));
            try (DataInputStream stream = NBTFileFactory.loadNBTToReader(input))
            {
                assertArrayEquals(bytes, stream.readAllBytes());
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void replaysPrefixAndPreservesCompressionOnDefaultSave(Compression_Types compression) throws IOException
    {
        byte[] bytes = switch (compression)
        {
            case NONE -> ROOT;
            case ZLIB -> zlib(7, 0);
            case GZIP -> {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                try (GZIPOutputStream gzip = new GZIPOutputStream(output))
                {
                    gzip.write(ROOT);
                }
                yield output.toByteArray();
            }
            default -> throw new AssertionError(compression);
        };
        File input = file(bytes);
        assertEquals(compression, NBTFileFactory.getCompressionType(input));
        Tag_Compound root = (Tag_Compound) NBTFileFactory.readNBTFile(input);
        root.addInt("value", 42);
        NBTFileFactory.writeNBTFile(input, root);
        assertEquals(compression, NBTFileFactory.getCompressionType(input));
        assertEquals(42, NBTFileFactory.readNBTFile(input).getInt("value"));
        assertFalse(Files.exists(directory.resolve("input.nbt.bak")));
    }

    @Test
    void missingFilesFailQuietlyAcrossDetectionAndWriterEntryPoints() throws IOException
    {
        File missing = directory.resolve("missing.nbt").toFile();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try (PrintStream out = new PrintStream(stdout); PrintStream err = new PrintStream(stderr))
        {
            System.setOut(out);
            System.setErr(err);
            assertThrows(FileNotFoundException.class, () -> NBTFileFactory.getCompressionType(missing));
            assertThrows(FileNotFoundException.class, () -> NBTFileFactory.loadNBTToReader(missing));
            assertThrows(FileNotFoundException.class, () -> NBTFileFactory.createWriter(missing));
            assertThrows(FileNotFoundException.class, () -> new NBTWriter(missing, NBTLimits.DEFAULT));
        } finally
        {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
        assertEquals("", stdout.toString());
        assertEquals("", stderr.toString());
        assertFalse(missing.exists());
        NBTFileFactory.writeNBTFile(missing, new Tag_Compound(""));
        assertEquals(Compression_Types.NONE, NBTFileFactory.getCompressionType(missing));
        assertTrue(NBTFileFactory.isValidNBTFile(missing));
    }

    @Test
    void directoryReadErrorsAreNotTreatedAsUncompressed() throws IOException
    {
        File input = directory.toFile();
        assertThrows(IOException.class, () -> NBTFileFactory.getCompressionType(input));
        assertThrows(IOException.class, () -> NBTFileFactory.loadNBTToReader(input));
        assertThrows(IOException.class, () -> NBTFileFactory.createWriter(input));
        assertThrows(IOException.class, () -> NBTFileFactory.writeNBTFile(input, new Tag_Compound("")));
        assertEquals(0, directory.toFile().list().length);
    }
}
