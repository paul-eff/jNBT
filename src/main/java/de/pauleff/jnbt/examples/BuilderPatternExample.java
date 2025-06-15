package de.pauleff.jnbt.examples;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFileFactory;
import de.pauleff.jnbt.builder.ListBuilder;
import de.pauleff.jnbt.builder.NBTBuilder;
import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.formats.binary.Compression_Types;
import de.pauleff.jnbt.util.NBTTags;

import java.io.File;
import java.io.IOException;

/**
 * Demonstrates the NBT builder pattern for creating complex structures.
 * Shows different approaches to handle nested structures and when to use each method.
 * <p><b>Key concepts demonstrated:</b></p>
 * <ul>
 *   <li>Simple compound building with terminal operations</li>
 *   <li>Single-level nesting where {@code end()} is safe and simple</li>
 *   <li>Complex chaining requiring type-safe {@code endCompound()}/{@code endList()}</li>
 *   <li>Variable storage pattern as alternative to complex chaining</li>
 *   <li>File integration and modification patterns</li>
 * </ul>
 * <p><b>When to use each approach:</b></p>
 * <ul>
 *   <li><b>end()</b> - Terminal operations, simple nesting, variable storage</li>
 *   <li><b>endCompound()/endList()</b> - Complex chaining, type safety</li>
 *   <li><b>Variable storage</b> - Very complex structures, conditional building</li>
 * </ul>
 *
 * @author Paul Ferlitz
 */
public class BuilderPatternExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== Builder Pattern Example ===\n");

        // Example 1: Simple builder usage - no nested structures
        System.out.println("1. Simple compound (no nesting):");
        ICompoundTag gameData = NBTBuilder.compound("GameData")
                .addString("name", "MyWorld")
                .addInt("version", 2)
                .addDouble("difficulty", 1.5)
                .build(); // Terminal operation - build() ends construction

        System.out.println("\tBuilt compound: " + gameData.getString("name"));

        // Example 2: Single-level nesting - end() is safe when terminating
        System.out.println("\n2. Single-level nesting - end() for terminal operations:");
        ICompoundTag simplePlayer = (ICompoundTag) NBTBuilder.compound("SimplePlayer")
                .addString("name", "Bob")
                .addString("class", "warrior")
                .addCompound("stats")
                .addInt("level", 10)
                .addDouble("health", 20.0)
                .end() // Safe: we're finishing the stats compound, no more chaining
                .build();

        System.out.println("\tSimple player: " + simplePlayer.getString("name"));

        // Example 3: Complex chaining - type-safe end methods
        System.out.println("\n3. Complex chaining - using type-safe end methods:");
        ICompoundTag playerData = NBTBuilder.compound("Player")
                .addString("name", "Alice")
                .addInt("level", 25)

                // Complex nested structure requires type-safe methods
                .addList("inventory", NBTTags.Tag_Compound)
                .addCompound("item1")
                .addString("type", "sword")
                .addInt("damage", 10)
                .endList() // Type-safe: return to ListBuilder to add more items
                .addCompound("item2")
                .addString("type", "potion")
                .addInt("healing", 5)
                .endList() // Type-safe: return to ListBuilder again
                .endCompound() // Type-safe: return to CompoundBuilder (Player)

                .addString("lastLogin", "2024-01-01")
                .build(); // Build final structure

        System.out.println("\tComplex player: " + playerData.getString("name"));

        IListTag inventory = playerData.getList("inventory");
        if (inventory != null)
        {
            System.out.println("\tInventory items: " + inventory.getData().size());
        }

        // Example 4: Variable storage pattern - alternative to complex chaining
        System.out.println("\n4. Variable storage pattern (alternative approach):");
        IListTag separateInventory = createInventoryList();
        ICompoundTag variablePlayer = NBTBuilder.compound("VariablePlayer")
                .addString("name", "Charlie")
                .addTag((Tag<?>) separateInventory) // Add pre-built list
                .build();

        System.out.println("\tVariable player: " + variablePlayer.getString("name"));

        // Example 5: File integration with simple structure
        System.out.println("\n5. File integration (simple structure):");
        File configFile = new File("./config.dat");

        NBTBuilder.compound("ServerConfig")
                .addString("serverName", "Test Server")
                .addInt("maxPlayers", 20)
                .addByte("pvp", (byte) 1)
                .buildAndSave(configFile, Compression_Types.GZIP);

        System.out.println("\tConfig saved to file (" + configFile.length() + " bytes)");

        // Load and modify existing file - demonstrates end() in modification context
        NBTBuilder.fromFile(configFile)
                .addString("motd", "Welcome!")
                .addLong("lastModified", System.currentTimeMillis())
                .buildAndSave(configFile);

        // Verify modification
        ICompoundTag loadedConfig = NBTFileFactory.readNBTFile(configFile);
        System.out.println("\tModified config MOTD: " + loadedConfig.getString("motd"));

        // Example 6: Static factory methods for simple cases
        System.out.println("\n6. Static factory methods (no builders needed):");
        ITag<String> title = NBTBuilder.string("title", "Example");
        ITag<Integer> count = NBTBuilder.integer("count", 100);

        System.out.println("\tStatic factories: " + title.getData() + ", " + count.getData());

        // Cleanup
        configFile.delete();
        System.out.println("\nCleanup completed");
    }

    /**
     * Helper method demonstrating variable storage pattern for complex structures.
     *
     * <p><b>When to use this pattern:</b></p>
     * <ul>
     *   <li>Very complex nested structures that are hard to read in fluent chains</li>
     *   <li>When building structures conditionally or in loops</li>
     *   <li>When you need to reuse parts of the structure</li>
     *   <li>When type safety becomes difficult with deep nesting</li>
     * </ul>
     *
     * <p>Note: Here we use {@code end()} safely because we're not continuing
     * the chain - each compound is built and we return to the stored list builder.</p>
     */
    private static IListTag createInventoryList()
    {
        // Store the list builder in a variable for clear, step-by-step construction
        ListBuilder listBuilder = NBTBuilder.list("inventory", NBTTags.Tag_Compound);

        // Add first item - end() is safe because we store the parent
        listBuilder.addCompound("item1")
                .addString("type", "sword")
                .addInt("damage", 10)
                .end(); // Safe: returning to stored listBuilder variable

        // Add second item - same pattern
        listBuilder.addCompound("item2")
                .addString("type", "potion")
                .addInt("healing", 5)
                .end(); // Safe: returning to stored listBuilder variable

        return listBuilder.build();
    }
}