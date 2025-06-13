package me.paulferlitz.formats.snbt;

import me.paulferlitz.api.ICompoundTag;
import me.paulferlitz.api.IListTag;
import me.paulferlitz.api.ITag;
import me.paulferlitz.api.NBTFactory;
import me.paulferlitz.util.NBTTags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for SNBT serialization functionality.
 */
public class SNBTSerializerTest
{

    @Test
    @DisplayName("Serialize basic primitive values")
    public void testSerializePrimitiveValues()
    {
        // Test all primitive types with proper suffixes
        assertEquals("42b", NBTFactory.toSNBT(NBTFactory.createByte("", (byte) 42)));
        assertEquals("32767s", NBTFactory.toSNBT(NBTFactory.createShort("", (short) 32767)));
        assertEquals("2147483647", NBTFactory.toSNBT(NBTFactory.createInt("", 2147483647)));
        assertEquals("9223372036854775807l", NBTFactory.toSNBT(NBTFactory.createLong("", 9223372036854775807L)));
        assertEquals("3.14159f", NBTFactory.toSNBT(NBTFactory.createFloat("", 3.14159f)));
        assertEquals("2.718281828459045", NBTFactory.toSNBT(NBTFactory.createDouble("", 2.718281828459045)));

        // Test special float/double values
        assertEquals("NaNf", NBTFactory.toSNBT(NBTFactory.createFloat("", Float.NaN)));
        assertEquals("Infinityf", NBTFactory.toSNBT(NBTFactory.createFloat("", Float.POSITIVE_INFINITY)));
        assertEquals("-Infinityf", NBTFactory.toSNBT(NBTFactory.createFloat("", Float.NEGATIVE_INFINITY)));
        assertEquals("NaN", NBTFactory.toSNBT(NBTFactory.createDouble("", Double.NaN)));
        assertEquals("Infinity", NBTFactory.toSNBT(NBTFactory.createDouble("", Double.POSITIVE_INFINITY)));
        assertEquals("-Infinity", NBTFactory.toSNBT(NBTFactory.createDouble("", Double.NEGATIVE_INFINITY)));
    }

    @Test
    @DisplayName("Serialize strings with smart quoting")
    public void testSerializeStrings()
    {
        // Unquoted strings (alphanumeric + underscore)
        assertEquals("simple_string", NBTFactory.toSNBT(NBTFactory.createString("", "simple_string")));
        assertEquals("minecraft_stone", NBTFactory.toSNBT(NBTFactory.createString("", "minecraft_stone")));
        assertEquals("test123", NBTFactory.toSNBT(NBTFactory.createString("", "test123")));

        // Quoted strings (contain spaces or special characters)
        assertEquals("\"Hello World\"", NBTFactory.toSNBT(NBTFactory.createString("", "Hello World")));
        assertEquals("\"special!@#$%characters\"", NBTFactory.toSNBT(NBTFactory.createString("", "special!@#$%characters")));

        // Reserved words must be quoted
        assertEquals("\"true\"", NBTFactory.toSNBT(NBTFactory.createString("", "true")));
        assertEquals("\"false\"", NBTFactory.toSNBT(NBTFactory.createString("", "false")));

        // Numbers must be quoted to avoid confusion
        assertEquals("\"123\"", NBTFactory.toSNBT(NBTFactory.createString("", "123")));
        assertEquals("\"-456\"", NBTFactory.toSNBT(NBTFactory.createString("", "-456")));

        // Empty string
        assertEquals("\"\"", NBTFactory.toSNBT(NBTFactory.createString("", "")));
    }

    @Test
    @DisplayName("Serialize strings with escape sequences")
    public void testSerializeEscapeSequences()
    {
        // Basic escape sequences
        assertEquals("\"Line 1\\nLine 2\"", NBTFactory.toSNBT(NBTFactory.createString("", "Line 1\nLine 2")));
        assertEquals("\"Tab\\tSeparated\"", NBTFactory.toSNBT(NBTFactory.createString("", "Tab\tSeparated")));
        assertEquals("\"Carriage\\rReturn\"", NBTFactory.toSNBT(NBTFactory.createString("", "Carriage\rReturn")));
        assertEquals("\"Quote:\\\"test\\\"\"", NBTFactory.toSNBT(NBTFactory.createString("", "Quote:\"test\"")));
        assertEquals("\"Backslash:\\\\test\"", NBTFactory.toSNBT(NBTFactory.createString("", "Backslash:\\test")));

        // Unicode escape for non-printable characters
        assertEquals("\"Unicode:\\u0001\\u001f\"", NBTFactory.toSNBT(NBTFactory.createString("", "Unicode:\u0001\u001f")));
    }

