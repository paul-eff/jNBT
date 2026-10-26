package de.pauleff.jnbt.builder;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.util.NBTTags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuilderNameTest
{
    @ParameterizedTest
    @ValueSource(strings = {"", "null", " ", "  spaced name  ", "\t"})
    void acceptsNamesInFactoriesAndContainers(String name)
    {
        assertEquals(name, NBTBuilder.string(name, "value").getName());
        assertEquals(name, NBTBuilder.integer(name, 1).getName());
        assertEquals(name, NBTBuilder.doubleTag(name, 1).getName());
        assertEquals(name, NBTBuilder.floatTag(name, 1).getName());
        assertEquals(name, NBTBuilder.byteTag(name, (byte) 1).getName());
        assertEquals(name, NBTBuilder.shortTag(name, (short) 1).getName());
        assertEquals(name, NBTBuilder.longTag(name, 1).getName());

        ICompoundTag compound = NBTBuilder.compound(name).addInt(name, 1).build();
        assertEquals(name, compound.getName());
        assertEquals(1, compound.getInt(name));

        IListTag list = NBTBuilder.list(name, NBTTags.Tag_Int).addInt(name, 2).build();
        assertEquals(name, list.getName());
        assertEquals(name, list.getData().getFirst().getName());

        ICompoundTag nested = NBTBuilder.compound("root")
                .addCompound(name).addInt("value", 3).endCompound()
                .build();
        assertEquals(name, nested.getCompound(name).getName());
        assertEquals(3, nested.getCompound(name).getInt("value"));
    }

    @Test
    void stillRejectsNullNames()
    {
        assertThrows(IllegalArgumentException.class, () -> NBTBuilder.compound((String) null));
        assertThrows(IllegalArgumentException.class, () -> NBTBuilder.list(null, NBTTags.Tag_Int));
        assertThrows(IllegalArgumentException.class, () -> NBTBuilder.integer(null, 1));
        assertThrows(IllegalArgumentException.class, () -> NBTBuilder.compound("").addInt(null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> NBTBuilder.list("", NBTTags.Tag_Int).addInt(null, 1));
    }
}
