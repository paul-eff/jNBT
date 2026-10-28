package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.core.Tag_List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UTFLengthTest
{
    @TempDir
    Path tempDir;

    private static Stream<Arguments> boundaries()
    {
        return Stream.of(32767, 32768, 65535, 65536).flatMap(length ->
                Stream.of("x", "\u0000", "\u0800", "\uD83D\uDE00").map(unit ->
                {
                    int unitBytes = unit.equals("x") ? 1 : unit.equals("\u0000") ? 2 : unit.length() * 3;
                    String text = unit.repeat(length / unitBytes) + "x".repeat(length % unitBytes);
                    return Arguments.of(length, text);
                }));
    }

    @ParameterizedTest(name = "boundary {0} bytes, case {index}")
    @MethodSource("boundaries")
    void checksEncodedByteBoundaries(int length, String text) throws IOException
    {
        Tag_Compound root = new Tag_Compound(text);
        root.addString(text, text);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        NBTWriter writer = new NBTWriter(new DataOutputStream(actual));
        if (length > 65535)
        {
            assertThrows(UTFDataFormatException.class, () -> writer.write(root));
            assertEquals(0, actual.size());
            return;
        }

        // Independent Java framing verifies both name fields and the string payload.
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(expected))
        {
            out.writeByte(10);
            out.writeUTF(text);
            out.writeByte(8);
            out.writeUTF(text);
            out.writeUTF(text);
            out.writeByte(0);
        }
        writer.write(root);
        assertArrayEquals(expected.toByteArray(), actual.toByteArray());

        ICompoundTag loaded = new NBTReader(new DataInputStream(
                new ByteArrayInputStream(expected.toByteArray()))).read();
        assertEquals(text, loaded.getName());
        assertEquals(text, loaded.getData().getFirst().getName());
        assertEquals(text, loaded.getString(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"root", "key", "value", "listValue", "nestedKey"})
    void findsOversizedFieldsBeforeWritingAnyBytes(String field)
    {
        String oversized = "\u0000".repeat(32768);
        Tag_Compound root = new Tag_Compound("");
        root.addInt("valid", 1);
        switch (field)
        {
            case "root" -> root.setName(oversized);
            case "key" -> root.addInt(oversized, 2);
            case "value" -> root.addString("bad", oversized);
            case "listValue" ->
            {
                Tag_List list = new Tag_List("list", 8);
                list.addString("", oversized);
                root.addTag(list);
            }
            case "nestedKey" ->
            {
                Tag_List list = new Tag_List("list", 10);
                Tag_Compound child = new Tag_Compound("");
                child.addString(oversized, "value");
                list.addTag(child);
                root.addTag(list);
            }
            default -> throw new AssertionError(field);
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        assertThrows(UTFDataFormatException.class,
                () -> new NBTWriter(new DataOutputStream(bytes)).write(root));
        assertEquals(0, bytes.size());
    }

    @Test
    void ignoresListElementNamesThatAreNotStored() throws IOException
    {
        Tag_Compound root = new Tag_Compound("");
        Tag_List list = new Tag_List("list", 8);
        list.addString("x".repeat(65536), "value");
        root.addTag(list);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        ICompoundTag loaded = new NBTReader(new DataInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))).read();
        assertEquals("value", loaded.getList("list").getData().getFirst().getData());
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void invalidLengthsPreserveFilesAndBackups(Compression_Types compression) throws IOException
    {
        Path file = tempDir.resolve("test.dat");
        Path backup = tempDir.resolve("test.dat.bak");
        byte[] original = {10, 0, 0, 0};
        byte[] originalBackup = {1, 2, 3};
        Files.write(file, original);
        Files.write(backup, originalBackup);
        Tag_Compound invalid = new Tag_Compound("");
        invalid.addString("bad", "x".repeat(65536));

        try (NBTWriter writer = new NBTWriter(file.toFile(), compression))
        {
            assertArrayEquals(original, Files.readAllBytes(file));
            assertArrayEquals(originalBackup, Files.readAllBytes(backup));
            assertThrows(UTFDataFormatException.class, () -> writer.write(invalid));
        }
        assertArrayEquals(original, Files.readAllBytes(file));
        assertArrayEquals(originalBackup, Files.readAllBytes(backup));

        try (NBTWriter writer = new NBTWriter(file.toFile()))
        {
            assertThrows(UTFDataFormatException.class, () -> writer.write(invalid));
        }
        assertArrayEquals(original, Files.readAllBytes(file));
        assertArrayEquals(originalBackup, Files.readAllBytes(backup));

        Path missing = tempDir.resolve("missing.dat");
        try (NBTWriter writer = new NBTWriter(missing.toFile(), compression))
        {
            assertThrows(UTFDataFormatException.class, () -> writer.write(invalid));
        }
        assertFalse(Files.exists(missing));

        try (NBTWriter writer = new NBTWriter(file.toFile(), compression))
        {
            writer.write(new Tag_Compound(""));
        }
        assertArrayEquals(original, Files.readAllBytes(backup));
        try (NBTReader reader = new NBTReader(file.toFile()))
        {
            assertEquals("", reader.read().getName());
        }
    }

    @Test
    void closingUnusedFileWriterDoesNotCreateFile() throws IOException
    {
        Path file = tempDir.resolve("unused.dat");
        NBTWriter writer = new NBTWriter(file.toFile(), Compression_Types.GZIP);
        writer.close();
        assertThrows(IOException.class, () -> writer.write(new Tag_Compound("")));
        assertFalse(Files.exists(file));
    }
}
