package de.paulferlitz.examples;

import de.paulferlitz.api.ICompoundTag;
import de.paulferlitz.api.IListTag;
import de.paulferlitz.api.NBTFactory;
import de.paulferlitz.api.NBTFileFactory;
import de.paulferlitz.util.NBTTags;

import java.io.File;
import java.io.IOException;

/**
 * Demonstrates type safety and validation features of jNBT.
 * Key concepts:
 * - Type-safe getters return defaults instead of throwing exceptions
 * - List type enforcement prevents adding wrong types
 * - Data validation strategies for robust applications
 * - Error handling patterns
 * Always check hasTag() before accessing optional data.
 * Use type-safe getters to avoid ClassCastException.
 *
 * @author Paul Ferlitz
 */
public class TypeSafetyAndValidationExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== Type Safety and Validation Example ===\n");

        // Create test data
        ICompoundTag playerData = NBTFactory.createCompound("Player")
                .addString("name", "TestPlayer")
                .addInt("level", 42)
                .addDouble("health", 20.0)
                .addByte("gameMode", (byte) 1);

        // Type-safe getters
        System.out.println("Type-safe access:");
        String name = playerData.getString("name");
        int level = playerData.getInt("level");
        double health = playerData.getDouble("health");

        System.out.println("\tPlayer: " + name + " (Level " + level + ", Health " + health + ")");

        // Type safety - wrong type returns default
        int nameAsInt = playerData.getInt("name"); // String accessed as int
        String missingField = playerData.getString("missing"); // Non-existent field

        System.out.println("\tWrong type access: " + nameAsInt);
        System.out.println("\tMissing field: " + missingField);

        // Safe existence checking
        if (playerData.hasTag("name"))
        {
            System.out.println("\tName field exists: " + playerData.getString("name"));
        }

        if (!playerData.hasTag("inventory"))
        {
            System.out.println("\tInventory field missing - using default");
        }

        // List type enforcement
        System.out.println("\nList type enforcement:");
        IListTag stringList = NBTFactory.createList("items", NBTTags.Tag_String.getId());
        IListTag intList = NBTFactory.createList("scores", NBTTags.Tag_Int.getId());

        // Correct usage
        stringList.addString("item1", "sword").addString("item2", "shield");
        intList.addInt("score1", 100).addInt("score2", 200);

        System.out.println("\tString list size: " + stringList.getData().size());
        System.out.println("\tInt list size: " + intList.getData().size());

        // Type enforcement - these will throw IllegalArgumentException
        try
        {
            stringList.addInt("invalid", 42);
            System.out.println("\tERROR: Type violation not caught!");
        } catch (IllegalArgumentException e)
        {
            System.out.println("\t✓ Prevented adding int to string list");
        }

        try
        {
            intList.addString("invalid", "text");
            System.out.println("\tERROR: Type violation not caught!");
        } catch (IllegalArgumentException e)
        {
            System.out.println("\t✓ Prevented adding string to int list");
        }

        // Data validation
        System.out.println("\nData validation:");
        boolean isValid = validatePlayer(playerData);
        System.out.println("\tPlayer data valid: " + isValid);

        // Test with invalid data
        ICompoundTag invalidPlayer = NBTFactory.createCompound("InvalidPlayer")
                .addString("name", "") // Empty name
                .addInt("level", -5)   // Negative level
                .addDouble("health", 25.0); // Health too high

        boolean invalidIsValid = validatePlayer(invalidPlayer);
        System.out.println("\tInvalid player data valid: " + invalidIsValid);

        // File validation
        System.out.println("\nFile validation:");
        File testFile = new File("./validation_test.dat");

        NBTFileFactory.writeNBTFile(testFile, playerData);

        if (NBTFileFactory.isValidNBTFile(testFile))
        {
            System.out.println("\tFile is valid NBT format");

            ICompoundTag loadedData = NBTFileFactory.readNBTFile(testFile);
            boolean contentValid = validatePlayer(loadedData);
            System.out.println("\tFile content valid: " + contentValid);
        } else
        {
            System.out.println("\tFile is not valid NBT format");
        }

        // Error handling
        System.out.println("\nError handling:");
        File nonExistentFile = new File("./does_not_exist.dat");

        try
        {
            NBTFileFactory.readNBTFile(nonExistentFile);
            System.out.println("\tShould not reach here");
        } catch (RuntimeException e)
        {
            System.out.println("\t✓ Correctly caught file not found error");
        }

        // Cleanup
        testFile.delete();
        System.out.println("Cleanup completed");
    }

    /**
     * Validates player data. Returns true if all validation checks pass.
     */
    private static boolean validatePlayer(ICompoundTag player)
    {
        // Check required fields exist
        if (!player.hasTag("name") || !player.hasTag("level") || !player.hasTag("health"))
        {
            return false;
        }

        // Validate name
        String name = player.getString("name");
        if (name == null || name.trim().isEmpty())
        {
            return false;
        }

        // Validate level range
        int level = player.getInt("level");
        if (level < 1 || level > 100)
        {
            return false;
        }

        // Validate health range
        double health = player.getDouble("health");
        return !(health < 0) && !(health > 20.0);
    }
}