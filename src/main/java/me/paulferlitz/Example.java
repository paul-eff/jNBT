package me.paulferlitz;

import me.paulferlitz.IO.NBTReader;
import me.paulferlitz.NBTTags.*;

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
        NBTReader reader = new NBTReader(nbtFile);
        try
        {
            // Fetch and print the whole NBT file
            Collection_Tag root = reader.read();
            System.out.println(root);

            // Fetch and print the "Inventory" list
            // Currently it doesn't matter if you use Tag or Tag<?>. The latter generates less warning though.
            Tag<?> inventory = root.getTagByName("Inventory");
            System.out.println(inventory);

            // Add a new item to the "Inventory" list
            Tag_Compound diamondHoe = new Tag_Compound();
            diamondHoe.addTag(new Tag_Int("count", 1));
            diamondHoe.addTag(new Tag_Double("Slot", 1.0));
            diamondHoe.addTag(new Tag_String("id", "minecraft:diamond_hoe"));
            ((Collection_Tag) inventory).addTag(diamondHoe);

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
            root.removeTag(root.getTagByName("Inventory"));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}