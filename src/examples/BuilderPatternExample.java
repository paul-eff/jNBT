import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.builder.NBTBuilder;
import de.pauleff.jnbt.util.NBTTags;

/** Builds an item with nested components and a list of lore lines. */
public class BuilderPatternExample
{
    public static void main(String[] args)
    {
        // Each end helper finishes the child and returns its parent.
        ICompoundTag item = NBTBuilder.compound("")
                .addString("id", "minecraft:diamond_pickaxe")
                .addInt("count", 1)
                .addCompound("components")
                    .addList("minecraft:lore", NBTTags.Tag_Compound)
                        .addCompound("")
                            .addString("text", "Ready for the next mining trip")
                        .endList()
                    .endCompound()
                .endCompound()
                .build();

        System.out.println(NBTFactory.toSNBT(item));
    }
}
