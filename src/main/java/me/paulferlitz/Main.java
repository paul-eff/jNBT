package me.paulferlitz;

import me.paulferlitz.IO.NBTReader;

import java.io.File;
import java.io.IOException;

public class Main
{
    private final static File nbtFile = new File("./src/main/resources/playerTestData.dat");

    public static void main(String[] args)
    {
        System.out.println("Loading jNBT...");

        NBTReader reader = new NBTReader(nbtFile);
        try {
            System.out.println(reader.readNBTTag(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}