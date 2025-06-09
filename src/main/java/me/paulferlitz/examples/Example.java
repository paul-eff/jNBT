package me.paulferlitz.examples;

import me.paulferlitz.api.*;
import me.paulferlitz.core.*;
import me.paulferlitz.io.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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
        INBTReader reader;
        INBTWriter writer;
        try
        {
            // === Creating a reader ===
            reader = NBTFileFactory.createReader(nbtFile);

            // Fetch and print the whole NBT file
            ICompoundTag root = reader.read();
            System.out.println(root);

            // Fetch and print the "Inventory" list
            // Currently it doesn't matter if you use Tag or Tag<?>. The latter generates less warning though.
            ITag<?> inventory = ((Tag_Compound) root).getTagByName("Inventory");
            System.out.println(inventory);

            // Add a new item to the "Inventory" list
            ICompoundTag diamondHoe = NBTFactory.createCompound();
            diamondHoe.addInt("count", 1);
            diamondHoe.addDouble("Slot", 1.0);
            diamondHoe.addString("id", "minecraft:diamond_hoe");

            // Remove an item from the "Inventory" list - accessing the inventory's data directly (getData array)
            ((Collection_Tag) inventory).getData().remove(1);
            // Remove an item from the "Inventory" list - accessing the inventory's data directly (iterator)
            Iterator<Tag<?>> iterator = ((Collection_Tag) inventory).getData().iterator();
            while (iterator.hasNext()) {
                Tag<?> tag = iterator.next();
                if (tag instanceof Tag_Compound invItem) {
                    if (invItem.getTagByName("id").getData().equals("minecraft:diamond_hoe")) {
                        iterator.remove();
                        break;
                    }
                }
            }
            // Remove an element ("Inventory") from the root - using built in remover
            ((Tag_Compound) root).removeTag(((Tag_Compound) root).getTagByName("Inventory"));

            // === Creating a writer ===
            // If you are overwriting a file, the writer will determine the compression type automatically
            writer = NBTFileFactory.createWriter(nbtFile);
            // In any other case you can specify the compression type
            writer = NBTFileFactory.createWriter(nbtFile, Compression_Types.GZIP);

            // Write the root back to the file
            writer.write(root);

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}