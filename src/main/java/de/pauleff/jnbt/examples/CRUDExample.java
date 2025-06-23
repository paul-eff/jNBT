package de.pauleff.jnbt.examples;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.builder.NBTBuilder;
import de.pauleff.jnbt.util.NBTTags;

/**
 * Simple demonstration of CRUD (Create, Read, Update, Delete) operations in jNBT.
 *
 * @author Paul Ferlitz
 */
public class CRUDExample
{
    public static void main(String[] args)
    {
        System.out.println("=== jNBT CRUD Example ===\n");

        // CREATE - Build a player with inventory
        ICompoundTag player = NBTBuilder.compound("Player")
            .addString("name", "Steve")
            .addInt("level", 1)
            .addList("inventory", NBTTags.Tag_String)
                .addString("slot1", "sword")
                .addString("slot2", "potion")
            .endCompound()
            .build();

        // READ - Get values
        System.out.println("Player: " + player.getString("name"));
        System.out.println("Level: " + player.getInt("level"));
        System.out.println("Items: " + player.getList("inventory").size());

        // UPDATE - Change values
        player.setString("name", "Yo Mama")
              .setInt("level", 50);

        IListTag inventory = player.getList("inventory");
        inventory.setString(0, "slot1", "diamond_sword");

        System.out.println("\nAfter updates:");
        System.out.println("Player: " + player.getString("name"));
        System.out.println("Level: " + player.getInt("level"));
        System.out.println("First item: " + inventory.get(0));

        // DELETE - Remove items
        player.removeTag("level");
        inventory.removeAt(1);

        System.out.println("\nAfter deletions:");
        System.out.println("Level exists: " + player.hasTag("level"));
        System.out.println("Items left: " + inventory.size());

        System.out.println("\n=== Complete ===");
    }
}