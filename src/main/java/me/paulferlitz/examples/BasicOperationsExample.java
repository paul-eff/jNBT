package me.paulferlitz.examples;

import me.paulferlitz.api.*;
import me.paulferlitz.util.NBTTags;

import java.io.File;
import java.io.IOException;

/**
 * Demonstrates basic NBT operations using jNBT library.
 * Shows fundamental tag creation, compound operations, and file I/O.
 * Core concepts:
 * - NBTFactory: Primary API for creating tags
 * - NBTFileFactory: File operations with automatic compression
 * - Type-safe getters: Access data without casting
 * - Fluent API: Method chaining for readable code
 * @author Paul Ferlitz
 */
public class BasicOperationsExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== Basic NBT Operations ===\n");
        
        // Create different tag types
        ITag<String> name = NBTFactory.createString("playerName", "Steve");
        ITag<Integer> level = NBTFactory.createInt("level", 42);
        ITag<Double> health = NBTFactory.createDouble("health", 20.0);
        
        System.out.println("Created tags:");
        System.out.println("\t" + name);
        System.out.println("\t" + level);
        System.out.println("\t" + health);
        
        // Create compound tag with fluent API
        ICompoundTag player = NBTFactory.createCompound("Player")
            .addString("name", "Steve")
            .addInt("level", 42)
            .addDouble("health", 20.0)
            .addByte("gameMode", (byte) 1);
        
        System.out.println("\nCompound tag created with " + player.getData().size() + " fields");
        
        // Type-safe data access
        String playerName = player.getString("name");
        int playerLevel = player.getInt("level");
        double playerHealth = player.getDouble("health");
        
        System.out.println("Player: " + playerName + " (Level " + playerLevel + ", Health " + playerHealth + ")");
        
        // Demonstrate type safety - wrong type returns default
        int nameAsInt = player.getInt("name"); // String accessed as int
        String missingField = player.getString("missing"); // Non-existent field
        
        System.out.println("Type safety: name as int = " + nameAsInt + ", missing field = " + missingField);
        
        // Create and use list
        IListTag inventory = NBTFactory.createList("inventory", NBTTags.Tag_String.getId());
        inventory.addString("item1", "sword")
                .addString("item2", "shield");
        
        System.out.println("Inventory list type: " + inventory.getListTypeID() + " (size: " + inventory.getData().size() + ")");
        
        // File operations
        File testFile = new File("./basic_test.dat");
        
        // Write to file
        NBTFileFactory.writeNBTFile(testFile, player);
        System.out.println("Wrote player data to file (" + testFile.length() + " bytes)");
        
        // Read from file
        ICompoundTag loadedPlayer = NBTFileFactory.readNBTFile(testFile);
        System.out.println("Loaded player: " + loadedPlayer.getString("name"));
        
        // Cleanup
        boolean success = testFile.delete();
        System.out.printf("Cleanup %s%n", success ? "succeeded" : "failed");
    }
}