    @Test
    @DisplayName("Serialize arrays")
    public void testSerializeArrays()
    {
        // Byte array
        byte[] byteArray = {1, 2, 3, 4, 5};
        assertEquals("[B;1b,2b,3b,4b,5b]", NBTFactory.toSNBT(NBTFactory.createByteArray("", byteArray)));

        // Int array
        int[] intArray = {100, 200, 300};
        assertEquals("[I;100,200,300]", NBTFactory.toSNBT(NBTFactory.createIntArray("", intArray)));

        // Long array
        long[] longArray = {1000000L, 2000000L};
        assertEquals("[L;1000000l,2000000l]", NBTFactory.toSNBT(NBTFactory.createLongArray("", longArray)));

        // Empty arrays
        assertEquals("[B;]", NBTFactory.toSNBT(NBTFactory.createByteArray("", new byte[0])));
        assertEquals("[I;]", NBTFactory.toSNBT(NBTFactory.createIntArray("", new int[0])));
        assertEquals("[L;]", NBTFactory.toSNBT(NBTFactory.createLongArray("", new long[0])));
    }

    @Test
    @DisplayName("Serialize empty compound")
    public void testSerializeEmptyCompound()
    {
        ICompoundTag empty = NBTFactory.createCompound();
        assertEquals("{}", NBTFactory.toSNBT(empty));
    }

    @Test
    @DisplayName("Serialize simple compound")
    public void testSerializeSimpleCompound()
    {
        ICompoundTag compound = NBTFactory.createCompound()
                .addString("name", "Steve")
                .addInt("level", 42)
                .addDouble("health", 20.0);

        String result = NBTFactory.toSNBT(compound);
        // Note: Order might vary, so we test that all elements are present
        assertTrue(result.contains("name: Steve") || result.contains("name: \"Steve\""));
        assertTrue(result.contains("level: 42"));
        assertTrue(result.contains("health: 20.0"));
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
    }

    @Test
    @DisplayName("Serialize nested compound")
    public void testSerializeNestedCompound()
    {
        ICompoundTag stats = NBTFactory.createCompound("stats")
                .addInt("level", 5)
                .addDouble("xp", 100.5);

        ICompoundTag nested = NBTFactory.createCompound()
                .addString("name", "Test");
        ((me.paulferlitz.core.Collection_Tag) nested).addTag(stats);

        String result = NBTFactory.toSNBT(nested);
        assertTrue(result.contains("name: Test") || result.contains("name: \"Test\""));
        assertTrue(result.contains("stats: {"));
        assertTrue(result.contains("level: 5"));
        assertTrue(result.contains("xp: 100.5"));
    }

    @Test
    @DisplayName("Serialize empty list")
    public void testSerializeEmptyList()
    {
        IListTag emptyList = NBTFactory.createList("", NBTTags.Tag_String.getId());
        assertEquals("[]", NBTFactory.toSNBT(emptyList));
    }

    @Test
    @DisplayName("Serialize string list")
    public void testSerializeStringList()
    {
        IListTag list = NBTFactory.createList("", NBTTags.Tag_String.getId())
                .addString("item1", "apple")
                .addString("item2", "banana")
                .addString("item3", "cherry");

        // Standard SNBT output (unquoted simple strings with spaces)
        assertEquals("[ apple, banana, cherry ]", NBTFactory.toSNBT(list));
    }

    @Test
    @DisplayName("Serialize integer list")
    public void testSerializeIntegerList()
    {
        IListTag list = NBTFactory.createList("", NBTTags.Tag_Int.getId());
        ((me.paulferlitz.core.Collection_Tag) list).addTag(NBTFactory.createInt("", 1));
        ((me.paulferlitz.core.Collection_Tag) list).addTag(NBTFactory.createInt("", 2));
        ((me.paulferlitz.core.Collection_Tag) list).addTag(NBTFactory.createInt("", 3));

        assertEquals("[ 1, 2, 3 ]", NBTFactory.toSNBT(list));
    }

