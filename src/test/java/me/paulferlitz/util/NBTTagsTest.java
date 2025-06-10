package me.paulferlitz.util;

import me.paulferlitz.core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NBTTagsTest {
    
    @Test
    void testTagIds() {
        assertEquals(0, NBTTags.Tag_End.getId());
        assertEquals(1, NBTTags.Tag_Byte.getId());
        assertEquals(2, NBTTags.Tag_Short.getId());
        assertEquals(3, NBTTags.Tag_Int.getId());
        assertEquals(4, NBTTags.Tag_Long.getId());
        assertEquals(5, NBTTags.Tag_Float.getId());
        assertEquals(6, NBTTags.Tag_Double.getId());
        assertEquals(7, NBTTags.Tag_Byte_Array.getId());
        assertEquals(8, NBTTags.Tag_String.getId());
        assertEquals(9, NBTTags.Tag_List.getId());
        assertEquals(10, NBTTags.Tag_Compound.getId());
        assertEquals(11, NBTTags.Tag_Int_Array.getId());
        assertEquals(12, NBTTags.Tag_Long_Array.getId());
    }
    
    @Test
    void testTagNames() {
        assertEquals("Tag_End", NBTTags.Tag_End.getName());
        assertEquals("Tag_Byte", NBTTags.Tag_Byte.getName());
        assertEquals("Tag_Short", NBTTags.Tag_Short.getName());
        assertEquals("Tag_Int", NBTTags.Tag_Int.getName());
        assertEquals("Tag_Long", NBTTags.Tag_Long.getName());
        assertEquals("Tag_Float", NBTTags.Tag_Float.getName());
        assertEquals("Tag_Double", NBTTags.Tag_Double.getName());
        assertEquals("Tag_Byte_Array", NBTTags.Tag_Byte_Array.getName());
        assertEquals("Tag_String", NBTTags.Tag_String.getName());
        assertEquals("Tag_List", NBTTags.Tag_List.getName());
        assertEquals("Tag_Compound", NBTTags.Tag_Compound.getName());
        assertEquals("Tag_Int_Array", NBTTags.Tag_Int_Array.getName());
        assertEquals("Tag_Long_Array", NBTTags.Tag_Long_Array.getName());
    }
    
    @Test
    void testEnumValues() {
        // Test that all expected enum values exist
        NBTTags[] allTags = NBTTags.values();
        assertEquals(13, allTags.length);
        
        // Test that we can access all expected tags
        assertNotNull(NBTTags.Tag_End);
        assertNotNull(NBTTags.Tag_Byte);
        assertNotNull(NBTTags.Tag_Short);
        assertNotNull(NBTTags.Tag_Int);
        assertNotNull(NBTTags.Tag_Long);
        assertNotNull(NBTTags.Tag_Float);
        assertNotNull(NBTTags.Tag_Double);
        assertNotNull(NBTTags.Tag_Byte_Array);
        assertNotNull(NBTTags.Tag_String);
        assertNotNull(NBTTags.Tag_List);
        assertNotNull(NBTTags.Tag_Compound);
        assertNotNull(NBTTags.Tag_Int_Array);
        assertNotNull(NBTTags.Tag_Long_Array);
    }
    
    @Test
    void testGetById() {
        assertEquals(NBTTags.Tag_End, NBTTags.getById(0));
        assertEquals(NBTTags.Tag_Byte, NBTTags.getById(1));
        assertEquals(NBTTags.Tag_Short, NBTTags.getById(2));
        assertEquals(NBTTags.Tag_Int, NBTTags.getById(3));
        assertEquals(NBTTags.Tag_Long, NBTTags.getById(4));
        assertEquals(NBTTags.Tag_Float, NBTTags.getById(5));
        assertEquals(NBTTags.Tag_Double, NBTTags.getById(6));
        assertEquals(NBTTags.Tag_Byte_Array, NBTTags.getById(7));
        assertEquals(NBTTags.Tag_String, NBTTags.getById(8));
        assertEquals(NBTTags.Tag_List, NBTTags.getById(9));
        assertEquals(NBTTags.Tag_Compound, NBTTags.getById(10));
        assertEquals(NBTTags.Tag_Int_Array, NBTTags.getById(11));
        assertEquals(NBTTags.Tag_Long_Array, NBTTags.getById(12));
    }
    
    @Test
    void testInvalidId() {
        assertNull(NBTTags.getById(-1));
        assertNull(NBTTags.getById(99));
    }
    
    @Test
    void testAllTagsHaveUniqueIds() {
        NBTTags[] allTags = NBTTags.values();
        for (int i = 0; i < allTags.length; i++) {
            for (int j = i + 1; j < allTags.length; j++) {
                assertNotEquals(allTags[i].getId(), allTags[j].getId(),
                    "Tags " + allTags[i] + " and " + allTags[j] + " have same ID");
            }
        }
    }
}