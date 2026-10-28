import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.api.NBTFileFactory;
import de.pauleff.jnbt.util.NBTTags;

import java.io.File;
import java.io.IOException;

public class TypeSafetyAndValidationExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== Type Safety and Validation Example ===\n");

        ICompoundTag playerData = NBTFactory.createCompound("Player")
                .addString("name", "TestPlayer")
                .addInt("level", 42)
                .addDouble("health", 20.0)
                .addByte("gameMode", (byte) 1);

        System.out.println("Type-safe access:");
        String name = playerData.getString("name");
        int level = playerData.getInt("level");
        double health = playerData.getDouble("health");

        System.out.println("\tPlayer: " + name + " (Level " + level + ", Health " + health + ")");

        // Wrong types return the same defaults as missing fields: 0 for int and null for String.
        int nameAsInt = playerData.getInt("name");
        String missingField = playerData.getString("missing");

        System.out.println("\tWrong type access: " + nameAsInt);
        System.out.println("\tMissing field: " + missingField);

        if (playerData.hasTag("name"))
        {
            System.out.println("\tName field exists: " + playerData.getString("name"));
        }

        if (!playerData.hasTag("inventory"))
        {
            System.out.println("\tInventory field missing - using default");
        }

        System.out.println("\nList type enforcement:");
        IListTag stringList = NBTFactory.createList("items", NBTTags.Tag_String.getId());
        IListTag intList = NBTFactory.createList("scores", NBTTags.Tag_Int.getId());

        stringList.addString("item1", "sword").addString("item2", "shield");
        intList.addInt("score1", 100).addInt("score2", 200);

        System.out.println("\tString list size: " + stringList.getData().size());
        System.out.println("\tInt list size: " + intList.getData().size());

        try
        {
            // Lists reject values that do not match their declared element type.
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

        System.out.println("\nData validation:");
        boolean isValid = validatePlayer(playerData);
        System.out.println("\tPlayer data valid: " + isValid);

        ICompoundTag invalidPlayer = NBTFactory.createCompound("InvalidPlayer")
                .addString("name", "")
                .addInt("level", -5)
                .addDouble("health", 25.0);

        boolean invalidIsValid = validatePlayer(invalidPlayer);
        System.out.println("\tInvalid player data valid: " + invalidIsValid);

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

        testFile.delete();
        System.out.println("Cleanup completed");
    }


    // These are application rules. The NBT library does not enforce player stat ranges.
    private static boolean validatePlayer(ICompoundTag player)
    {
        if (!player.hasTag("name") || !player.hasTag("level") || !player.hasTag("health"))
        {
            return false;
        }

        String name = player.getString("name");
        if (name == null || name.trim().isEmpty())
        {
            return false;
        }

        // This example accepts levels from 1 to 100 and health from 0 to 20.
        int level = player.getInt("level");
        if (level < 1 || level > 100)
        {
            return false;
        }

        double health = player.getDouble("health");
        return !(health < 0) && !(health > 20.0);
    }
}
