package me.paulferlitz.Helpers;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.GZIPInputStream;

public class FileHelper
{
    public static byte[] loadNBTFileToBytes(File file) throws IOException
    {
        if (Files.notExists(file.toPath())) {
            throw new FileNotFoundException(String.format("The path %s doesn't exist!", file.getPath()));
        }

        if (isGzipped(file))
        {
            System.out.println(String.format("The file %s was compressed with gzip, decompressing...", file.getName()));
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(file)))
            {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) > 0)
                {
                    output.write(buffer, 0, len);
                }
            }
            return output.toByteArray();
        }

        // TODO: Implement zlib (aka DEFLATE) check and decompression

        return Files.readAllBytes(file.toPath());
    }

    private static boolean isGzipped(File file)
    {
        try {
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
