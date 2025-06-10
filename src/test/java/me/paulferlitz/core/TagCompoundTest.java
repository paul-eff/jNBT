package me.paulferlitz.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class TagCompoundTest {
    
    private Tag_Compound compound;
    
    @BeforeEach
    void setUp() {
        compound = new Tag_Compound("testCompound");
    }
    
    @Test
    void testCreateEmpty() {
        assertEquals("testCompound", compound.getName());
        assertTrue(compound.getData().isEmpty());
    }
    
    @Test
    void testAddStringTag() {
        compound.addString("name", "Steve");
        assertEquals("Steve", compound.getString("name"));
        assertTrue(compound.hasTag("name"));
    }
    
    @Test
    void testAddIntTag() {
        compound.addInt("level", 42);
        assertEquals(42, compound.getInt("level"));
    }
    
    @Test
    void testAddDoubleTag() {
        compound.addDouble("health", 20.5);
        assertEquals(20.5, compound.getDouble("health"));
    }
    
    @Test
    void testAddByteTag() {
        compound.addByte("flag", (byte) 1);
        assertEquals((byte) 1, compound.getByte("flag"));
    }
    
    @Test
    void testGetNonExistentTag() {
        assertNull(compound.getString("missing"));
        assertEquals(0, compound.getInt("missing"));
        assertEquals(0.0, compound.getDouble("missing"));
        assertEquals((byte) 0, compound.getByte("missing"));
        assertFalse(compound.hasTag("missing"));
    }
    
    @Test
    void testFluentChaining() {
        var result = compound.addString("name", "Alice")
                            .addInt("age", 25)
                            .addDouble("score", 99.5);
        
        assertSame(compound, result);
        assertEquals("Alice", compound.getString("name"));
        assertEquals(25, compound.getInt("age"));
        assertEquals(99.5, compound.getDouble("score"));
    }
    
    @Test
    void testAddNestedCompound() {
        Tag_Compound nested = new Tag_Compound("nested");
        nested.addString("inner", "value");
        compound.addTag(nested);
        
        var retrieved = compound.getCompound("nested");
        assertNotNull(retrieved);
        assertEquals("value", retrieved.getString("inner"));
    }
    
    @Test
    void testToString() {
        compound.addString("test", "value");
        String result = compound.toString();
        assertTrue(result.contains("testCompound"));
        assertTrue(result.contains("size=1"));
    }
}