package me.paulferlitz.IO;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

public class NBTFileHandler
{
    public static DataInputStream laodFileToStream(File file) throws IOException
    {
        InputStream fileStream;

        if (Files.notExists(file.toPath()))
        {
            throw new FileNotFoundException(String.format("The path %s doesn't exist!", file.getPath()));
        }

        if (isGzipped(file))
        {
            System.out.println(String.format("The file %s was compressed with gzip, decompressing...", file.getName()));
            fileStream = new GZIPInputStream(new FileInputStream(file));
        } else
        {
            // TODO: Implement zlib (aka DEFLATE) check and decompression
            fileStream = new FileInputStream(file);
        }

        return new DataInputStream(fileStream);
    }

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
