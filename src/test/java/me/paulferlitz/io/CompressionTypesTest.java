package me.paulferlitz.io;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompressionTypesTest {
    
    @Test
    void testCompressionTypes() {
        assertEquals("GZIP", Compression_Types.GZIP.name());
        assertEquals("ZLIB", Compression_Types.ZLIB.name());
        assertEquals("NONE", Compression_Types.NONE.name());
    }
    
    @Test
    void testAllTypesPresent() {
        Compression_Types[] types = Compression_Types.values();
        assertEquals(4, types.length);
        
        boolean hasGzip = false, hasZlib = false, hasNone = false, hasLz4 = false;
        for (Compression_Types type : types) {
            switch (type) {
                case GZIP -> hasGzip = true;
                case ZLIB -> hasZlib = true;
                case NONE -> hasNone = true;
                case LZ4 -> hasLz4 = true;
            }
        }
        
        assertTrue(hasGzip && hasZlib && hasNone && hasLz4);
    }
    
    @Test
    void testValueOf() {
        assertEquals(Compression_Types.GZIP, Compression_Types.valueOf("GZIP"));
        assertEquals(Compression_Types.ZLIB, Compression_Types.valueOf("ZLIB"));
        assertEquals(Compression_Types.NONE, Compression_Types.valueOf("NONE"));
    }
}