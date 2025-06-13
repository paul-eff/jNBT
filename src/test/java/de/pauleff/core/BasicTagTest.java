package de.pauleff.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicTagTest
{

    @Test
    void testTagString()
    {
        // Test string tag creation
        Tag_String tag = new Tag_String("name", "value");
        assertEquals("name", tag.getName());
        assertEquals("value", tag.getData());
    }

    @Test
    void testTagInt()
    {
        // Test int tag creation
        Tag_Int tag = new Tag_Int("number", 42);
        assertEquals("number", tag.getName());
        assertEquals(42, tag.getData());
    }

    @Test
    void testTagDouble()
    {
        // Test double tag creation
        Tag_Double tag = new Tag_Double("decimal", 3.14);
        assertEquals("decimal", tag.getName());
        assertEquals(3.14, tag.getData());
    }

    @Test
    void testTagByte()
    {
        // Test byte tag creation
        Tag_Byte tag = new Tag_Byte("flag", (byte) 1);
        assertEquals("flag", tag.getName());
        assertEquals((byte) 1, tag.getData());
    }

    @Test
    void testTagShort()
    {
        // Test short tag creation
        Tag_Short tag = new Tag_Short("small", (short) 256);
        assertEquals("small", tag.getName());
        assertEquals((short) 256, tag.getData());
    }

    @Test
    void testTagLong()
    {
        // Test long tag creation
        Tag_Long tag = new Tag_Long("big", 9223372036854775807L);
        assertEquals("big", tag.getName());
        assertEquals(9223372036854775807L, tag.getData());
    }

    @Test
    void testTagFloat()
    {
        // Test float tag creation
        Tag_Float tag = new Tag_Float("floating", 2.5f);
        assertEquals("floating", tag.getName());
        assertEquals(2.5f, tag.getData());
    }

    @Test
    void testTagByteArray()
    {
        // Test byte array tag creation
        byte[] data = {1, 2, 3, 4};
        Tag_Byte_Array tag = new Tag_Byte_Array("bytes", data);
        assertEquals("bytes", tag.getName());
        assertArrayEquals(data, tag.getData());
    }

    @Test
    void testTagIntArray()
    {
        // Test int array tag creation
        int[] data = {10, 20, 30};
        Tag_Int_Array tag = new Tag_Int_Array("ints", data);
        assertEquals("ints", tag.getName());
        assertArrayEquals(data, tag.getData());
    }

    @Test
    void testTagLongArray()
    {
        // Test long array tag creation
        long[] data = {100L, 200L, 300L};
        Tag_Long_Array tag = new Tag_Long_Array("longs", data);
        assertEquals("longs", tag.getName());
        assertArrayEquals(data, tag.getData());
    }
}