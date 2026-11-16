package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.core.Tag_List;
import de.pauleff.jnbt.core.Tag_String;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SNBTFidelityTest
{
    private static Stream<ITag<?>> finiteNumbers()
    {
        Stream<ITag<?>> floats = Stream.of(0.0f, -0.0f, 1.0f, -1.25f, 1e20f, -1e-20f,
                        Float.MIN_VALUE, -Float.MIN_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE,
                        -Float.MAX_VALUE, Math.nextUp(1.0f))
                .map(value -> NBTFactory.createFloat("n", value));
        Stream<ITag<?>> doubles = Stream.of(0.0d, -0.0d, 1.0d, -1.25d, 1e20d, -1e-20d,
                        Double.MIN_VALUE, -Double.MIN_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE,
                        -Double.MAX_VALUE, Math.nextUp(1.0d))
                .map(value -> NBTFactory.createDouble("n", value));
        Stream<ITag<?>> integers = Stream.of(
                NBTFactory.createByte("n", Byte.MIN_VALUE), NBTFactory.createByte("n", Byte.MAX_VALUE),
                NBTFactory.createShort("n", Short.MIN_VALUE), NBTFactory.createShort("n", Short.MAX_VALUE),
                NBTFactory.createInt("n", Integer.MIN_VALUE), NBTFactory.createInt("n", Integer.MAX_VALUE),
                NBTFactory.createLong("n", Long.MIN_VALUE), NBTFactory.createLong("n", Long.MAX_VALUE));
        return Stream.concat(Stream.concat(floats, doubles), integers);
    }

    private static void assertSameNumber(ITag<?> expected, ITag<?> actual)
    {
        assertEquals(expected.getId(), actual.getId());
        if (expected.getData() instanceof Float value)
        {
            assertEquals(Float.floatToRawIntBits(value),
                    Float.floatToRawIntBits(assertInstanceOf(Float.class, actual.getData())));
        } else if (expected.getData() instanceof Double value)
        {
            assertEquals(Double.doubleToRawLongBits(value),
                    Double.doubleToRawLongBits(assertInstanceOf(Double.class, actual.getData())));
        } else
        {
            assertEquals(expected.getData(), actual.getData());
        }
    }

    @ParameterizedTest
    @MethodSource("finiteNumbers")
    void finiteNumbersRoundTripInRootsCompoundsAndLists(ITag<?> original) throws SNBTException
    {
        assertSameNumber(original, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(original)));

        ICompoundTag compound = NBTFactory.createCompound().setTag(original);
        ICompoundTag parsed = NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(compound));
        assertSameNumber(original, parsed.getTag("n"));

        Tag_List list = new Tag_List("list", original.getId());
        list.addTag(original);
        IListTag parsedList = NBTFactory.parseListFromSNBT(NBTFactory.toSNBT(list));
        assertEquals(original.getId(), parsedList.getListTypeID());
        assertSameNumber(original, parsedList.get(0));
    }

    private static Stream<Arguments> numericLiterals()
    {
        return Stream.of(
                Arguments.of("1.0E20", 1e20d), Arguments.of("1e3", 1000.0d),
                Arguments.of("1.2e3", 1200.0d), Arguments.of("1.2E+3", 1200.0d),
                Arguments.of("12000e-1", 1200.0d), Arguments.of("-2.5e-2D", -0.025d),
                Arguments.of("+2E3d", 2000.0d), Arguments.of("1e2f", 100.0f),
                Arguments.of("-1.25E+2F", -125.0f), Arguments.of(".5", 0.5d),
                Arguments.of("-.5f", -0.5f), Arguments.of("1.", 1.0d),
                Arguments.of(".5e2", 50.0d), Arguments.of("1.e2f", 100.0f),
                Arguments.of("-0e10", -0.0d));
    }

    @ParameterizedTest
    @MethodSource("numericLiterals")
    void parsesIndependentDecimalAndExponentLiterals(String input, Object expected) throws SNBTException
    {
        ITag<?> parsed = NBTFactory.parseFromSNBT(input);
        assertEquals(expected.getClass(), parsed.getData().getClass());
        assertEquals(expected, parsed.getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1e", "1e+", "1e-", "1e2e3", "1e2.3", ".e2", "+.",
            "1e2b", "1e2s", "1e2l", "1e309", "-1e309", "1e39f", "-1e39F"})
    void rejectsMalformedExponentsAndOverflow(String input)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(input));
    }

    @Test
    void numericErrorsRetainTheFailingTokenPosition()
    {
        SNBTException error = assertThrows(SNBTException.class,
                () -> new SNBTParser("{n:1e309,after:1}").parse());
        assertEquals(3, error.getPosition());
        assertInstanceOf(NumberFormatException.class, error.getCause());
        assertTrue(error.getMessage().contains("1e309"));
    }

    private static Stream<ITag<?>> nonFiniteNumbers()
    {
        return Stream.of(NBTFactory.createFloat("bad", Float.NaN),
                NBTFactory.createFloat("bad", Float.POSITIVE_INFINITY),
                NBTFactory.createFloat("bad", Float.NEGATIVE_INFINITY),
                NBTFactory.createDouble("bad", Double.NaN),
                NBTFactory.createDouble("bad", Double.POSITIVE_INFINITY),
                NBTFactory.createDouble("bad", Double.NEGATIVE_INFINITY));
    }

    @ParameterizedTest
    @MethodSource("nonFiniteNumbers")
    void rejectsNonFiniteOutputBeforeOpeningFiles(ITag<?> invalid, @TempDir Path directory) throws Exception
    {
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(invalid));
        ICompoundTag root = NBTFactory.createCompound().addInt("before", 1).setTag(invalid);
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(root));
        Tag_List list = new Tag_List("", invalid.getId());
        list.addTag(invalid);
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(list));

        Path existing = directory.resolve("existing.snbt");
        Files.writeString(existing, "original");
        assertThrows(IllegalArgumentException.class,
                () -> NBTFactory.writeToSNBTFile(root, existing.toFile()));
        assertEquals("original", Files.readString(existing));
        Path absent = directory.resolve("absent.snbt");
        assertThrows(IllegalArgumentException.class,
                () -> NBTFactory.writeToSNBTFile(root, absent.toFile()));
        assertFalse(Files.exists(absent));
    }

    @ParameterizedTest
    @ValueSource(strings = {"café", "日本語", "😀", "abc١", "true", "false", "1e20", ".5",
            "+value", "-value", "", "a b", "a\"b", "a\\b", "line\nbreak"})
    void quotesStringsAndKeysWithoutChangingTheirValues(String value) throws SNBTException
    {
        String output = NBTFactory.toSNBT(NBTFactory.createString("", value));
        assertTrue(output.startsWith("\"") && output.endsWith("\""), output);
        assertEquals(value, NBTFactory.parseFromSNBT(output).getData());

        ICompoundTag original = NBTFactory.createCompound().addString(value, value);
        String compoundOutput = NBTFactory.toSNBT(original);
        assertTrue(compoundOutput.startsWith("{" + output + ":"), compoundOutput);
        assertEquals(value, NBTFactory.parseCompoundFromSNBT(compoundOutput).getString(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"simple_string", "ABC123", "_key"})
    void keepsSafeAsciiIdentifiersUnquoted(String value) throws SNBTException
    {
        assertEquals(value, NBTFactory.toSNBT(NBTFactory.createString("", value)));
        assertEquals(value, NBTFactory.parseFromSNBT(value).getData());
    }

    private static Tag<Object> invalidTag(int type, Object payload)
    {
        // Bypass model validation to exercise the serializer's public ITag boundary.
        return new Tag<Object>(3, "bad")
        {
            @Override
            public int getId()
            {
                return type;
            }

            @Override
            public Object getData()
            {
                return payload;
            }
        };
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12})
    void rejectsNullAndIncompatiblePayloads(int type)
    {
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(invalidTag(type, null)));
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(invalidTag(type, new Object())));
    }

    @ParameterizedTest
    @ValueSource(ints = {9, 10})
    void rejectsCollectionPayloadWithoutTheRequiredInterface(int type)
    {
        assertThrows(IllegalArgumentException.class,
                () -> NBTFactory.toSNBT(invalidTag(type, new java.util.ArrayList<>())));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 13, 99})
    void rejectsEndAndUnknownTypes(int type)
    {
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(invalidTag(type, 1)));
    }

    @Test
    void rejectsCorruptedCollections()
    {
        Tag_Compound nullChild = new Tag_Compound("")
        {
            {
                data.add(null);
            }
        };
        Tag_List wrongType = new Tag_List("", 3)
        {
            {
                data.add(new Tag_String("", "text"));
            }
        };
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(nullChild));
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.toSNBT(wrongType));
    }

    @Test
    void invalidNestedPayloadDoesNotChangeDestination(@TempDir Path directory) throws Exception
    {
        ICompoundTag root = NBTFactory.createCompound().addInt("before", 1).setTag(invalidTag(1, "wrong"));
        Path file = directory.resolve("existing.snbt");
        Files.writeString(file, "original");
        assertThrows(IllegalArgumentException.class, () -> NBTFactory.writeToSNBTFile(root, file.toFile()));
        assertEquals("original", Files.readString(file));
    }
}
