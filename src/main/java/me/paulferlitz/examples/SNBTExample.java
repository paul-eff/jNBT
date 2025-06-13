package me.paulferlitz.examples;

import me.paulferlitz.api.ICompoundTag;
import me.paulferlitz.api.IListTag;
import me.paulferlitz.api.ITag;
import me.paulferlitz.api.NBTFactory;
import me.paulferlitz.formats.snbt.SNBTException;
import me.paulferlitz.util.NBTTags;

import java.io.File;
import java.io.IOException;

/**
 * Demonstrates SNBT (Stringified NBT) parsing and serialization capabilities.
 * Shows conversion between NBT and human-readable string format for debugging and Minecraft commands.
 * <p><b>Key concepts demonstrated:</b></p>
 * <ul>
 *   <li>SNBT parsing with type suffixes (b, s, l, f, d)</li>
 *   <li>Smart string quoting and escape sequences</li>
 *   <li>Array notation ([B;], [I;], [L;]) for typed arrays</li>
 *   <li>Minecraft command NBT generation and parsing</li>
 *   <li>SNBT serialization for Minecraft compatibility</li>
 *   <li>Round-trip conversion verification</li>
 *   <li>Position-aware error handling for debugging</li>
 * </ul>
 * <p><b>SNBT format features:</b></p>
 * <ul>
 *   <li><b>Type suffixes</b> - Explicit type control (42b, 3.14f, 1000l)</li>
 *   <li><b>Smart quoting</b> - Automatic quoting only when necessary</li>
 *   <li><b>Unicode support</b> - Full escape sequence handling</li>
 *   <li><b>Arrays</b> - Typed array notation compatible with Minecraft</li>
 * </ul>
 *
 * @author Paul Ferlitz
 */
