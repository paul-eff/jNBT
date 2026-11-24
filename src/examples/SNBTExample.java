import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.formats.snbt.SNBTException;

/** Parses and serializes an item stack with a custom name. */
public class SNBTExample
{
    public static void main(String[] args) throws SNBTException
    {
        String snbt = """
                {id:"minecraft:diamond_sword",count:1,components:{
                    "minecraft:custom_name":{text:"Mr Reliable"}
                }}
                """;

        ICompoundTag item = NBTFactory.parseCompoundFromSNBT(snbt);
        ICompoundTag name = item.getCompound("components").getCompound("minecraft:custom_name");
        name.setString("text", "Mr Indestructible");

        System.out.println(NBTFactory.toSNBT(item));
    }
}