    @Test
    @DisplayName("Serialize compound list")
    public void testSerializeCompoundList()
    {
        IListTag list = NBTFactory.createList("", NBTTags.Tag_Compound.getId());

        ICompoundTag item1 = NBTFactory.createCompound()
                .addString("name", "sword")
                .addInt("damage", 10);
        ICompoundTag item2 = NBTFactory.createCompound()
                .addString("name", "shield")
                .addInt("defense", 5);

        ((me.paulferlitz.core.Collection_Tag) list).addTag(item1);
        ((me.paulferlitz.core.Collection_Tag) list).addTag(item2);

        String result = NBTFactory.toSNBT(list);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("name: sword") || result.contains("name: \"sword\""));
        assertTrue(result.contains("damage: 10"));
        assertTrue(result.contains("name: shield") || result.contains("name: \"shield\""));
        assertTrue(result.contains("defense: 5"));
    }

    @Test
    @DisplayName("Test standard SNBT formatting")
    public void testStandardFormatting()
    {
        ICompoundTag compound = NBTFactory.createCompound()
                .addString("name", "test")
                .addInt("value", 42);

        // Standard SNBT format (Minecraft-compatible)
        String result = NBTFactory.toSNBT(compound);
        assertTrue(result.contains(": "));
        assertTrue(result.contains(", "));
        assertTrue(result.contains("name: test"));
        assertTrue(result.contains("value: 42"));
    }

    @Test
    @DisplayName("Test array formatting")
    public void testArrayFormatting()
    {
        byte[] array = {1, 2, 3};
        ITag<?> byteArray = NBTFactory.createByteArray("", array);

        // Standard format (no spaces in arrays for Minecraft compatibility)
        assertEquals("[B;1b,2b,3b]", NBTFactory.toSNBT(byteArray));
    }

    @Test
    @DisplayName("Test double type (no suffix)")
    public void testDoubleType()
    {
        ITag<?> doubleTag = NBTFactory.createDouble("", 3.14159);

        // Standard format (no suffix for doubles)
        assertEquals("3.14159", NBTFactory.toSNBT(doubleTag));
    }

    @Test
    @DisplayName("Test Minecraft command compatibility")
    public void testMinecraftCommandCompatibility()
    {
        // Create a typical /give command NBT structure
        ICompoundTag display = NBTFactory.createCompound("display")
                .addString("Name", "\"Legendary Sword\"");

        IListTag lore = NBTFactory.createList("Lore", NBTTags.Tag_String.getId())
                .addString("line1", "§cPowerful weapon")
                .addString("line2", "§7Ancient artifact");
        ((me.paulferlitz.core.Collection_Tag) display).addTag(lore);

        ICompoundTag itemNBT = NBTFactory.createCompound();
        ((me.paulferlitz.core.Collection_Tag) itemNBT).addTag(display);

        // Add enchantments
        IListTag enchantments = NBTFactory.createList("Enchantments", NBTTags.Tag_Compound.getId());
        ICompoundTag sharpness = NBTFactory.createCompound()
                .addString("id", "sharpness")
                .addByte("lvl", (byte) 5);
        ((me.paulferlitz.core.Collection_Tag) enchantments).addTag(sharpness);
        ((me.paulferlitz.core.Collection_Tag) itemNBT).addTag(enchantments);

        String result = NBTFactory.toSNBT(itemNBT);

        // Should be parseable back
        try
        {
            ITag<?> parsed = NBTFactory.parseFromSNBT(result);
            assertNotNull(parsed);
        } catch (SNBTException e)
        {
            fail("Generated SNBT should be parseable: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test round-trip conversion")
    public void testRoundTripConversion() throws SNBTException
    {
        // Create complex structure
        ICompoundTag original = NBTFactory.createCompound()
                .addString("name", "Test Player")
                .addByte("gameMode", (byte) 1)
                .addFloat("health", 20.0f);

        IListTag inventory = NBTFactory.createList("inventory", NBTTags.Tag_Compound.getId());
        ((me.paulferlitz.core.Collection_Tag) original).addTag(inventory);

        // Add inventory item
        ICompoundTag item = NBTFactory.createCompound()
                .addString("id", "minecraft:diamond_sword")
                .addByte("Count", (byte) 1);
        ((me.paulferlitz.core.Collection_Tag) inventory).addTag(item);

        // Serialize to SNBT
        String snbt = NBTFactory.toSNBT(original);

        // Parse back
        ICompoundTag parsed = NBTFactory.parseCompoundFromSNBT(snbt);

        // Verify key data
        assertEquals("Test Player", parsed.getString("name"));
        assertEquals((byte) 1, parsed.getByte("gameMode"));
        assertEquals(20.0f, (Float) parsed.getTag("health").getData(), 0.001f);
        assertEquals(1, parsed.getList("inventory").getData().size());

        ICompoundTag parsedItem = (ICompoundTag) parsed.getList("inventory").getData().get(0);
        assertEquals("minecraft:diamond_sword", parsedItem.getString("id"));
        assertEquals((byte) 1, parsedItem.getByte("Count"));
    }

    @Test
    @DisplayName("Test file writing")
    public void testFileWriting(@TempDir Path tempDir) throws IOException
    {
        ICompoundTag data = NBTFactory.createCompound()
                .addString("message", "Hello SNBT File!")
                .addInt("version", 1);

        File testFile = tempDir.resolve("test.snbt").toFile();

        // Write to file
        NBTFactory.writeToSNBTFile(data, testFile);

        // Verify file exists and has content
        assertTrue(testFile.exists());
        String content = Files.readString(testFile.toPath());
        assertTrue(content.contains("message: \"Hello SNBT File!\""));
        assertTrue(content.contains("version: 1"));
        assertTrue(content.endsWith("\n")); // Should have trailing newline
    }

    @Test
    @DisplayName("Test file writing with custom config")
    public void testFileWritingWithConfig(@TempDir Path tempDir) throws IOException
    {
        ICompoundTag data = NBTFactory.createCompound()
                .addString("test", "value")
                .addInt("number", 42);

        File testFile = tempDir.resolve("pretty.snbt").toFile();

        // Write to SNBT file
        NBTFactory.writeToSNBTFile(data, testFile);

        // Verify content and newline
        String content = Files.readString(testFile.toPath());
        assertTrue(content.contains("test: value"));
        assertTrue(content.contains("number: 42"));
        assertTrue(content.endsWith("\n")); // Trailing newline
    }

    @Test
    @DisplayName("Test null input handling")
    public void testNullInput()
    {
        assertEquals("null", NBTFactory.toSNBT(null));

        // Null string should produce empty quoted string
        assertEquals("\"\"", NBTFactory.toSNBT(NBTFactory.createString("", null)));
    }

    @Test
    @DisplayName("Test edge cases")
    public void testEdgeCases()
    {
        // Very large numbers
        assertEquals("127b", NBTFactory.toSNBT(NBTFactory.createByte("", Byte.MAX_VALUE)));
        assertEquals("-128b", NBTFactory.toSNBT(NBTFactory.createByte("", Byte.MIN_VALUE)));
        assertEquals("32767s", NBTFactory.toSNBT(NBTFactory.createShort("", Short.MAX_VALUE)));
        assertEquals("-32768s", NBTFactory.toSNBT(NBTFactory.createShort("", Short.MIN_VALUE)));

        // Very long string (should be unquoted since it's simple)
        String longString = "a".repeat(1000);
        String result = NBTFactory.toSNBT(NBTFactory.createString("", longString));
        assertEquals(longString, result);

        // Complex nested structure
        ICompoundTag complex = NBTFactory.createCompound();
        for (int i = 0; i < 5; i++)
        {
            ICompoundTag level = NBTFactory.createCompound();
            for (int j = 0; j < 3; j++)
            {
                level.addInt("value_" + j, i * 10 + j);
            }
            ((me.paulferlitz.core.Collection_Tag) complex).addTag(level);
            level.setName("level_" + i);
        }

        String complexResult = NBTFactory.toSNBT(complex);
        assertNotNull(complexResult);
        assertTrue(complexResult.length() > 100); // Should be substantial
    }
}