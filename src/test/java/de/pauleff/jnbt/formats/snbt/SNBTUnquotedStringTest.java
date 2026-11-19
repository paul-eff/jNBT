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
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HexFormat;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SNBTUnquotedStringTest
{
    @ParameterizedTest
    @ValueSource(strings = {"foo+bar", "_+", "a.b-c+d", "A0_+.-z", "true+false", "true+", "_1", "B"})
    void acceptsAsciiStringsAcrossValueContexts(String text) throws SNBTException
    {
        ITag<?> scalar = NBTFactory.parseFromSNBT(text);
        assertEquals(8, scalar.getId());
        assertEquals(text, scalar.getData());
        assertEquals(text, NBTFactory.parseCompoundFromSNBT("{n:" + text + "}").getString("n"));
        IListTag list = NBTFactory.parseListFromSNBT("[" + text + "," + text + "]");
        assertEquals(8, list.getListTypeID());
        assertEquals(text, list.get(0).getData());
        assertEquals(text, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(scalar)).getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"foo+bar", "+foo", "-foo", ".foo", "1foo", "+", "-", ".", "123", "012",
            "0xFF", "0xGG", "1e309", "2147483648", "1_", "true", "TRUE", "false", "bool", "uuid", "B"})
    void keysRemainLiteralRegardlessOfNumericOrBooleanAppearance(String key) throws SNBTException
    {
        // Both the first key and a key after a comma must use the key grammar.
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT("{" + key + ":1,n:{" + key + ":2},}");
        assertEquals(1, root.getInt(key));
        assertEquals(2, root.getCompound("n").getInt(key));
        ICompoundTag afterComma = NBTFactory.parseCompoundFromSNBT("{n:0," + key + ":3}");
        assertEquals(3, afterComma.getInt(key));
        ICompoundTag roundTrip = NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(root));
        assertEquals(1, roundTrip.getInt(key));
        assertEquals(2, roundTrip.getCompound("n").getInt(key));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+foo", "-foo", ".foo", "1foo", "+", "-", ".", "012", "0xGG", "1e309",
            "2147483648", "1_", "1+2"})
    void numericLookingValuesStillRequireValidNumbers(String text)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(text));
        assertThrows(SNBTException.class, () -> NBTFactory.parseCompoundFromSNBT("{n:" + text + "}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"é", "aé", "中", "a中", "١", "a١", "a\u0301", "😀", "a😀", "a$", "a/", "a@", "a#", "a!", "a~", "a\\b"})
    void rejectsCharactersOutsideTheUnquotedAlphabet(String text) throws SNBTException
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(text));
        assertThrows(SNBTException.class, () -> NBTFactory.parseCompoundFromSNBT("{" + text + ":1}"));
        String quoted = NBTFactory.toSNBT(NBTFactory.createString("", text));
        assertEquals(text, NBTFactory.parseFromSNBT(quoted).getData());
        ICompoundTag root = NBTFactory.createCompound().addString(text, text);
        assertEquals(text, NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(root)).getString(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRUE", "True", "tRuE", "FALSE", "False", "fAlSe"})
    void booleansIgnoreCaseOnlyInUnquotedValues(String text) throws SNBTException
    {
        byte expected = (byte) (text.equalsIgnoreCase("true") ? 1 : 0);
        ITag<?> scalar = NBTFactory.parseFromSNBT(text);
        assertEquals(1, scalar.getId());
        assertEquals(expected, scalar.getData());
        assertEquals(expected, NBTFactory.parseFromSNBT("bool(" + text + ")").getData());
        for (String quote : new String[]{"'", "\""})
        {
            ITag<?> quoted = NBTFactory.parseFromSNBT(quote + text + quote);
            assertEquals(8, quoted.getId());
            assertEquals(text, quoted.getData());
        }
        assertEquals(text, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(NBTFactory.createString("", text))).getData());
        ICompoundTag root = NBTFactory.createCompound().addString(text, text);
        assertEquals(text, NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(root)).getString(text));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("[B;" + text + "]"));
    }

    @Test
    void booleanCaseVariantsRemainInOneByteList() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[true,TRUE,False,fAlSe]");
        assertEquals(1, list.getListTypeID());
        assertEquals((byte) 1, list.get(1).getData());
        assertEquals((byte) 0, list.get(2).getData());
    }

    @Test
    void keyCaseRemainsSignificant() throws SNBTException
    {
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT("{true:1,TRUE:2,foo+bar:3,FOO+BAR:4}");
        assertEquals(4, root.size());
        assertEquals(1, root.getInt("true"));
        assertEquals(2, root.getInt("TRUE"));
        assertEquals(3, root.getInt("foo+bar"));
        assertEquals(4, root.getInt("FOO+BAR"));
    }

    @Test
    void literalKeysDoNotChangeValuesArraysOrConversions() throws SNBTException
    {
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT(
                "{123:0xFF,012:[I;1b,2s],+foo:bool(TRUE),0xGG:{1e309:uuid('1-2-3-4-5')},B:foo+bar}");
        assertEquals(255, root.getInt("123"));
        assertArrayEquals(new int[]{1, 2}, root.getIntArray("012"));
        assertEquals((byte) 1, root.getByte("+foo"));
        assertEquals(11, root.getCompound("0xGG").getTag("1e309").getId());
        assertEquals("foo+bar", root.getString("B"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{123:1,'123':2}", "{foo+bar:1,\"foo+bar\":2}", "{TRUE:1,'TRUE':2}",
            "{a b:1}", "{foo+bar(1):2}", "foo+bar(1)", "TRUE(1)", "[B;foo+bar]"})
    void rejectsDuplicatesAndInvalidContexts(String input)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(input));
    }

    @Test
    void plusSignRemainsPartOfTheStringToken() throws SNBTException
    {
        SNBTTokenizer tokenizer = new SNBTTokenizer("  foo+bar,");
        SNBTTokenizer.Token token = tokenizer.nextToken();
        assertEquals(SNBTTokenizer.TokenType.STRING, token.type());
        assertEquals("foo+bar", token.value());
        assertEquals(2, token.position());
        assertFalse(token.quoted());
        assertEquals(SNBTTokenizer.TokenType.COMMA, tokenizer.nextToken().type());
    }

    @Test
    void reportsInvalidKeyAndValuePositions()
    {
        SNBTException key = assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("{a:0,é:1}"));
        assertEquals(5, key.getPosition());
        SNBTException value = assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("{123:1e309}"));
        assertEquals(5, value.getPosition());
        assertInstanceOf(NumberFormatException.class, value.getCause());
    }

    static Stream<Arguments> binaryFixtures()
    {
        // Hand-encoded compound roots; keys and strings retain their original ASCII bytes.
        return Stream.of(
                Arguments.of("{foo+bar:foo+bar}", "0a0000 080007666f6f2b626172 0007666f6f2b626172 00"),
                Arguments.of("{123:1,0xFF:2}", "0a0000 030003313233 00000001 03000430784646 00000002 00"),
                Arguments.of("{TRUE:False,False:'TRUE'}", "0a0000 01000454525545 00 08000546616c7365 000454525545 00"));
    }

    @ParameterizedTest
    @MethodSource("binaryFixtures")
    void matchesIndependentBinaryFixtures(String input, String hex) throws Exception
    {
        byte[] expected = HexFormat.of().parseHex(hex.replace(" ", ""));
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT(input);
        root.setName("");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(output)).write(root);
        assertArrayEquals(expected, output.toByteArray());
        ICompoundTag decoded = new NBTReader(new DataInputStream(new ByteArrayInputStream(expected))).read();
        assertEquals(NBTFactory.toSNBT(root), NBTFactory.toSNBT(decoded));
    }

    @Test
    void longNumericLookingKeysRespectTextAndAllocationLimits() throws SNBTException
    {
        String key = "9".repeat(20_000);
        String input = "{" + key + ":foo+bar}";
        NBTLimits exact = new NBTLimits(1, 1_000_000, 4L * input.length() + 192, 2, 1, input.length());
        ICompoundTag root = assertInstanceOf(ICompoundTag.class, new SNBTParser(input, exact).parse());
        assertEquals("foo+bar", root.getString(key));
        NBTLimits shortText = new NBTLimits(1, 1_000_000, 1_000_000, 2, 1, input.length() - 1);
        assertThrows(SNBTException.class, () -> new SNBTParser(input, shortText).parse());
        NBTLimits shortAllocation = new NBTLimits(1, 1_000_000, 4L * input.length() + 191, 2, 1, input.length());
        assertThrows(SNBTException.class, () -> new SNBTParser(input, shortAllocation).parse());
    }
}
