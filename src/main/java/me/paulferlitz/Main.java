package me.paulferlitz;

import me.paulferlitz.Helpers.FileHelper;

import java.io.File;
import java.io.IOException;

public class Main
{
    private final static File nbtFile = new File("./src/main/resources/playerTestData.dat");

    public static void main(String[] args)
    {
        System.out.println("Loading jNBT...");

        try
        {
            byte[] nbtByteArray = FileHelper.loadNBTFileToBytes(nbtFile);

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}