import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.NBTFactory;

/** Creates and edits an item stack. */
public class BasicOperationsExample
{
    public static void main(String[] args)
    {
        ICompoundTag item = NBTFactory.createCompound("")
                .addString("id", "minecraft:diamond")
                .addInt("count", 3);

        String id = item.getString("id");
        int count = item.getInt("count");
        System.out.println(id + " x" + count);

        // Setters replace an existing child or add it if missing.
        item.setInt("count", count + 1);
        System.out.println(NBTFactory.toSNBT(item));
    }
}
