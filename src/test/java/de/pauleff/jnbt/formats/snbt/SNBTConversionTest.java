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

class SNBTConversionTest
{
    @ParameterizedTest
    @ValueSource(strings = {"true", "1b", "-1s", "256", "4294967296l", "-9223372036854775808l",
            "0.5f", "-0.5d", "1e-300", "5e-324", "0xff", "240ub", "0xffffffffffffffffl", "bool(1)"})
    void nonzeroNumbersAndTrueConvertToByteOne(String argument) throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("bool(" + argument + ")");
        assertEquals(1, tag.getId());
        assertEquals((byte) 1, tag.getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"false", "0b", "0s", "0", "0l", "0f", "-0.0f", "0.0d", "-0e10",
            "0x0", "0b0", "1e-999", "bool(0)"})
    void zeroNumbersAndFalseConvertToByteZero(String argument) throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("bool(" + argument + ")");
        assertEquals(1, tag.getId());
        assertEquals((byte) 0, tag.getData());
    }

    static Stream<Arguments> uuids()
    {
        return Stream.of(
                Arguments.of("'123e4567-e89b-12d3-a456-426614174000'", new int[]{306070887, -392490285, -1537850778, 337068032}),
                Arguments.of("\"123E4567-E89B-12D3-A456-426614174000\"", new int[]{306070887, -392490285, -1537850778, 337068032}),
                Arguments.of("'00000000-0000-0000-0000-000000000000'", new int[]{0, 0, 0, 0}),
                Arguments.of("'ffffffff-ffff-ffff-ffff-ffffffffffff'", new int[]{-1, -1, -1, -1}),
                Arguments.of("'80000000-0000-0000-8000-000000000000'", new int[]{Integer.MIN_VALUE, 0, Integer.MIN_VALUE, 0}),
                Arguments.of("'1-1-1-1-1'", new int[]{1, 65537, 65536, 1}),
                Arguments.of("'\\x31-1-1-1-1'", new int[]{1, 65537, 65536, 1}),
                Arguments.of("abcdef00-0000-0000-0000-000000000001", new int[]{0xabcdef00, 0, 0, 1}),
                Arguments.of("'+0000000-0000-0000-0000-000000000000'", new int[]{0, 0, 0, 0}));
    }

    @ParameterizedTest
    @MethodSource("uuids")
    void uuidUsesTheMinecraftIntegerOrder(String argument, int[] expected) throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT("uuid(" + argument + ")");
        assertEquals(11, tag.getId());
        assertArrayEquals(expected, assertInstanceOf(int[].class, tag.getData()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bool ( true )", "bool(1,)", "bool(bool(bool(1),),)", "bool\n(\t1\n)"})
    void allowsWhitespaceAndOneTrailingComma(String input) throws SNBTException
    {
        assertEquals((byte) 1, NBTFactory.parseFromSNBT(input).getData());
    }

    @Test
    void uuidAllowsATrailingComma() throws SNBTException
    {
        assertArrayEquals(new int[]{0, 0, 0, 0}, (int[]) NBTFactory.parseFromSNBT(
                "uuid ( '00000000-0000-0000-0000-000000000000' , )").getData());
    }

    @Test
    void conversionsWorkInCompoundsAndBothListForms() throws SNBTException
    {
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT("{flag:bool(256),id:uuid('1-1-1-1-1'),after:2}");
        assertEquals((byte) 1, compound.getByte("flag"));
        assertArrayEquals(new int[]{1, 65537, 65536, 1}, compound.getIntArray("id"));
        assertEquals(2, compound.getInt("after"));
        IListTag bytes = NBTFactory.parseListFromSNBT("[bool(1),bool(0),true]");
        assertEquals(1, bytes.getListTypeID());
        assertEquals((byte) 1, bytes.get(0).getData());
        assertEquals((byte) 0, bytes.get(1).getData());
        IListTag mixed = NBTFactory.parseListFromSNBT("[bool(1),uuid('1-1-1-1-1'),'x']");
        assertEquals(10, mixed.getListTypeID());
        assertEquals((byte) 1, assertInstanceOf(ICompoundTag.class, mixed.get(0)).getByte(""));
        assertArrayEquals(new int[]{1, 65537, 65536, 1}, assertInstanceOf(ICompoundTag.class, mixed.get(1)).getIntArray(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bool", "uuid", "bool(1)", "uuid('1-1-1-1-1')"})
    void quotedNamesAndCallTextStayStrings(String text) throws SNBTException
    {
        String serialized = NBTFactory.toSNBT(NBTFactory.createString("", text));
        assertEquals(text, NBTFactory.parseFromSNBT(serialized).getData());
        assertEquals(text, NBTFactory.parseFromSNBT('"' + text + '"').getData());
    }

    @Test
    void bareNamesRemainStringsAndCompoundKeys() throws SNBTException
    {
        assertEquals("bool", NBTFactory.parseFromSNBT("bool").getData());
        assertEquals("uuid", NBTFactory.parseFromSNBT("uuid").getData());
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT("{bool:bool(0),uuid:uuid('1-1-1-1-1')}");
        assertEquals((byte) 0, compound.getByte("bool"));
        assertArrayEquals(new int[]{1, 65537, 65536, 1}, compound.getIntArray("uuid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"bool()", "bool(1,2)", "bool(1,,)", "bool(,)", "bool(1", "bool 1)",
            "bool(1))", "bool(1) extra", "BOOL(1)", "foo(1)", "'bool'(1)", "bool('true')", "bool(NaN)",
            "bool({})", "bool([])", "bool([B;1])", "bool(uuid('1-1-1-1-1'))", "bool(1e309)",
            "uuid()", "uuid(1)", "uuid(true)", "uuid({})", "uuid(bool(1))", "uuid(uuid('1-1-1-1-1'))",
            "uuid('')", "uuid('bad')", "uuid('00000000000000000000000000000000')", "uuid('1-1-1-1-1',2)",
            "[B;bool(1)]", "[I;bool(1)]", "[L;bool(1)]", "[I;uuid('1-1-1-1-1')]", "{bool(1):2}"})
    void rejectsInvalidCallsAndArguments(String input)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"uuid('bad')", "bool('true')", "foo(1)"})
    void conversionFailuresPointToTheCall(String input)
    {
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser("{n:" + input + "}").parse());
        assertEquals(3, error.getPosition());
    }

    @Test
    void uuidErrorsRetainTheirCause()
    {
        SNBTException error = assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("uuid('bad')"));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    static Stream<Arguments> binaryFixtures()
    {
        return Stream.of(
                Arguments.of("bool(256)", "0a0000 0100016e 01 00"),
                Arguments.of("bool(-0.0d)", "0a0000 0100016e 00 00"),
                Arguments.of("bool(0.5d)", "0a0000 0100016e 01 00"),
                Arguments.of("uuid('123e4567-e89b-12d3-a456-426614174000')",
                        "0a0000 0b00016e 00000004 123e4567 e89b12d3 a4564266 14174000 00"),
                Arguments.of("uuid('ffffffff-ffff-ffff-ffff-ffffffffffff')",
                        "0a0000 0b00016e 00000004 ffffffff ffffffff ffffffff ffffffff 00"));
    }

    @ParameterizedTest
    @MethodSource("binaryFixtures")
    void binaryAndTextRoundTripsMatchIndependentBytes(String input, String hex) throws Exception
    {
        byte[] fixture = HexFormat.of().parseHex(hex.replace(" ", ""));
        ICompoundTag parsed = NBTFactory.parseCompoundFromSNBT("{n:" + input + "}");
        parsed.setName("");
        assertArrayEquals(fixture, write(parsed));
        ICompoundTag decoded = new NBTReader(new DataInputStream(new ByteArrayInputStream(fixture))).read();
        String text = NBTFactory.toSNBT(decoded);
        assertFalse(text.contains("bool("));
        assertFalse(text.contains("uuid("));
        ICompoundTag reparsed = NBTFactory.parseCompoundFromSNBT(text);
        reparsed.setName("");
        assertArrayEquals(fixture, write(reparsed));
    }

    @ParameterizedTest
    @CsvSource(value = {"bool(1)|188|2|0", "bool(bool(1))|308|3|0", "uuid('00000000-0000-0000-0000-000000000000')|352|2|4"}, delimiter = '|')
    void argumentsAndEvaluationRespectBudgets(String input, long allocation, long tags, int length) throws SNBTException
    {
        assertNotNull(new SNBTParser(input, limits(allocation, tags, length)).parse());
        SNBTException count = assertThrows(SNBTException.class,
                () -> new SNBTParser(input, limits(4096, tags - 1, length)).parse());
        assertTrue(count.getMessage().contains("tag count limit"), count::getMessage);
        SNBTException memory = assertThrows(SNBTException.class,
                () -> new SNBTParser(input, limits(allocation - 1, tags, length)).parse());
        assertTrue(memory.getMessage().contains("allocation limit"), memory::getMessage);
    }

    @Test
    void uuidChecksTheOutputArrayLengthBeforeAllocation()
    {
        SNBTException error = assertThrows(SNBTException.class,
                () -> new SNBTParser("uuid('1-1-1-1-1')", limits(4096, 2, 3)).parse());
        assertTrue(error.getMessage().contains("collection length"), error::getMessage);
    }

    @Test
    void deepConversionsDoNotUseTheJavaCallStack() throws SNBTException
    {
        String input = "bool(".repeat(2_000) + "1" + ")".repeat(2_000);
        assertEquals((byte) 1, new SNBTParser(input).parse().getData());
    }

    @Test
    void tokenizerPreservesNamesQuotesAndParenthesisPositions() throws SNBTException
    {
        SNBTTokenizer tokenizer = new SNBTTokenizer(" bool(1)");
        assertEquals(SNBTTokenizer.TokenType.STRING, tokenizer.nextToken().type());
        SNBTTokenizer.Token open = tokenizer.nextToken();
        assertEquals("LPAREN", open.type().name());
        assertEquals(5, open.position());
        assertEquals(SNBTTokenizer.TokenType.NUMBER, tokenizer.nextToken().type());
        SNBTTokenizer.Token close = tokenizer.nextToken();
        assertEquals("RPAREN", close.type().name());
        assertEquals(7, close.position());
        assertEquals(SNBTTokenizer.TokenType.EOF, tokenizer.nextToken().type());
    }

    private static byte[] write(ICompoundTag root) throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        return bytes.toByteArray();
    }

    private static NBTLimits limits(long allocation, long tags, int length)
    {
        return new NBTLimits(1, 4096, allocation, tags, length, 4096);
    }
}
