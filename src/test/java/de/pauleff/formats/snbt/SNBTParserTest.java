package de.pauleff.formats.snbt;

import de.pauleff.api.ICompoundTag;
import de.pauleff.api.IListTag;
import de.pauleff.api.ITag;
import de.pauleff.api.NBTFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SNBTParserTest
{

    @Test
    public void testParsePrimitiveValues() throws SNBTException
    {
        // Test basic numeric types with suffixes
        ITag<?> byteTag = NBTFactory.parseFromSNBT("42b");
        assertEquals((byte) 42, byteTag.getData());

        ITag<?> intTag = NBTFactory.parseFromSNBT("12345");
        assertEquals(12345, intTag.getData());

        ITag<?> floatTag = NBTFactory.parseFromSNBT("3.14f");
        assertEquals(3.14f, (Float) floatTag.getData(), 0.001f);

        ITag<?> stringTag = NBTFactory.parseFromSNBT("\"Hello World\"");
        assertEquals("Hello World", stringTag.getData());
    }

    @Test
    public void testParseBooleanValues() throws SNBTException
    {
        // Test boolean conversion to bytes
        ITag<?> trueTag = NBTFactory.parseFromSNBT("true");
        assertEquals((byte) 1, trueTag.getData());

        ITag<?> falseTag = NBTFactory.parseFromSNBT("false");
        assertEquals((byte) 0, falseTag.getData());
    }

    @Test
    public void testParseCompounds() throws SNBTException
    {
        // Test empty compound
        ITag<?> empty = NBTFactory.parseFromSNBT("{}");
        assertInstanceOf(ICompoundTag.class, empty);
        assertTrue(((ICompoundTag) empty).getData().isEmpty());

        // Test simple compound
        ITag<?> simple = NBTFactory.parseFromSNBT("{name: \"Steve\", level: 42}");
        ICompoundTag compound = (ICompoundTag) simple;
        assertEquals("Steve", compound.getString("name"));
        assertEquals(42, compound.getInt("level"));
    }

    @Test
    public void testParseLists() throws SNBTException
    {
        // Test string list
        ITag<?> stringList = NBTFactory.parseFromSNBT("[\"apple\", \"banana\"]");
        IListTag list = (IListTag) stringList;
        assertEquals(2, list.getData().size());
        assertEquals("apple", list.getData().get(0).getData());
    }

    @Test
    public void testParseArrays() throws SNBTException
    {
        // Test byte array
        ITag<?> byteArray = NBTFactory.parseFromSNBT("[B; 1b, 2b, 3b]");
        assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) byteArray.getData());

        // Test int array
        ITag<?> intArray = NBTFactory.parseFromSNBT("[I; 100, 200]");
        assertArrayEquals(new int[]{100, 200}, (int[]) intArray.getData());
    }

    @Test
    public void testErrorCases()
    {
        // Test invalid syntax
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("{invalid"));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("[1, \"mixed\"]"));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("\"unterminated"));
    }
}