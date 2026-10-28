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

public class BuilderPatternExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== Builder Pattern Example ===\n");

        System.out.println("1. Simple compound (no nesting):");
        ICompoundTag gameData = NBTBuilder.compound("GameData")
                .addString("name", "MyWorld")
                .addInt("version", 2)
                .addDouble("difficulty", 1.5)
                .build();

        System.out.println("\tBuilt compound: " + gameData.getString("name"));

        System.out.println("\n2. Single-level nesting - end() for terminal operations:");
        // end() adds the nested tag to its parent and returns an NBTBuilder.
        ICompoundTag simplePlayer = (ICompoundTag) NBTBuilder.compound("SimplePlayer")
                .addString("name", "Bob")
                .addString("class", "warrior")
                .addCompound("stats")
                .addInt("level", 10)
                .addDouble("health", 20.0)
                .end()
                .build();

        System.out.println("\tSimple player: " + simplePlayer.getString("name"));

        System.out.println("\n3. Complex chaining - using type-safe end methods:");
        // Use endList() and endCompound() to keep the parent type when chaining.
        ICompoundTag playerData = NBTBuilder.compound("Player")
                .addString("name", "Alice")
                .addInt("level", 25)

                .addList("inventory", NBTTags.Tag_Compound)
                .addCompound("item1")
                .addString("type", "sword")
                .addInt("damage", 10)
                .endList()
                .addCompound("item2")
                .addString("type", "potion")
                .addInt("healing", 5)
                .endList()
                .endCompound()

                .addString("lastLogin", "2024-01-01")
                .build();

        System.out.println("\tComplex player: " + playerData.getString("name"));

        IListTag inventory = playerData.getList("inventory");
        if (inventory != null)
        {
            System.out.println("\tInventory items: " + inventory.getData().size());
        }

        System.out.println("\n4. Variable storage pattern (alternative approach):");
        IListTag separateInventory = createInventoryList();
        ICompoundTag variablePlayer = NBTBuilder.compound("VariablePlayer")
                .addString("name", "Charlie")
                .addTag((Tag<?>) separateInventory)
                .build();

        System.out.println("\tVariable player: " + variablePlayer.getString("name"));

        System.out.println("\n5. File integration (simple structure):");
        File configFile = new File("./config.dat");

        NBTBuilder.compound("ServerConfig")
                .addString("serverName", "Test Server")
                .addInt("maxPlayers", 20)
                .addByte("pvp", (byte) 1)
                .buildAndSave(configFile, Compression_Types.GZIP);

        System.out.println("\tConfig saved to file (" + configFile.length() + " bytes)");

        // Loading a file allows updates while keeping its compression on save.
        NBTBuilder.fromFile(configFile)
                .addString("motd", "Welcome!")
                .addLong("lastModified", System.currentTimeMillis())
                .buildAndSave(configFile);

        ICompoundTag loadedConfig = NBTFileFactory.readNBTFile(configFile);
        System.out.println("\tModified config MOTD: " + loadedConfig.getString("motd"));

        System.out.println("\n6. Static factory methods (no builders needed):");
        ITag<String> title = NBTBuilder.string("title", "Example");
        ITag<Integer> count = NBTBuilder.integer("count", 100);

        System.out.println("\tStatic factories: " + title.getData() + ", " + count.getData());

        configFile.delete();
        System.out.println("\nCleanup completed");
    }


    private static IListTag createInventoryList()
    {
        // Keep the parent builder in a variable when the return value of end() is not needed.
        ListBuilder listBuilder = NBTBuilder.list("inventory", NBTTags.Tag_Compound);

        listBuilder.addCompound("item1")
                .addString("type", "sword")
                .addInt("damage", 10)
                .end();

        listBuilder.addCompound("item2")
                .addString("type", "potion")
                .addInt("healing", 5)
                .end();

        return listBuilder.build();
    }
}
