package de.pauleff.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NBTTagsTest
{

    @Test
    void testTagIds()
    {
        // Test NBT tag ID constants
        assertEquals(0, NBTTags.Tag_End.getId());
        assertEquals(1, NBTTags.Tag_Byte.getId());
        assertEquals(8, NBTTags.Tag_String.getId());
        assertEquals(10, NBTTags.Tag_Compound.getId());
    }

    @Test
    void testTagNames()
    {
        // Test NBT tag name constants
        assertEquals("Tag_End", NBTTags.Tag_End.getName());
        assertEquals("Tag_Byte", NBTTags.Tag_Byte.getName());
        assertEquals("Tag_String", NBTTags.Tag_String.getName());
        assertEquals("Tag_Compound", NBTTags.Tag_Compound.getName());
    }

    @Test
    void testEnumValues()
    {
        // Test enum has expected values
        NBTTags[] allTags = NBTTags.values();
        assertEquals(13, allTags.length);
        assertNotNull(NBTTags.Tag_End);
        assertNotNull(NBTTags.Tag_Compound);
    }

    @Test
    void testGetById()
    {
        // Test lookup by ID
        assertEquals(NBTTags.Tag_End, NBTTags.getById(0));
        assertEquals(NBTTags.Tag_Byte, NBTTags.getById(1));
        assertEquals(NBTTags.Tag_String, NBTTags.getById(8));
        assertEquals(NBTTags.Tag_Compound, NBTTags.getById(10));
    }

    @Test
    void testInvalidId()
    {
        // Test invalid ID returns null
        assertNull(NBTTags.getById(-1));
        assertNull(NBTTags.getById(99));
    }

    @Test
    void testAllTagsHaveUniqueIds()
    {
        // Test all tags have unique IDs
        NBTTags[] allTags = NBTTags.values();
        for (int i = 0; i < allTags.length; i++)
        {
            for (int j = i + 1; j < allTags.length; j++)
            {
                assertNotEquals(allTags[i].getId(), allTags[j].getId());
            }
        }
    }
}