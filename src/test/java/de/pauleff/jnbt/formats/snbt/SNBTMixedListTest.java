package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.core.Collection_Tag;
import de.pauleff.jnbt.core.Tag_String;
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
import java.io.IOException;
import java.util.HexFormat;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SNBTMixedListTest
{
    static Stream<Arguments> nonCompoundValues()
    {
        return Stream.of(
                Arguments.of("1b", 1, (byte) 1),
                Arguments.of("2s", 2, (short) 2),
                Arguments.of("3", 3, 3),
                Arguments.of("4l", 4, 4L),
                Arguments.of("-0.0f", 5, -0.0f),
                Arguments.of("1e20", 6, 1e20),
                Arguments.of("[B;1b,-2b]", 7, new byte[]{1, -2}),
                Arguments.of("'true'", 8, "true"),
                Arguments.of("[1,2]", 9, null),
                Arguments.of("[I;1,-2]", 11, new int[]{1, -2}),
                Arguments.of("[L;1l,-2l]", 12, new long[]{1, -2}));
    }

    @ParameterizedTest
    @MethodSource("nonCompoundValues")
    void wrapsEveryNonCompoundTypeBeforeOrAfterACompound(String literal, int type, Object expected)
            throws SNBTException
    {
        for (boolean compoundFirst : new boolean[]{false, true})
        {
            String input = compoundFirst ? "[{keep:3}," + literal + "]" : "[" + literal + ",{keep:3}]";
            IListTag list = NBTFactory.parseListFromSNBT(input);
            assertEquals(10, list.getListTypeID());
            assertEquals(2, list.size());
            ITag<?> value = wrappedValue(list, compoundFirst ? 1 : 0);
            assertEquals(type, value.getId());
            if (type == 9)
            {
                IListTag nested = assertInstanceOf(IListTag.class, value);
                assertEquals(3, nested.getListTypeID());
                assertEquals(1, nested.get(0).getData());
                assertEquals(2, nested.get(1).getData());
            } else
            {
                assertPayloadEquals(expected, value.getData());
            }
            ICompoundTag compound = assertInstanceOf(ICompoundTag.class, list.get(compoundFirst ? 0 : 1));
            assertEquals(1, compound.getData().size());
            assertEquals(3, compound.getInt("keep"));
            assertFalse(compound.hasTag(""));
        }
    }

    @Test
    void differentNumericTypesAreWrappedWithoutCoercion() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[1b,2s,3,4l,5f,6d]");
        assertEquals(10, list.getListTypeID());
        assertEquals(6, list.size());
        for (int i = 0; i < list.size(); i++) assertEquals(i + 1, wrappedValue(list, i).getId());
    }

    @Test
    void lateMismatchWrapsEarlierAndLaterValuesInOrder() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[1,2,3,'x',4,{},'y']");
        assertEquals(10, list.getListTypeID());
        assertEquals(7, list.size());
        for (int i = 0; i < 3; i++) assertEquals(i + 1, wrappedValue(list, i).getData());
        assertEquals("x", wrappedValue(list, 3).getData());
        assertEquals(4, wrappedValue(list, 4).getData());
        assertTrue(assertInstanceOf(ICompoundTag.class, list.get(5)).getData().isEmpty());
        assertEquals("y", wrappedValue(list, 6).getData());
    }

    @Test
    void quotedBooleansAndEscapedStringsRetainTheirTypes() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[true,'true',false,'false','\\x41\\s\\N{Snowman}']");
        assertEquals((byte) 1, wrappedValue(list, 0).getData());
        assertEquals("true", wrappedValue(list, 1).getData());
        assertEquals((byte) 0, wrappedValue(list, 2).getData());
        assertEquals("false", wrappedValue(list, 3).getData());
        assertEquals("A \u2603", wrappedValue(list, 4).getData());
    }

    @ParameterizedTest
    @CsvSource(value = {"[1b,2b]|1", "[1s,2s]|2", "[1,2]|3", "[1l,2l]|4", "[1f,2f]|5",
            "[1d,2d]|6", "[[B;1b],[B;2b]]|7", "['x','y']|8", "[[1],['x']]|9",
            "[{}, {a:1}]|10", "[[I;1],[I;2]]|11", "[[L;1l],[L;2l]]|12", "[]|10"}, delimiter = '|')
    void homogeneousListsKeepTheirOriginalStorage(String input, int elementType) throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT(input);
        assertEquals(elementType, list.getListTypeID());
        for (ITag<?> child : list.getData()) assertEquals(elementType, child.getId());
    }

    @Test
    void existingEmptyKeyCompoundsArePreservedLiterally() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[{'':1},{'':{k:2}},{'':3,other:4},5]");
        assertEquals(1, assertInstanceOf(ICompoundTag.class, list.get(0)).getInt(""));
        assertEquals(2, assertInstanceOf(ICompoundTag.class, list.get(1)).getCompound("").getInt("k"));
        ICompoundTag multipleKeys = assertInstanceOf(ICompoundTag.class, list.get(2));
        assertEquals(2, multipleKeys.getData().size());
        assertEquals(3, multipleKeys.getInt(""));
        assertEquals(4, multipleKeys.getInt("other"));
        assertEquals(5, wrappedValue(list, 3).getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[1,'x']", "['a',{b:3}]", "[{},[1]]", "[[1,'x'],{},[2]]",
            "[[],1]", "[{'':1},2]", "[true,'false',]", "[1b,2s,3,4l,5f,6d]",
            "[[B;1b],[I;2],[L;3l]]", "[{a:[1,'x']},'y']"})
    void binaryAndTextRoundTripsPreserveStoredStructure(String input) throws Exception
    {
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT("{items:" + input + "}");
        root.setName("");
        byte[] binary = write(root);
        ICompoundTag decoded = read(binary);
        assertArrayEquals(binary, write(decoded));
        String text = new SNBTSerializer().serialize(decoded);
        ICompoundTag reparsed = NBTFactory.parseCompoundFromSNBT(text);
        reparsed.setName("");
        assertArrayEquals(binary, write(reparsed));
        assertEquals(text, new SNBTSerializer().serialize(reparsed));
    }

    static Stream<Arguments> binaryFixtures()
    {
        // Hand-encoded, uncompressed compound roots with a named list "items".
        // Each compound-list element ends with END; only wrapped values have an empty-key header.
        return Stream.of(
                Arguments.of("[1,'x']", "0a0000 0900056974656d73 0a00000002 0300000000000100 08000000017800 00"),
                Arguments.of("['a',{b:3}]", "0a0000 0900056974656d73 0a00000002 08000000016100 030001620000000300 00"),
                Arguments.of("[{},[1]]", "0a0000 0900056974656d73 0a00000002 00 09000003000000010000000100 00"),
                Arguments.of("[[1],['x']]", "0a0000 0900056974656d73 0900000002 030000000100000001 0800000001000178 00"));
    }

    @ParameterizedTest
    @MethodSource("binaryFixtures")
    void matchesIndependentBinaryFixtures(String input, String hex) throws Exception
    {
        byte[] fixture = HexFormat.of().parseHex(hex.replace(" ", ""));
        ICompoundTag parsed = NBTFactory.parseCompoundFromSNBT("{items:" + input + "}");
        parsed.setName("");
        assertArrayEquals(fixture, write(parsed));
        ICompoundTag decoded = read(fixture);
        assertEquals(new SNBTSerializer().serialize(parsed), new SNBTSerializer().serialize(decoded));
        assertArrayEquals(fixture, write(decoded));
    }

    @Test
    void serializerExposesStorageWrappers() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[1,'x',{b:3}]");
        assertEquals("[ {\"\": 1}, {\"\": x}, {b: 3} ]", new SNBTSerializer().serialize(list));
    }

    @Test
    void modelStillRejectsRawMixedMutation() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[1,'x']");
        assertThrows(IllegalArgumentException.class, () -> ((Collection_Tag) list).addTag(new Tag_String("", "y")));
        assertEquals(2, list.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[,]", "[1,,]", "[1,'x',,]", "[1 'x']", "[1,'x'", "[1,'x'] extra",
            "[B;1b,'x']", "[I;1,'x']", "[L;1l,'x']", "[B;[1,'x']]", "[1,{a:2,a:3}]"})
    void rejectsMalformedListsAndMixedTypedArrays(String input)
    {
        assertThrows(SNBTException.class, () -> new SNBTParser(input).parse());
    }

    @ParameterizedTest
    @CsvSource(value = {"[1,'x']|2", "[[],1]|3", "[{},[1]]|3", "[[1,'x'],0]|4",
            "{a:[[],1]}|4", "[{a:[[],1]},0]|5", "[[1,'x'],[2,'y']]|3"}, delimiter = '|')
    void depthLimitIncludesGeneratedWrappersAndTheirDescendants(String input, int storageDepth)
            throws SNBTException
    {
        ITag<?> parsed = new SNBTParser(input, limits(storageDepth, 4096, 100, 100)).parse();
        assertDoesNotThrow(() -> new SNBTSerializer(limits(storageDepth, 8192, 100, 100)).serialize(parsed));
        SNBTException error = assertThrows(SNBTException.class,
                () -> new SNBTParser(input, limits(storageDepth - 1, 4096, 100, 100)).parse());
        assertTrue(error.getMessage().contains("depth limit"), error::getMessage);
    }

    @Test
    void tagBudgetIncludesEveryGeneratedWrapper() throws SNBTException
    {
        assertNotNull(new SNBTParser("[1,'x']", limits(2, 4096, 5, 2)).parse());
        SNBTException error = assertThrows(SNBTException.class,
                () -> new SNBTParser("[1,'x']", limits(2, 4096, 4, 2)).parse());
        assertTrue(error.getMessage().contains("tag count limit"), error::getMessage);
        // The original compound is already counted; only the integer needs a wrapper.
        assertNotNull(new SNBTParser("[{},1]", limits(2, 4096, 4, 2)).parse());
    }

    @Test
    void allocationBudgetIncludesGeneratedWrappers() throws SNBTException
    {
        // [1,'x']: 28 text bytes + 3*64 tag bytes + 2*32 list slots = 284.
        // Each wrapper adds a 64-byte tag and 64 bytes for its compound member.
        assertNotNull(new SNBTParser("[1,'x']", limits(2, 540, 5, 2)).parse());
        SNBTException error = assertThrows(SNBTException.class,
                () -> new SNBTParser("[1,'x']", limits(2, 539, 5, 2)).parse());
        assertTrue(error.getMessage().contains("allocation limit"), error::getMessage);
    }

    @Test
    void collectionLengthStillLimitsMixedLists() throws SNBTException
    {
        assertNotNull(new SNBTParser("[1,'x']", limits(2, 4096, 5, 2)).parse());
        SNBTException error = assertThrows(SNBTException.class,
                () -> new SNBTParser("[1,'x']", limits(2, 4096, 5, 1)).parse());
        assertTrue(error.getMessage().contains("collection length"), error::getMessage);
    }

    private static ITag<?> wrappedValue(IListTag list, int index)
    {
        ICompoundTag wrapper = assertInstanceOf(ICompoundTag.class, list.get(index));
        assertEquals(1, wrapper.getData().size());
        assertTrue(wrapper.hasTag(""));
        ITag<?> value = wrapper.getTag("");
        assertEquals("", value.getName());
        return value;
    }

    private static void assertPayloadEquals(Object expected, Object actual)
    {
        if (expected instanceof byte[] bytes) assertArrayEquals(bytes, (byte[]) actual);
        else if (expected instanceof int[] ints) assertArrayEquals(ints, (int[]) actual);
        else if (expected instanceof long[] longs) assertArrayEquals(longs, (long[]) actual);
        else assertEquals(expected, actual);
    }

    private static byte[] write(ICompoundTag root) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        return bytes.toByteArray();
    }

    private static ICompoundTag read(byte[] bytes) throws IOException
    {
        return new NBTReader(new DataInputStream(new ByteArrayInputStream(bytes))).read();
    }

    private static NBTLimits limits(int depth, long allocation, long tags, int length)
    {
        return new NBTLimits(depth, 4096, allocation, tags, length, 4096);
    }
}
