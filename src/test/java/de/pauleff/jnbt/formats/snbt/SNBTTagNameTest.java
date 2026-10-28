package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.builder.NBTBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SNBTTagNameTest
{
    private static void assertKeys(ICompoundTag compound)
    {
        assertEquals(3, compound.getData().size());
        assertEquals(1, compound.getInt(""));
        assertEquals(2, compound.getInt("null"));
        assertEquals(3, compound.getInt(" "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"\":1,\"null\":2,\" \":3}", "{'':1,'null':2,' ':3}"})
    void preservesEmptyLiteralNullAndWhitespaceKeys(String input) throws SNBTException
    {
        ICompoundTag parsed = assertInstanceOf(ICompoundTag.class, new SNBTParser(input).parse());
        assertKeys(parsed);

        String serialized = new SNBTSerializer().serialize(parsed);
        assertKeys(assertInstanceOf(ICompoundTag.class, new SNBTParser(serialized).parse()));

        ICompoundTag built = NBTBuilder.compound("")
                .addInt("", 1).addInt("null", 2).addInt(" ", 3).build();
        assertKeys(assertInstanceOf(ICompoundTag.class,
                new SNBTParser(new SNBTSerializer().serialize(built)).parse()));
    }
}
