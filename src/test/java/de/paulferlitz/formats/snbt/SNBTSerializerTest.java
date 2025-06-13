package de.paulferlitz.formats.snbt;

import de.paulferlitz.api.ICompoundTag;
import de.paulferlitz.api.IListTag;
import de.paulferlitz.api.NBTFactory;
import de.paulferlitz.util.NBTTags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SNBTSerializerTest
{

    @Test
    public void testSerializePrimitiveValues()
    {
        // Test primitive types with suffixes
        assertEquals("42b", NBTFactory.toSNBT(NBTFactory.createByte("", (byte) 42)));
        assertEquals("2147483647", NBTFactory.toSNBT(NBTFactory.createInt("", 2147483647)));
        assertEquals("3.14159f", NBTFactory.toSNBT(NBTFactory.createFloat("", 3.14159f)));
        assertEquals("2.718281828459045", NBTFactory.toSNBT(NBTFactory.createDouble("", 2.718281828459045)));
    }

    @Test
    public void testSerializeStrings()
    {
        // Test string quoting
        assertEquals("simple_string", NBTFactory.toSNBT(NBTFactory.createString("", "simple_string")));
        assertEquals("\"Hello World\"", NBTFactory.toSNBT(NBTFactory.createString("", "Hello World")));
        assertEquals("\"true\"", NBTFactory.toSNBT(NBTFactory.createString("", "true")));
        assertEquals("\"\"", NBTFactory.toSNBT(NBTFactory.createString("", "")));
    }

    @Test
    public void testSerializeArrays()
    {
        // Test array serialization
        byte[] byteArray = {1, 2, 3};
        assertEquals("[B;1b,2b,3b]", NBTFactory.toSNBT(NBTFactory.createByteArray("", byteArray)));

        int[] intArray = {100, 200};
        assertEquals("[I;100,200]", NBTFactory.toSNBT(NBTFactory.createIntArray("", intArray)));

        assertEquals("[B;]", NBTFactory.toSNBT(NBTFactory.createByteArray("", new byte[0])));
    }

    @Test
    public void testSerializeCompounds()
    {
        // Test empty compound
        ICompoundTag empty = NBTFactory.createCompound();
        assertEquals("{}", NBTFactory.toSNBT(empty));

        // Test simple compound
        ICompoundTag compound = NBTFactory.createCompound()
                .addString("name", "Steve")
                .addInt("level", 42);

        String result = NBTFactory.toSNBT(compound);
        assertTrue(result.contains("name: Steve"));
        assertTrue(result.contains("level: 42"));
    }

    @Test
    public void testSerializeLists()
    {
        // Test empty list
        IListTag emptyList = NBTFactory.createList("", NBTTags.Tag_String.getId());
        assertEquals("[]", NBTFactory.toSNBT(emptyList));

        // Test string list
        IListTag list = NBTFactory.createList("", NBTTags.Tag_String.getId())
                .addString("item1", "apple")
                .addString("item2", "banana");

        assertEquals("[ apple, banana ]", NBTFactory.toSNBT(list));
    }

    @Test
    public void testRoundTripConversion() throws SNBTException
    {
        // Test parse -> serialize -> parse
        ICompoundTag original = NBTFactory.createCompound()
                .addString("name", "Test")
                .addByte("flag", (byte) 1);

        String snbt = NBTFactory.toSNBT(original);
        ICompoundTag parsed = NBTFactory.parseCompoundFromSNBT(snbt);

        assertEquals("Test", parsed.getString("name"));
        assertEquals((byte) 1, parsed.getByte("flag"));
    }

    @Test
    public void testFileWriting(@TempDir Path tempDir) throws IOException
    {
        // Test writing to file
        ICompoundTag data = NBTFactory.createCompound()
                .addString("message", "Hello SNBT!");

        File testFile = tempDir.resolve("test.snbt").toFile();
        NBTFactory.writeToSNBTFile(data, testFile);

        assertTrue(testFile.exists());
        String content = Files.readString(testFile.toPath());
        assertTrue(content.contains("message: \"Hello SNBT!\""));
        assertTrue(content.endsWith("\n"));
    }

    @Test
    public void testNullInput()
    {
        // Test null handling
        assertEquals("null", NBTFactory.toSNBT(null));
        assertEquals("\"\"", NBTFactory.toSNBT(NBTFactory.createString("", null)));
    }
}