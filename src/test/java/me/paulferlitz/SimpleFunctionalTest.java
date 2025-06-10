package me.paulferlitz;

import me.paulferlitz.api.*;
import me.paulferlitz.builder.NBTBuilder;
import me.paulferlitz.io.Compression_Types;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Simple functional tests covering the main API without getting into complex details.
 */
class SimpleFunctionalTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testBasicCompoundOperations() {
        // Test compound creation and basic operations
        ICompoundTag compound = NBTBuilder.compound("test")
            .addString("name", "value")
            .addInt("number", 42)
            .addDouble("decimal", 3.14)
            .build();
        
        assertEquals("test", compound.getName());
        assertEquals("value", compound.getString("name"));
        assertEquals(42, compound.getInt("number"));
        assertEquals(3.14, compound.getDouble("decimal"));
        assertTrue(compound.hasTag("name"));
        assertFalse(compound.hasTag("missing"));
    }
    
    @Test
    void testNestedCompounds() {
        ICompoundTag root = NBTBuilder.compound("root")
            .addString("rootField", "rootValue")
            .addCompound("nested")
                .addString("nestedField", "nestedValue")
                .addInt("nestedNumber", 99)
            .endCompound()
            .build();
        
        assertEquals("rootValue", root.getString("rootField"));
        ICompoundTag nested = root.getCompound("nested");
        assertNotNull(nested);
        assertEquals("nestedValue", nested.getString("nestedField"));
        assertEquals(99, nested.getInt("nestedNumber"));
    }
    
    @Test
    void testFactoryMethods() {
        // Test factory pattern
        ICompoundTag compound = NBTFactory.createCompound("factory");
        compound.addString("test", "value");
        assertEquals("value", compound.getString("test"));
        
        ITag<String> stringTag = NBTFactory.createString("name", "value");
        assertEquals("name", stringTag.getName());
        assertEquals("value", stringTag.getData());
        
        ITag<Integer> intTag = NBTFactory.createInt("number", 42);
        assertEquals("number", intTag.getName());
        assertEquals(42, intTag.getData());
    }
    
    @Test
    void testFileIOBasic() throws IOException {
        // Test basic file operations
        ICompoundTag original = NBTBuilder.compound("FileTest")
            .addString("name", "TestFile")
            .addInt("version", 1)
            .addDouble("value", 99.5)
            .build();
        
        File testFile = tempDir.resolve("test.dat").toFile();
        
        // Test uncompressed
        NBTFileFactory.writeNBTFile(testFile, original, Compression_Types.NONE);
        assertTrue(testFile.exists());
        
        ICompoundTag loaded = NBTFileFactory.readNBTFile(testFile);
        assertEquals("FileTest", loaded.getName());
        assertEquals("TestFile", loaded.getString("name"));
        assertEquals(1, loaded.getInt("version"));
        assertEquals(99.5, loaded.getDouble("value"));
    }
    
    @Test
    void testCompressionTypes() {
        // Test that compression types exist
        assertNotNull(Compression_Types.NONE);
        assertNotNull(Compression_Types.GZIP);
        assertNotNull(Compression_Types.ZLIB);
        assertNotNull(Compression_Types.LZ4);
        
        assertEquals("NONE", Compression_Types.NONE.getName());
        assertEquals("GZIP", Compression_Types.GZIP.getName());
        assertEquals("ZLIB", Compression_Types.ZLIB.getName());
        assertEquals("LZ4", Compression_Types.LZ4.getName());
    }
    
    @Test
    void testArrayTypes() {
        // Test array creation
        byte[] byteArray = {1, 2, 3, 4};
        int[] intArray = {10, 20, 30};
        long[] longArray = {100L, 200L, 300L};
        
        ITag<byte[]> byteTag = NBTFactory.createByteArray("bytes", byteArray);
        ITag<int[]> intTag = NBTFactory.createIntArray("ints", intArray);
        ITag<long[]> longTag = NBTFactory.createLongArray("longs", longArray);
        
        assertArrayEquals(byteArray, byteTag.getData());
        assertArrayEquals(intArray, intTag.getData());
        assertArrayEquals(longArray, longTag.getData());
    }
    
    @Test
    void testErrorHandling() {
        // Test error cases
        ICompoundTag compound = NBTBuilder.compound("test").build();
        
        // Non-existent tags should return null/default values
        assertNull(compound.getString("missing"));
        assertEquals(0, compound.getInt("missing"));
        assertEquals(0.0, compound.getDouble("missing"));
        assertEquals((byte) 0, compound.getByte("missing"));
        assertNull(compound.getCompound("missing"));
        assertNull(compound.getList("missing"));
    }
    
    @Test
    void testFluentAPI() {
        // Test fluent chaining
        ICompoundTag result = NBTBuilder.compound("fluent")
            .addString("first", "value1")
            .addInt("second", 10)
            .addDouble("third", 1.5)
            .addByte("fourth", (byte) 1)
            .addShort("fifth", (short) 256)
            .addLong("sixth", 123456L)
            .addFloat("seventh", 2.5f)
            .build();
        
        assertEquals("value1", result.getString("first"));
        assertEquals(10, result.getInt("second"));
        assertEquals(1.5, result.getDouble("third"));
        assertEquals((byte) 1, result.getByte("fourth"));
    }
}