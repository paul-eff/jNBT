package me.paulferlitz.formats.snbt;

import me.paulferlitz.api.ICompoundTag;
import me.paulferlitz.api.IListTag;
import me.paulferlitz.api.ITag;
import me.paulferlitz.api.NBTFactory;
import me.paulferlitz.util.NBTTags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for SNBT parser functionality.
 */
public class SNBTParserTest
{

    @Test
    @DisplayName("Parse basic primitive values")
    public void testParsePrimitiveValues() throws SNBTException
    {
        // Byte values
        ITag<?> byteTag = NBTFactory.parseFromSNBT("42b");
        assertEquals(Byte.class, byteTag.getData().getClass());
        assertEquals((byte) 42, byteTag.getData());

        // Short values
        ITag<?> shortTag = NBTFactory.parseFromSNBT("-123s");
        assertEquals(Short.class, shortTag.getData().getClass());
        assertEquals((short) -123, shortTag.getData());

        // Integer values (no suffix)
        ITag<?> intTag = NBTFactory.parseFromSNBT("12345");
        assertEquals(Integer.class, intTag.getData().getClass());
        assertEquals(12345, intTag.getData());

        // Long values
        ITag<?> longTag = NBTFactory.parseFromSNBT("9876543210l");
        assertEquals(Long.class, longTag.getData().getClass());
        assertEquals(9876543210L, longTag.getData());

        // Float values
        ITag<?> floatTag = NBTFactory.parseFromSNBT("3.14f");
        assertEquals(Float.class, floatTag.getData().getClass());
        assertEquals(3.14f, (Float) floatTag.getData(), 0.001f);

        // Double values
        ITag<?> doubleTag = NBTFactory.parseFromSNBT("2.718281828");
        assertEquals(Double.class, doubleTag.getData().getClass());
        assertEquals(2.718281828, (Double) doubleTag.getData(), 0.000000001);

        // String values
        ITag<?> stringTag = NBTFactory.parseFromSNBT("\"Hello World\"");
        assertEquals(String.class, stringTag.getData().getClass());
        assertEquals("Hello World", stringTag.getData());
    }

    @Test
    @DisplayName("Parse boolean values as bytes")
    public void testParseBooleanValues() throws SNBTException
    {
        ITag<?> trueTag = NBTFactory.parseFromSNBT("true");
        assertEquals(Byte.class, trueTag.getData().getClass());
        assertEquals((byte) 1, trueTag.getData());

        ITag<?> falseTag = NBTFactory.parseFromSNBT("false");
        assertEquals(Byte.class, falseTag.getData().getClass());
        assertEquals((byte) 0, falseTag.getData());
    }

