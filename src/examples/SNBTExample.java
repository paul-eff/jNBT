import de.pauleff.jnbt.core.Collection_Tag;
import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.formats.snbt.SNBTException;
import de.pauleff.jnbt.util.NBTTags;

import java.io.File;
import java.io.IOException;

public class SNBTExample
{
    public static void main(String[] args) throws IOException
    {
        System.out.println("=== SNBT (Stringified NBT) Example ===\n");

        try
        {
            System.out.println("1. Basic SNBT types:");

            System.out.println("\tParsing: 127b -> " + NBTFactory.parseFromSNBT("127b").getData() + " (Byte)");
            System.out.println("\tParsing: 32767s -> " + NBTFactory.parseFromSNBT("32767s").getData() + " (Short)");
            System.out.println("\tParsing: 2147483647 -> " + NBTFactory.parseFromSNBT("2147483647").getData() + " (Int)");
            System.out.println("\tParsing: 9223372036854775807l -> " + NBTFactory.parseFromSNBT("9223372036854775807l").getData() + " (Long)");
            System.out.println("\tParsing: 3.14159f -> " + NBTFactory.parseFromSNBT("3.14159f").getData() + " (Float)");
            System.out.println("\tParsing: 2.718281828459045 -> " + NBTFactory.parseFromSNBT("2.718281828459045").getData() + " (Double)");

            // The array prefix selects the element type. Byte values also need the b suffix.
            byte[] byteArray = (byte[]) NBTFactory.parseFromSNBT("[B;1b,2b,3b]").getData();
            System.out.println("\tByte array [B;1b,2b,3b]: " + byteArray.length + " elements");

            IListTag stringList = NBTFactory.parseListFromSNBT("[apple,banana,cherry]");
            System.out.println("\tString list: " + stringList.getData().size() + " items");

            String compound = "{name:Steve,level:42,health:20.0}";
            ICompoundTag player = NBTFactory.parseCompoundFromSNBT(compound);
            System.out.println("\tCompound: " + player.getString("name") + ", level " + player.getInt("level"));

            System.out.println("\n2. Minecraft command NBT:");

            String itemNBT = "{display:{Name:Legendary_Sword,Lore:[Powerful_weapon]},Enchantments:[{id:sharpness,lvl:5s}]}";
            ICompoundTag item = NBTFactory.parseCompoundFromSNBT(itemNBT);
            String itemName = item.getCompound("display").getString("Name");
            int enchantCount = item.getList("Enchantments").getData().size();
            System.out.println("\tParsed item: " + itemName + " with " + enchantCount + " enchantments");

            ICompoundTag playerData = NBTFactory.createCompound()
                    .addFloat("Health", 20.0f)
                    .addInt("XpLevel", 30);
            String command = NBTFactory.toSNBT(playerData);
            System.out.println("\tGenerated command: /data merge entity @s " + command);

            System.out.println("\n3. SNBT serialization:");

            ICompoundTag config = NBTFactory.createCompound()
                    .addString("name", "Test Server")
                    .addInt("maxPlayers", 100)
                    .addFloat("version", 1.21f);

            IListTag features = NBTFactory.createList("features", NBTTags.Tag_String.getId())
                    .addString("f1", "pvp")
                    .addString("f2", "economy");
            // Collection_Tag exposes addTag() for attaching an existing list.
            ((Collection_Tag) config).addTag(features);

            String snbtOutput = NBTFactory.toSNBT(config);
            System.out.println("\tSNBT: " + snbtOutput);

            System.out.println("\n4. File operations:");

            ICompoundTag serverConfig = NBTFactory.createCompound()
                    .addString("serverName", "My Server")
                    .addInt("maxPlayers", 50)
                    .addString("difficulty", "normal");

            File configFile = new File("server.snbt");
            NBTFactory.writeToSNBTFile(serverConfig, configFile);
            System.out.println("\tWrote config to: " + configFile.getName() + " (" + configFile.length() + " bytes)");

            String fileContent = java.nio.file.Files.readString(configFile.toPath());
            ICompoundTag loaded = NBTFactory.parseCompoundFromSNBT(fileContent);
            System.out.println("\tLoaded server: " + loaded.getString("serverName") +
                    " (" + loaded.getInt("maxPlayers") + " players)");

            // Compare serialized output after parsing it again.
            String original = NBTFactory.toSNBT(serverConfig);
            String roundTrip = NBTFactory.toSNBT(NBTFactory.parseFromSNBT(original));
            boolean success = original.equals(roundTrip);
            System.out.println("\tRound-trip test: " + (success ? "✓ PASSED" : "✗ FAILED"));

            boolean deleted = configFile.delete();
            System.out.println("\tCleanup: " + (deleted ? "success" : "failed"));

            System.out.println("\n5. Error handling:");

            // These inputs cover missing delimiters, mixed types, and invalid numbers.
            String[] testCases = {
                    "{unclosed_brace: value",
                    "[1, 2, mixed_types]",
                    "{key: 42invalid_suffix}",
                    "[I; 1, 2, 3.14]"
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
