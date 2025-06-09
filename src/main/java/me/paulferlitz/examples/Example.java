package me.paulferlitz.examples;

import me.paulferlitz.api.*;
import me.paulferlitz.builder.*;
import me.paulferlitz.core.*;
import me.paulferlitz.io.*;
import me.paulferlitz.util.NBTTags;

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
        try
        {
            // === IMPROVED API EXAMPLE ===
            
            // 1. Simple file reading with convenience method
            ICompoundTag root = NBTFileFactory.readNBTFile(nbtFile);
            System.out.println("Loaded NBT file: " + nbtFile.getName());

            // 2. Access data without casting (NEW!)
            String playerName = root.getString("Name");
            int playerLevel = root.getInt("Level");
            IListTag inventory = root.getList("Inventory");
            
            System.out.printf("Player: %s (Level %d)%n", playerName, playerLevel);

            // 3. Create new structures using builders with I/O integration
            CompoundBuilder playerBuilder = NBTBuilder.compound("Player")
                .addString("Name", "Steve")
                .addInt("Level", 42)
                .addDouble("Health", 20.0);
            
            // Add inventory as a separate step
            playerBuilder.addList("Inventory", NBTTags.Tag_Compound)
                .addCompound("item1")
                    .addString("id", "minecraft:diamond_sword")
                    .addInt("Count", 1)
                .end()
            .end();

            // 4. Build and save directly to file (NEW!)
            File newPlayerFile = new File("./new_player.dat");
            playerBuilder.buildAndSave(newPlayerFile, Compression_Types.GZIP);
            System.out.println("Created new player file: " + newPlayerFile.getName());

            // 5. Load existing file and modify it (NEW!)
            CompoundBuilder modifiedPlayer = NBTBuilder.fromFile(newPlayerFile)
                .addString("LastLogin", "2024-01-01")
                .addInt("Experience", 1000);

            // 6. Save modifications
            modifiedPlayer.buildAndSave(newPlayerFile);
            System.out.println("Updated player file with new data");

            // 7. Quick validation
            if (NBTFileFactory.isValidNBTFile(newPlayerFile))
            {
                System.out.println("File validation: PASSED");
            }

            // 8. Create simple config file
            ICompoundTag config = NBTFactory.createSimpleCompound("Config",
                "serverName", "My Server",
                "maxPlayers", "20",
                "difficulty", "normal"
            );
            NBTFileFactory.writeNBTFile(new File("./config.dat"), config, Compression_Types.NONE);

        } catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}