public class SNBTExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== SNBT (Stringified NBT) Example ===\n");

        try
        {
            // Example 1: Basic type parsing and serialization
            System.out.println("1. Basic SNBT types:");

            // Parse primitive types with suffixes
            System.out.println("\tParsing: 127b -> " + NBTFactory.parseFromSNBT("127b").getData() + " (Byte)");
            System.out.println("\tParsing: 32767s -> " + NBTFactory.parseFromSNBT("32767s").getData() + " (Short)");
            System.out.println("\tParsing: 2147483647 -> " + NBTFactory.parseFromSNBT("2147483647").getData() + " (Int)");
            System.out.println("\tParsing: 9223372036854775807l -> " + NBTFactory.parseFromSNBT("9223372036854775807l").getData() + " (Long)");
            System.out.println("\tParsing: 3.14159f -> " + NBTFactory.parseFromSNBT("3.14159f").getData() + " (Float)");
            System.out.println("\tParsing: 2.718281828459045 -> " + NBTFactory.parseFromSNBT("2.718281828459045").getData() + " (Double)");

            // Arrays with type prefixes
            byte[] byteArray = (byte[]) NBTFactory.parseFromSNBT("[B;1b,2b,3b]").getData();
            System.out.println("\tByte array [B;1b,2b,3b]: " + byteArray.length + " elements");

            // Collections
            IListTag stringList = NBTFactory.parseListFromSNBT("[apple,banana,cherry]");
            System.out.println("\tString list: " + stringList.getData().size() + " items");

            // Simple compound
            String compound = "{name:Steve,level:42,health:20.0}";
            ICompoundTag player = NBTFactory.parseCompoundFromSNBT(compound);
            System.out.println("\tCompound: " + player.getString("name") + ", level " + player.getInt("level"));

            // Example 2: Minecraft command NBT
            System.out.println("\n2. Minecraft command NBT:");

            // Parse simple item NBT
            String itemNBT = "{display:{Name:Legendary_Sword,Lore:[Powerful_weapon]},Enchantments:[{id:sharpness,lvl:5s}]}";
            ICompoundTag item = NBTFactory.parseCompoundFromSNBT(itemNBT);
            String itemName = item.getCompound("display").getString("Name");
            int enchantCount = item.getList("Enchantments").getData().size();
            System.out.println("\tParsed item: " + itemName + " with " + enchantCount + " enchantments");

            // Generate command NBT for entity data
            ICompoundTag playerData = NBTFactory.createCompound()
                    .addFloat("Health", 20.0f)
                    .addInt("XpLevel", 30);
            String command = NBTFactory.toSNBT(playerData);
            System.out.println("\tGenerated command: /data merge entity @s " + command);

            // Example 3: SNBT serialization
            System.out.println("\n3. SNBT serialization:");

            // Create test data with nested structure
            ICompoundTag config = NBTFactory.createCompound()
                    .addString("name", "Test Server")
                    .addInt("maxPlayers", 100)
                    .addFloat("version", 1.21f);

            IListTag features = NBTFactory.createList("features", NBTTags.Tag_String.getId())
                    .addString("f1", "pvp")
                    .addString("f2", "economy");
            ((me.paulferlitz.core.Collection_Tag) config).addTag(features);

            // Standard SNBT output
            String snbtOutput = NBTFactory.toSNBT(config);
            System.out.println("\tSNBT: " + snbtOutput);

            // Example 4: File operations
            System.out.println("\n4. File operations:");

            // Create test configuration data
            ICompoundTag serverConfig = NBTFactory.createCompound()
                    .addString("serverName", "My Server")
                    .addInt("maxPlayers", 50)
                    .addString("difficulty", "normal");

            // Write to SNBT file
            File configFile = new File("server.snbt");
            NBTFactory.writeToSNBTFile(serverConfig, configFile);
            System.out.println("\tWrote config to: " + configFile.getName() + " (" + configFile.length() + " bytes)");

            // Read back and verify data integrity
            String fileContent = java.nio.file.Files.readString(configFile.toPath());
            ICompoundTag loaded = NBTFactory.parseCompoundFromSNBT(fileContent);
            System.out.println("\tLoaded server: " + loaded.getString("serverName") +
                    " (" + loaded.getInt("maxPlayers") + " players)");

            // Verify round-trip conversion preserves data
            String original = NBTFactory.toSNBT(serverConfig);
            String roundTrip = NBTFactory.toSNBT(NBTFactory.parseFromSNBT(original));
            boolean success = original.equals(roundTrip);
            System.out.println("\tRound-trip test: " + (success ? "✓ PASSED" : "✗ FAILED"));

            // Cleanup
            boolean deleted = configFile.delete();
            System.out.println("\tCleanup: " + (deleted ? "success" : "failed"));

            // Example 5: Error handling and debugging
            System.out.println("\n5. Error handling:");

            // Test cases with various syntax errors
            String[] testCases = {
                    "{unclosed_brace: value", // Missing closing brace
                    "[1, 2, mixed_types]", // Mixed types in list
                    "{key: 42invalid_suffix}", // Invalid numeric suffix
                    "[I; 1, 2, 3.14]" // Mixed types in list
            };

            System.out.println("\tTesting malformed SNBT strings:");
            for (String snbt : testCases)
            {
                try
                {
                    ITag<?> result = NBTFactory.parseFromSNBT(snbt);
                    System.out.println("\t\t✓ Parsed: " + snbt);
                } catch (SNBTException e)
                {
                    String errorType = e.getMessage().split(":")[0];
                    System.out.println("\t\t✗ Error: " + snbt + " (" + errorType + ")");
                }
            }

            // Demonstrate detailed position tracking for debugging
            System.out.println("\n\tPosition tracking example:");
            try
            {
                NBTFactory.parseFromSNBT("{valid: start, broken syntax here}");
            } catch (SNBTException e)
            {
                System.out.println("\t\tError at character " + e.getPosition() + ": " + e.getMessage().split(":")[0]);
            }

        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
    }
}