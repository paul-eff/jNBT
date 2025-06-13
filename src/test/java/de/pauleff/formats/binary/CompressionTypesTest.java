package de.pauleff.formats.binary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompressionTypesTest
{

    @Test
    void testCompressionTypes()
    {
        // Test compression type names
        assertEquals("GZIP", Compression_Types.GZIP.name());
        assertEquals("ZLIB", Compression_Types.ZLIB.name());
        assertEquals("NONE", Compression_Types.NONE.name());
    }

    @Test
    void testAllTypesPresent()
    {
        // Test all compression types exist
        Compression_Types[] types = Compression_Types.values();
        assertEquals(4, types.length);

        boolean hasGzip = false, hasNone = false;
        for (Compression_Types type : types)
        {
            if (type == Compression_Types.GZIP) hasGzip = true;
            if (type == Compression_Types.NONE) hasNone = true;
        }

        assertTrue(hasGzip && hasNone);
    }

    @Test
    void testValueOf()
    {
        // Test enum valueOf method
        assertEquals(Compression_Types.GZIP, Compression_Types.valueOf("GZIP"));
        assertEquals(Compression_Types.NONE, Compression_Types.valueOf("NONE"));
    }
}