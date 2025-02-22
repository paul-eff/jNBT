package me.paulferlitz;

import me.paulferlitz.IO.NBTReader;
import me.paulferlitz.NBTTags.Tag;
import me.paulferlitz.NBTTags.Tag_Compound;
import me.paulferlitz.NBTTags.Tag_Short;

import java.io.File;
import java.io.IOException;

/**
 * Example class with an example on how the parser can be used.
 *
 * @author Paul Ferlitz
 */
public class Example
{
    // File not included in this library!
    private final static File nbtFile = new File("./src/main/resources/playerTestData.dat");

    public static void main(String[] args)
    {
        // Actual relevant lines of code when using this yourself!
        NBTReader reader = new NBTReader(nbtFile);
        try
        {
            Tag root = reader.read();
            System.out.println(root);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}