package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.formats.binary.NBTReader;
import de.pauleff.jnbt.formats.binary.NBTWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HexFormat;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SNBTTypedArrayTest
{
    static Stream<Arguments> arrays()
    {
        return Stream.of(
                Arguments.of("[B;1,2]", new byte[]{1, 2}),
                Arguments.of("[B;-128,127]", new byte[]{-128, 127}),
                Arguments.of("[B;0xff,0b10000000]", new byte[]{-1, -128}),
                Arguments.of("[B;240ub,-0x80sb,1_0,0b]", new byte[]{-16, -128, 10, 0}),
                Arguments.of("[I;1b,2s,3]", new int[]{1, 2, 3}),
                Arguments.of("[I;255ub,65535us,4294967295ui]", new int[]{-1, -1, -1}),
                Arguments.of("[I;-128b,-32768s,-2147483648]", new int[]{-128, -32768, Integer.MIN_VALUE}),
                Arguments.of("[I;2147483647,0x80000000,0xffffffff]", new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE, -1}),
                Arguments.of("[L;1,2]", new long[]{1, 2}),
                Arguments.of("[L;1b,2s,3i,4l,5]", new long[]{1, 2, 3, 4, 5}),
                Arguments.of("[L;2147483648,9223372036854775807,-9223372036854775808]",
                        new long[]{2147483648L, Long.MAX_VALUE, Long.MIN_VALUE}),
                Arguments.of("[L;0xffffffff,4294967295ui,0xffffffffffffffff]", new long[]{4294967295L, -1, -1}),
                Arguments.of("[L;255UB,65535US,18446744073709551615UL]", new long[]{-1, -1, -1}),
                Arguments.of("[L;0b1111111111111111111111111111111111111111111111111111111111111111]", new long[]{-1}),
                Arguments.of("[B;]", new byte[0]), Arguments.of("[I;]", new int[0]), Arguments.of("[L;]", new long[0]),
                Arguments.of("[B; 1 , 2b , ]", new byte[]{1, 2}),
                Arguments.of("[I; 1b , 2s , ]", new int[]{1, 2}),
                Arguments.of("[L; 1i , 2 , ]", new long[]{1, 2}));
    }

    @ParameterizedTest
    @MethodSource("arrays")
    void parsesAndRoundTripsArraysInEveryContainer(String input, Object expected) throws Exception
    {
        ITag<?> root = NBTFactory.parseFromSNBT(input);
        assertPayload(expected, root);
        assertPayload(expected, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(root)));
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT("{n:" + input + ",after:1}");
        assertPayload(expected, compound.getTag("n"));
        assertEquals(1, compound.getInt("after"));
        IListTag list = NBTFactory.parseListFromSNBT("[" + input + "," + input + "]");
        assertEquals(root.getId(), list.getListTypeID());
        assertPayload(expected, list.get(0));
        assertPayload(expected, list.get(1));
        IListTag mixed = NBTFactory.parseListFromSNBT("[" + input + ",'x']");
        assertPayload(expected, assertInstanceOf(ICompoundTag.class, mixed.get(0)).getTag(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[B;1s]", "[B;1i]", "[B;1l]", "[I;1l]", "[B;1.0]", "[I;1f]", "[L;1d]",
            "[B;128]", "[B;-129]", "[B;0x100]", "[B;0b100000000]", "[B;256ub]", "[B;128sb]",
            "[I;2147483648]", "[I;-2147483649]", "[I;0x100000000]", "[I;128b]", "[L;65536us]",
            "[L;9223372036854775808]", "[L;-9223372036854775809]", "[L;0x10000000000000000]",
            "[L;18446744073709551616ul]", "[B;true]", "[I;false]", "[L;true]", "[B;'1']", "[I;\"1\"]",
            "[L;'true']", "[B;{}]", "[I;[]]", "[L;[B;1]]", "[B;,]", "[I;1,,]", "[L;1 2]",
            "[B;1", "[I;1]extra", "[L;00]", "[B;1_]", "[I;0x_G]", "[L;240u]", "[b;1]"})
    void rejectsInvalidTypesRangesAndSyntax(String input)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"128", "-129", "256ub", "1s", "1.0", "true", "'1'", "{}", "[]"})
    void reportsTheRejectedElementPosition(String element)
    {
        String text = "{n:[B;1," + element + "]}";
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser(text).parse());
        assertEquals(8, error.getPosition());
    }

    @ParameterizedTest
    @ValueSource(strings = {"128", "-129", "256ub", "0x100"})
    void rangeErrorsRetainTheirNumericCause(String element)
    {
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser("[B;" + element + "]").parse());
        assertEquals(3, error.getPosition());
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    static Stream<Arguments> binaryFixtures()
    {
        // Independent binary compound roots with one array field named "n".
        return Stream.of(
                Arguments.of("[B;1,2,-128,0xff]", "0a0000 0700016e 00000004 010280ff 00"),
                Arguments.of("[I;1b,2s,3]", "0a0000 0b00016e 00000003 00000001 00000002 00000003 00"),
                Arguments.of("[I;255ub,65535us,0xffffffff]", "0a0000 0b00016e 00000003 ffffffff ffffffff ffffffff 00"),
                Arguments.of("[L;1,2,2147483648]", "0a0000 0c00016e 00000003 0000000000000001 0000000000000002 0000000080000000 00"),
                Arguments.of("[L;0xffffffff,4294967295ui]", "0a0000 0c00016e 00000002 00000000ffffffff ffffffffffffffff 00"),
                Arguments.of("[L;0xffffffffffffffff]", "0a0000 0c00016e 00000001 ffffffffffffffff 00"));
    }

    @ParameterizedTest
    @MethodSource("binaryFixtures")
    void matchesIndependentBinaryFixtures(String input, String hex) throws Exception
    {
        byte[] fixture = HexFormat.of().parseHex(hex.replace(" ", ""));
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT("{n:" + input + "}");
        root.setName("");
        assertArrayEquals(fixture, write(root));
        ICompoundTag decoded = new NBTReader(new DataInputStream(new ByteArrayInputStream(fixture))).read();
        assertArrayEquals(fixture, write(decoded));
        ICompoundTag textRoundTrip = NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(decoded));
        textRoundTrip.setName("");
        assertArrayEquals(fixture, write(textRoundTrip));
    }

    @ParameterizedTest
    @CsvSource({"B,238", "I,244", "L,252"})
    void inferredElementsRespectDepthCountLengthAndAllocationLimits(char prefix, long allocation) throws SNBTException
    {
        String input = "[" + prefix + ";1,2]";
        assertNotNull(new SNBTParser(input, limits(allocation, 3, 2)).parse());
        SNBTException count = assertThrows(SNBTException.class,
                () -> new SNBTParser(input, limits(4096, 2, 2)).parse());
        assertTrue(count.getMessage().contains("tag count limit"), count::getMessage);
        SNBTException length = assertThrows(SNBTException.class,
                () -> new SNBTParser(input, limits(4096, 3, 1)).parse());
        assertTrue(length.getMessage().contains("collection length"), length::getMessage);
        SNBTException memory = assertThrows(SNBTException.class,
                () -> new SNBTParser(input, limits(allocation - 1, 3, 2)).parse());
        assertTrue(memory.getMessage().contains("allocation limit"), memory::getMessage);
    }

    @Test
    void emptyArraysNeedNoElementBudget() throws SNBTException
    {
        for (String input : new String[]{"[B;]", "[I;]", "[L;]"})
            assertNotNull(new SNBTParser(input, limits(4096, 1, 0)).parse());
    }

    @Test
    void inferenceDoesNotAffectScalarOrListNumbers() throws SNBTException
    {
        assertEquals(Integer.class, NBTFactory.parseFromSNBT("1").getData().getClass());
        IListTag list = NBTFactory.parseListFromSNBT("[1,2]");
        assertEquals(3, list.getListTypeID());
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("2147483648"));
        assertThrows(SNBTException.class, () -> NBTFactory.parseListFromSNBT("[2147483648]"));
    }

    private static byte[] write(ICompoundTag root) throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        return bytes.toByteArray();
    }

    private static void assertPayload(Object expected, ITag<?> actual)
    {
        if (expected instanceof byte[] bytes)
        {
            assertEquals(7, actual.getId());
            assertArrayEquals(bytes, assertInstanceOf(byte[].class, actual.getData()));
        } else if (expected instanceof int[] ints)
        {
            assertEquals(11, actual.getId());
            assertArrayEquals(ints, assertInstanceOf(int[].class, actual.getData()));
        } else
        {
            assertEquals(12, actual.getId());
            assertArrayEquals((long[]) expected, assertInstanceOf(long[].class, actual.getData()));
        }
    }

    private static NBTLimits limits(long allocation, long tags, int length)
    {
        return new NBTLimits(1, 4096, allocation, tags, length, 4096);
    }
}
