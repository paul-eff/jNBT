package me.paulferlitz.examples;

import me.paulferlitz.api.*;
import me.paulferlitz.formats.binary.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Demonstrates compression handling and advanced I/O operations.
 * Key concepts:
 * - Compression formats: GZIP, ZLIB, NONE
 * - Automatic compression detection
 * - Manual resource management with try-with-resources
 * - File validation and error handling
 * Most operations use NBTFileFactory convenience methods.
 * Use manual readers/writers for custom error handling.
 * @author Paul Ferlitz
 */
public class CompressionAndIOExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== Compression and I/O Example ===\n");
        
        // Create test data
        ICompoundTag testData = NBTFactory.createCompound("TestData")
            .addString("name", "CompressionTest")
            .addInt("size", 1024)
            .addDouble("ratio", 0.75);
        
        // Test different compression formats
        File gzipFile = new File("./test_gzip.dat");
        File zlibFile = new File("./test_zlib.dat");
        File noneFile = new File("./test_none.dat");
        
        // Write with different compressions
        NBTFileFactory.writeNBTFile(gzipFile, testData, Compression_Types.GZIP);
        NBTFileFactory.writeNBTFile(zlibFile, testData, Compression_Types.ZLIB);
        NBTFileFactory.writeNBTFile(noneFile, testData, Compression_Types.NONE);
        
        System.out.println("File sizes:");
        System.out.println("  GZIP: " + gzipFile.length() + " bytes");
        System.out.println("  ZLIB: " + zlibFile.length() + " bytes");
        System.out.println("  NONE: " + noneFile.length() + " bytes");
        
        // Automatic compression detection
        Compression_Types gzipDetected = NBTFileHandler.getCompressionType(gzipFile);
        Compression_Types zlibDetected = NBTFileHandler.getCompressionType(zlibFile);
        Compression_Types noneDetected = NBTFileHandler.getCompressionType(noneFile);
        
        System.out.println("\nDetected compression:");
        System.out.println("  GZIP file: " + gzipDetected.getName());
        System.out.println("  ZLIB file: " + zlibDetected.getName());
        System.out.println("  NONE file: " + noneDetected.getName());
        
        // Manual resource management
        System.out.println("\nManual I/O with resource management:");
        
        try (INBTReader reader = NBTFileFactory.createReader(gzipFile)) {
            ICompoundTag loadedData = reader.read();
            System.out.println("Manually loaded: " + loadedData.getString("name"));
        } // Automatically closed
        
        try (INBTWriter writer = NBTFileFactory.createWriter(new File("./manual_test.dat"), Compression_Types.GZIP)) {
            writer.write(testData);
            System.out.println("Manually written file");
        } // Automatically closed
        
        // File validation
        System.out.println("\nFile validation:");
        System.out.println("  GZIP file valid: " + NBTFileFactory.isValidNBTFile(gzipFile));
        System.out.println("  ZLIB file valid: " + NBTFileFactory.isValidNBTFile(zlibFile));
        System.out.println("  NONE file valid: " + NBTFileFactory.isValidNBTFile(noneFile));
        
        // Compression preservation
        System.out.println("\nCompression preservation:");
        ICompoundTag modifiedData = NBTFileFactory.readNBTFile(gzipFile);
        modifiedData.addString("modified", "yes");
        
        // Write back - compression should be preserved
        NBTFileFactory.writeNBTFile(gzipFile, modifiedData);
        Compression_Types afterModification = NBTFileHandler.getCompressionType(gzipFile);
        System.out.println("  After modification: " + afterModification.getName());
        
        // Error handling example
        File invalidFile = new File("./invalid.dat");
        try {
            NBTFileFactory.readNBTFile(invalidFile);
            System.out.println("Should not reach here");
        } catch (RuntimeException e) {
            System.out.println("  Correctly caught error: " + e.getMessage().substring(0, Math.min(50, e.getMessage().length())) + "...");
        }
        
        // Cleanup
        gzipFile.delete();
        zlibFile.delete();
        noneFile.delete();
        new File("./manual_test.dat").delete();
        System.out.println("Cleanup completed");
    }
}