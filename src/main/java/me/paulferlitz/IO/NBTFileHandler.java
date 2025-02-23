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
    public static DataInputStream laodFileToStream(File file) throws IOException
    {
        InputStream fileStream;

        if (Files.notExists(file.toPath()))
        {
            throw new FileNotFoundException(String.format("The path %s doesn't exist!", file.getPath()));
        }

        if (isGzipped(file))
        {
            System.out.printf("The file %s was compressed with gzip, decompressing...%n", file.getName());
            fileStream = new GZIPInputStream(new FileInputStream(file));
        } else
        {
            // TODO: Implement zlib (aka DEFLATE) check and decompression
            fileStream = new FileInputStream(file);
        }

        return new DataInputStream(fileStream);
    }

    public static DataOutputStream targetFileToStream(File file) throws IOException {
        OutputStream fileStream;

        if (Files.notExists(file.toPath()))
        {
            throw new FileNotFoundException(String.format("The path %s doesn't exist!", file.getPath()));
        }

        if (isGzipped(file) && false)
        {
            file = new File("./src/main/resources/playerdata-new.dat");
            // TODO: Correctly implement GZIP and co.
            System.out.printf("The file %s was compressed with gzip, compressing...%n", file.getName());
            fileStream = new GZIPOutputStream(new FileOutputStream(file));
        } else
        {
            // TODO: Implement zlib (aka DEFLATE) check and decompression
            fileStream = new FileOutputStream(file);
        }
        return new DataOutputStream(fileStream);
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
}