    @Test
    @DisplayName("Parse unquoted strings")
    public void testParseUnquotedStrings() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("minecraft_stone");
        assertEquals(String.class, tag.getData().getClass());
        assertEquals("minecraft_stone", tag.getData());
    }

    @Test
    @DisplayName("Parse quoted strings with escape sequences")
    public void testParseQuotedStringsWithEscapes() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("\"Line 1\\nLine 2\\tTabbed\"");
        assertEquals(String.class, tag.getData().getClass());
        assertEquals("Line 1\nLine 2\tTabbed", tag.getData());

        ITag<?> quotedTag = NBTFactory.parseFromSNBT("\"He said \\\"Hello\\\"\"");
        assertEquals("He said \"Hello\"", quotedTag.getData());
    }

    @Test
    @DisplayName("Parse empty compound")
    public void testParseEmptyCompound() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("{}");
        assertInstanceOf(ICompoundTag.class, tag);
        ICompoundTag compound = (ICompoundTag) tag;
        assertTrue(compound.getData().isEmpty());
    }

    @Test
    @DisplayName("Parse simple compound")
    public void testParseSimpleCompound() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("{name: \"Steve\", level: 42, health: 20.0}");
        assertInstanceOf(ICompoundTag.class, tag);
        ICompoundTag compound = (ICompoundTag) tag;

        assertEquals("Steve", compound.getString("name"));
        assertEquals(42, compound.getInt("level"));
        assertEquals(20.0, compound.getDouble("health"), 0.001);
    }

    @Test
    @DisplayName("Parse nested compound")
    public void testParseNestedCompound() throws SNBTException
    {
        String snbt = "{player: {name: \"Steve\", stats: {level: 42, xp: 100}}}";
        ITag<?> tag = NBTFactory.parseFromSNBT(snbt);
        assertInstanceOf(ICompoundTag.class, tag);
        ICompoundTag root = (ICompoundTag) tag;

        ICompoundTag player = root.getCompound("player");
        assertNotNull(player);
        assertEquals("Steve", player.getString("name"));

        ICompoundTag stats = player.getCompound("stats");
        assertNotNull(stats);
        assertEquals(42, stats.getInt("level"));
        assertEquals(100, stats.getInt("xp"));
    }

    @Test
    @DisplayName("Parse empty list")
    public void testParseEmptyList() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("[]");
        assertInstanceOf(IListTag.class, tag);
        IListTag list = (IListTag) tag;
        assertEquals(0, list.getData().size());
        assertEquals(NBTTags.Tag_Compound.getId(), list.getListTypeID());
    }

    @Test
    @DisplayName("Parse string list")
    public void testParseStringList() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("[\"apple\", \"banana\", \"cherry\"]");
        assertInstanceOf(IListTag.class, tag);
        IListTag list = (IListTag) tag;
        assertEquals(3, list.getData().size());
        assertEquals(NBTTags.Tag_String.getId(), list.getListTypeID());

        assertEquals("apple", list.getData().get(0).getData());
        assertEquals("banana", list.getData().get(1).getData());
        assertEquals("cherry", list.getData().get(2).getData());
    }

    @Test
    @DisplayName("Parse integer list")
    public void testParseIntegerList() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("[1, 2, 3, 4, 5]");
        assertInstanceOf(IListTag.class, tag);
        IListTag list = (IListTag) tag;
        assertEquals(5, list.getData().size());
        assertEquals(NBTTags.Tag_Int.getId(), list.getListTypeID());

        for (int i = 0; i < 5; i++)
        {
            assertEquals(i + 1, list.getData().get(i).getData());
        }
    }

    @Test
    @DisplayName("Parse compound list")
    public void testParseCompoundList() throws SNBTException
    {
        String snbt = "[{name: \"item1\", count: 64}, {name: \"item2\", count: 32}]";
        ITag<?> tag = NBTFactory.parseFromSNBT(snbt);
        assertInstanceOf(IListTag.class, tag);
        IListTag list = (IListTag) tag;
        assertEquals(2, list.getData().size());
        assertEquals(NBTTags.Tag_Compound.getId(), list.getListTypeID());

        ICompoundTag item1 = (ICompoundTag) list.getData().get(0);
        assertEquals("item1", item1.getString("name"));
        assertEquals(64, item1.getInt("count"));

        ICompoundTag item2 = (ICompoundTag) list.getData().get(1);
        assertEquals("item2", item2.getString("name"));
        assertEquals(32, item2.getInt("count"));
    }

    @Test
    @DisplayName("Parse byte array")
    public void testParseByteArray() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("[B; 1b, 2b, 3b, 4b]");
        assertEquals(byte[].class, tag.getData().getClass());
        byte[] array = (byte[]) tag.getData();
        assertArrayEquals(new byte[]{1, 2, 3, 4}, array);
    }

    @Test
    @DisplayName("Parse int array")
    public void testParseIntArray() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("[I; 100, 200, 300]");
        assertEquals(int[].class, tag.getData().getClass());
        int[] array = (int[]) tag.getData();
        assertArrayEquals(new int[]{100, 200, 300}, array);
    }

    @Test
    @DisplayName("Parse long array")
    public void testParseLongArray() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("[L; 1000000l, 2000000l, 3000000l]");
        assertEquals(long[].class, tag.getData().getClass());
        long[] array = (long[]) tag.getData();
        assertArrayEquals(new long[]{1000000L, 2000000L, 3000000L}, array);
    }

    @Test
    @DisplayName("Parse empty arrays")
    public void testParseEmptyArrays() throws SNBTException
    {
        ITag<?> byteArray = NBTFactory.parseFromSNBT("[B;]");
        assertEquals(byte[].class, byteArray.getData().getClass());
        assertEquals(0, ((byte[]) byteArray.getData()).length);

        ITag<?> intArray = NBTFactory.parseFromSNBT("[I;]");
        assertEquals(int[].class, intArray.getData().getClass());
        assertEquals(0, ((int[]) intArray.getData()).length);

        ITag<?> longArray = NBTFactory.parseFromSNBT("[L;]");
        assertEquals(long[].class, longArray.getData().getClass());
        assertEquals(0, ((long[]) longArray.getData()).length);
    }

    @Test
    @DisplayName("Parse Minecraft command examples")
    public void testParseMinecraftCommands() throws SNBTException
    {
        // Typical /give command NBT
        String giveCmd = "{display:{Name:\"\\\"Custom Sword\\\"\",Lore:[\"Line 1\",\"Line 2\"]},Enchantments:[{id:\"sharpness\",lvl:5s}]}";
        ITag<?> tag = NBTFactory.parseFromSNBT(giveCmd);
        assertInstanceOf(ICompoundTag.class, tag);
        ICompoundTag root = (ICompoundTag) tag;

        ICompoundTag display = root.getCompound("display");
        assertNotNull(display);
        assertEquals("\"Custom Sword\"", display.getString("Name"));

        IListTag lore = display.getList("Lore");
        assertNotNull(lore);
        assertEquals(2, lore.getData().size());
        assertEquals("Line 1", lore.getData().get(0).getData());
        assertEquals("Line 2", lore.getData().get(1).getData());

        IListTag enchantments = root.getList("Enchantments");
        assertNotNull(enchantments);
        assertEquals(1, enchantments.getData().size());
        ICompoundTag enchant = (ICompoundTag) enchantments.getData().get(0);
        assertEquals("sharpness", enchant.getString("id"));
        assertEquals((short) 5, (Short) enchant.getTag("lvl").getData());
    }

    @Test
    @DisplayName("Parse with trailing commas")
    public void testParseWithTrailingCommas() throws SNBTException
    {
        // Compound with trailing comma
        ITag<?> compound = NBTFactory.parseFromSNBT("{a: 1, b: 2,}");
        assertInstanceOf(ICompoundTag.class, compound);
        assertEquals(2, ((ICompoundTag) compound).getData().size());

        // List with trailing comma
        ITag<?> list = NBTFactory.parseFromSNBT("[1, 2, 3,]");
        assertInstanceOf(IListTag.class, list);
        assertEquals(3, ((IListTag) list).getData().size());
    }

    @Test
    @DisplayName("Test factory convenience methods")
    public void testFactoryConvenienceMethods() throws SNBTException
    {
        // Test parseCompoundFromSNBT
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT("{test: \"value\"}");
        assertEquals("value", compound.getString("test"));

        // Test parseListFromSNBT
        IListTag list = NBTFactory.parseListFromSNBT("[1, 2, 3]");
        assertEquals(3, list.getData().size());
    }

    @Test
    @DisplayName("Test error cases")
    public void testErrorCases()
    {
        // Null/empty input
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(null));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(""));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("   "));

        // Invalid syntax
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("{invalid syntax"));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("[1, 2, \"three\"]")); // Mixed types
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("42x")); // Invalid suffix

        // Type mismatches in convenience methods
        assertThrows(SNBTException.class, () -> NBTFactory.parseCompoundFromSNBT("42"));
        assertThrows(SNBTException.class, () -> NBTFactory.parseListFromSNBT("{test: 1}"));

        // Unterminated strings
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("\"unterminated"));

        // Invalid array types
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("[X; 1, 2, 3]"));
    }

    @Test
    @DisplayName("Test whitespace handling")
    public void testWhitespaceHandling() throws SNBTException
    {
        // Various whitespace scenarios
        ITag<?> tag1 = NBTFactory.parseFromSNBT("  {  key  :  \"value\"  }  ");
        assertInstanceOf(ICompoundTag.class, tag1);
        assertEquals("value", ((ICompoundTag) tag1).getString("key"));

        ITag<?> tag2 = NBTFactory.parseFromSNBT("[\n  1,\n  2,\n  3\n]");
        assertInstanceOf(IListTag.class, tag2);
        assertEquals(3, ((IListTag) tag2).getData().size());
    }

    @Test
    @DisplayName("Test case sensitivity")
    public void testCaseSensitivity() throws SNBTException
    {
        // Type suffixes should be case insensitive
        ITag<?> byteUpper = NBTFactory.parseFromSNBT("42B");
        ITag<?> byteLower = NBTFactory.parseFromSNBT("42b");
        assertEquals(byteUpper.getData(), byteLower.getData());

        ITag<?> floatUpper = NBTFactory.parseFromSNBT("3.14F");
        ITag<?> floatLower = NBTFactory.parseFromSNBT("3.14f");
        assertEquals(floatUpper.getData(), floatLower.getData());
    }

    @Test
    @DisplayName("Test unicode escape sequences")
    public void testUnicodeEscapeSequences() throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("\"Unicode: \\u0041\\u0042\\u0043\"");
        assertEquals("Unicode: ABC", tag.getData());
    }

    @Test
    @DisplayName("Test large numbers")
    public void testLargeNumbers() throws SNBTException
    {
        // Test edge cases for numeric types
        ITag<?> maxByte = NBTFactory.parseFromSNBT("127b");
        assertEquals((byte) 127, maxByte.getData());

        ITag<?> minByte = NBTFactory.parseFromSNBT("-128b");
        assertEquals((byte) -128, minByte.getData());

        ITag<?> maxShort = NBTFactory.parseFromSNBT("32767s");
        assertEquals((short) 32767, maxShort.getData());

        ITag<?> maxInt = NBTFactory.parseFromSNBT("2147483647");
        assertEquals(2147483647, maxInt.getData());

        ITag<?> maxLong = NBTFactory.parseFromSNBT("9223372036854775807l");
        assertEquals(9223372036854775807L, maxLong.getData());
    }
}