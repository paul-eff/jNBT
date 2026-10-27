import de.pauleff.jnbt.api.*;
import de.pauleff.jnbt.util.NBTTags;

import java.io.File;
import java.io.IOException;

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

        // Add methods can be chained. Each child name must be unique.
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

        // Missing fields and wrong types return defaults instead of throwing.
        int nameAsInt = player.getInt("name"); // Returns 0 because the stored value is a string.
        String missingField = player.getString("missing"); // Returns null because the field is missing.

        System.out.println("Type safety: name as int = " + nameAsInt + ", missing field = " + missingField);

        // All elements must match the list type. Element names are not stored in binary NBT.
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
