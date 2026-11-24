import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.api.NBTFileFactory;
import de.pauleff.jnbt.formats.binary.Compression_Types;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/** Saves and reloads an item stack with GZIP compression. */
public class CompressionAndIOExample
{
    public static void main(String[] args) throws IOException
    {
        ICompoundTag item = NBTFactory.createCompound("")
                .addString("id", "minecraft:bread")
                .addInt("count", 16);

        File file = Files.createTempFile("jnbt-item-", ".nbt").toFile();
        try
        {
            NBTFileFactory.writeNBTFile(file, item, Compression_Types.GZIP);

            // Reading detects compression; saving without a type preserves it.
            ICompoundTag loaded = NBTFileFactory.readNBTFile(file);
            loaded.setInt("count", 32);
            NBTFileFactory.writeNBTFile(file, loaded);

            System.out.println(NBTFileFactory.getCompressionType(file));
            System.out.println(NBTFactory.toSNBT(NBTFileFactory.readNBTFile(file)));
        } finally
        {
            Files.deleteIfExists(file.toPath());
        }
    }
}
