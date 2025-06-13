package de.pauleff.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagCompoundTest
{

    private Tag_Compound compound;

    @BeforeEach
    void setUp()
    {
        compound = new Tag_Compound("testCompound");
    }

    @Test
    void testCreateEmpty()
    {
        // Test empty compound creation
        assertEquals("testCompound", compound.getName());
        assertTrue(compound.getData().isEmpty());
    }

    @Test
    void testAddStringTag()
    {
        // Test adding string tag
        compound.addString("name", "Steve");
        assertEquals("Steve", compound.getString("name"));
        assertTrue(compound.hasTag("name"));
    }

    @Test
    void testAddIntTag()
    {
        // Test adding int tag
        compound.addInt("level", 42);
        assertEquals(42, compound.getInt("level"));
    }

    @Test
    void testAddDoubleTag()
    {
        // Test adding double tag
        compound.addDouble("health", 20.5);
        assertEquals(20.5, compound.getDouble("health"));
    }

    @Test
    void testAddByteTag()
    {
        // Test adding byte tag
        compound.addByte("flag", (byte) 1);
        assertEquals((byte) 1, compound.getByte("flag"));
    }

    @Test
    void testGetNonExistentTag()
    {
        // Test default values for missing tags
        assertNull(compound.getString("missing"));
        assertEquals(0, compound.getInt("missing"));
        assertEquals(0.0, compound.getDouble("missing"));
        assertFalse(compound.hasTag("missing"));
    }

    @Test
    void testFluentChaining()
    {
        // Test fluent method chaining
        var result = compound.addString("name", "Alice")
                .addInt("age", 25);

        assertSame(compound, result);
        assertEquals("Alice", compound.getString("name"));
        assertEquals(25, compound.getInt("age"));
    }

    @Test
    void testAddNestedCompound()
    {
        // Test nested compound tags
        Tag_Compound nested = new Tag_Compound("nested");
        nested.addString("inner", "value");
        compound.addTag(nested);

        var retrieved = compound.getCompound("nested");
        assertNotNull(retrieved);
        assertEquals("value", retrieved.getString("inner"));
    }

    @Test
    void testToString()
    {
        // Test string representation
        compound.addString("test", "value");
        String result = compound.toString();
        assertTrue(result.contains("testCompound"));
        assertTrue(result.contains("size=1"));
    }
}