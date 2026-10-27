import de.pauleff.jnbt.api.*;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.formats.binary.NBTFileHandler;

import java.io.File;
import java.io.IOException;

public class CompressionAndIOExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== Compression and I/O Example ===\n");

        ICompoundTag testData = NBTFactory.createCompound("TestData")
                .addString("name", "CompressionTest")
                .addInt("size", 1024)
                .addDouble("ratio", 0.75);

        File gzipFile = new File("./test_gzip.dat");
        File zlibFile = new File("./test_zlib.dat");
        File noneFile = new File("./test_none.dat");

        // Pass a compression type when creating a file. LZ4 is not supported.
        NBTFileFactory.writeNBTFile(gzipFile, testData, Compression_Types.GZIP);
        NBTFileFactory.writeNBTFile(zlibFile, testData, Compression_Types.ZLIB);
        NBTFileFactory.writeNBTFile(noneFile, testData, Compression_Types.NONE);

        System.out.println("File sizes:");
        System.out.println("  GZIP: " + gzipFile.length() + " bytes");
        System.out.println("  ZLIB: " + zlibFile.length() + " bytes");
        System.out.println("  NONE: " + noneFile.length() + " bytes");

        // Readers detect compression from the file header.
        Compression_Types gzipDetected = NBTFileHandler.getCompressionType(gzipFile);
        Compression_Types zlibDetected = NBTFileHandler.getCompressionType(zlibFile);
        Compression_Types noneDetected = NBTFileHandler.getCompressionType(noneFile);

        System.out.println("\nDetected compression:");
        System.out.println("  GZIP file: " + gzipDetected.getName());
        System.out.println("  ZLIB file: " + zlibDetected.getName());
        System.out.println("  NONE file: " + noneDetected.getName());

        System.out.println("\nManual I/O with resource management:");

        try (INBTReader reader = NBTFileFactory.createReader(gzipFile))
        {
            ICompoundTag loadedData = reader.read();
            System.out.println("Manually loaded: " + loadedData.getString("name"));
        }

        try (INBTWriter writer = NBTFileFactory.createWriter(new File("./manual_test.dat"), Compression_Types.GZIP))
        {
            writer.write(testData);
            System.out.println("Manually written file");
        }

        System.out.println("\nFile validation:");
        System.out.println("  GZIP file valid: " + NBTFileFactory.isValidNBTFile(gzipFile));
        System.out.println("  ZLIB file valid: " + NBTFileFactory.isValidNBTFile(zlibFile));
        System.out.println("  NONE file valid: " + NBTFileFactory.isValidNBTFile(noneFile));

        System.out.println("\nCompression preservation:");
        ICompoundTag modifiedData = NBTFileFactory.readNBTFile(gzipFile);
        modifiedData.addString("modified", "yes");

        // Without an explicit type, writing keeps the existing file compression.
        NBTFileFactory.writeNBTFile(gzipFile, modifiedData);
        Compression_Types afterModification = NBTFileHandler.getCompressionType(gzipFile);
        System.out.println("  After modification: " + afterModification.getName());

        // Opening a missing file wraps the I/O error in a RuntimeException.
        File invalidFile = new File("./invalid.dat");
        try
        {
            NBTFileFactory.readNBTFile(invalidFile);
            System.out.println("Should not reach here");
        } catch (RuntimeException e)
        {
            System.out.println("  Correctly caught error: " + e.getMessage().substring(0, Math.min(50, e.getMessage().length())) + "...");
        }

        gzipFile.delete();
        zlibFile.delete();
        noneFile.delete();
        new File("./manual_test.dat").delete();
        System.out.println("Cleanup completed");
    }
}
