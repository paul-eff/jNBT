package me.paulferlitz.IO;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Class for managing the loading and conversion of a given NBT file to the desired stream.
 *
 * @author Paul Ferlitz
 */
public class NBTFileHandler
{
    /**
     * Method to fully load a NBT file and return it as a {@link DataInputStream}.
     *
     * @param file The target file.
     * @return The file as {@link DataInputStream}.
     * @throws IOException When encountering an error whilst reading the file to a {@link DataInputStream}.
     */
    public static DataInputStream loadNBTToReader(File file) throws IOException
    {
        if (Files.notExists(file.toPath()))
        {
            throw new FileNotFoundException(String.format("The file %s doesn't exist!", file.getPath()));
        }

        InputStream fileStream;

        switch (getCompressionType(file))
        {
            case NONE:
                System.out.printf("The file %s was uncompressed.%n", file.getName(), Compression_Types.GZIP.getName());
                fileStream = new FileInputStream(file);
                break;
            case GZIP:
                System.out.printf("The file %s was compressed with %s, decompressing...%n", file.getName(), Compression_Types.GZIP.getName());
                fileStream = new GZIPInputStream(new FileInputStream(file));
                break;
            default:
                throw new IllegalArgumentException(String.format("The file %s is compressed with an unsupported format!", file.getName()));
        }
        return new DataInputStream(fileStream);
    }

    /**
     * Method to create a {@link DataOutputStream} to later write a NBT file.
     * If the file already exists, a backup will be created.
     *
     * @param file The target file.
     * @param compression The compression type of the file.
     * @return The file as {@link DataOutputStream}.
     * @throws IOException When encountering an error whilst reading the file to a {@link DataOutputStream}.
     */
    public static DataOutputStream loadNBTToWriter(File file, Compression_Types compression) throws IOException
    {
        if (Files.exists(file.toPath()))
        {
            File backupFile = new File(file.getPath() + ".bak");
            Files.copy(file.toPath(), backupFile.toPath());
            System.out.printf("Created backup of file %s%n", file.getName());
        }

        OutputStream fileStream;

        switch (compression)
        {
            case NONE:
                System.out.printf("Compression type for writing %s set to %s%n", file.getName(), Compression_Types.NONE.getName());
                fileStream = new FileOutputStream(file);
                break;
            case GZIP:
                System.out.printf("Compression type for writing %s set to %s%n", file.getName(), Compression_Types.GZIP.getName());
                fileStream = new GZIPOutputStream(new FileOutputStream(file));
                break;
            default:
                throw new IllegalArgumentException(String.format("The compression type %s is not supported!", compression));
        }
        return new DataOutputStream(fileStream);
    }

    /**
     * Method to get the compression type of a file.
     *
     * @param file The target file.
     * @return The compression type of the file.
     */
    public static Compression_Types getCompressionType(File file)
    {
        if (isGzipped(file)) return Compression_Types.GZIP;
        else if (isZlibed(file)) return Compression_Types.ZLIB;
        else if (isLZ4ed(file)) return Compression_Types.LZ4;
        else return Compression_Types.NONE;
    }

    /**
     * Method to check if a file was compressed with Gzip.
     *
     * @param file The target file.
     * @return {@code True} if the file was compressed with Gzip.
     */
    private static boolean isGzipped(File file)
    {
        try
        {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            int magic = raf.read() & 0xff | (raf.read() << 8) & 0xff00;
            raf.close();
            return magic == GZIPInputStream.GZIP_MAGIC;
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method to check if a file was compressed with Zlib.
     *
     * @param file The target file.
     * @return {@code True} if the file was compressed with Zlib.
     */
    private static boolean isZlibed(File file)
    {
        return false;
    }

    /**
     * Method to check if a file was compressed with LZ4.
     *
     * @param file The target file.
     * @return {@code True} if the file was compressed with LZ4.
     */
    private static boolean isLZ4ed(File file)
    {
        return false;
    }
}
