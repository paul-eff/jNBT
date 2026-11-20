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

class SNBTNumericWhitespaceTest
{
    static Stream<Arguments> numbers()
    {
        // Values and whitespace boundaries verified with the official Minecraft 26.2 parser.
        return Stream.of(
                Arguments.of("+ 1", 1), Arguments.of("- 1", -1),
                Arguments.of("0 x FF", 255), Arguments.of("0x FF", 255), Arguments.of("0 xFF", 255),
                Arguments.of("0 b 101", 5), Arguments.of("0b 101", 5), Arguments.of("0 b101", 5),
                Arguments.of("0 b", (byte) 0), Arguments.of("- 0 b", (byte) 0),
                Arguments.of("1 b", (byte) 1), Arguments.of("1 s", (short) 1),
                Arguments.of("1 s i", 1), Arguments.of("1u b", (byte) 1),
                Arguments.of("255 u b", (byte) -1), Arguments.of("65535 u s", (short) -1),
                Arguments.of("4294967295 u i", -1), Arguments.of("18446744073709551615 u l", -1L),
                Arguments.of("- 0 X 80 S B", (byte) -128), Arguments.of("0 B 1111_1111 b", (byte) -1),
                Arguments.of("0xAb", 171), Arguments.of("0xA b", (byte) 10),
                Arguments.of("0xFF b", (byte) -1), Arguments.of("0xFF s", (short) 255),
                Arguments.of("1 . 5", 1.5d), Arguments.of("1. 5", 1.5d), Arguments.of("1 .5", 1.5d),
                Arguments.of(". 5", 0.5d), Arguments.of("1 .", 1d),
                Arguments.of("1 . e 2", 100d), Arguments.of("1 e + 2", 100d),
                Arguments.of("1e +2", 100d), Arguments.of("1 e- 2", 0.01d),
                Arguments.of("1 e 2 f", 100f), Arguments.of("1_2 . 3_4__5 f", 12.345f),
                Arguments.of(". 5_0 E + 1_0 d", 5e9d), Arguments.of("00 . 5", 0.5d),
                Arguments.of("00 f", 0f), Arguments.of("00 e 2", 0d),
                // Preserve jNBT's existing bit-exact negative-zero policy.
                Arguments.of("- 0 . 0 f", -0f), Arguments.of("- 0 e + 2", -0d));
    }

    @ParameterizedTest
    @MethodSource("numbers")
    void parsesExactValuesAndTypesAcrossContainers(String text, Object expected) throws SNBTException
    {
        assertNumber(expected, NBTFactory.parseFromSNBT(text));
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT("{n:" + text + ",after:2}");
        assertNumber(expected, compound.getTag("n"));
        assertEquals(2, compound.getInt("after"));
        IListTag homogeneous = NBTFactory.parseListFromSNBT("[" + text + "," + text + ",]");
        assertNumber(expected, homogeneous.get(0));
        assertNumber(expected, homogeneous.get(1));
        IListTag mixed = NBTFactory.parseListFromSNBT("[" + text + ",'text']");
        assertNumber(expected, assertInstanceOf(ICompoundTag.class, mixed.get(0)).getTag(""));
        assertNumber(expected, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(compound.getTag("n"))));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\r", "\n", "\f", "\u001c", "\u2003", "\u2028"})
    void acceptsJavaWhitespaceBetweenComponents(String space) throws SNBTException
    {
        assertNumber((byte) -128, NBTFactory.parseFromSNBT(String.join(space, "-", "0", "x", "80", "s", "b")));
        assertNumber(125f, NBTFactory.parseFromSNBT(String.join(space, "1", ".", "25", "e", "+", "2", "f")));
        assertNumber((byte) 0, NBTFactory.parseFromSNBT("0" + space + "b" + space));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\u00a0", "\u0085", "\u2007", "\u202f", "\u200b"})
    void rejectsNonWhitespaceSeparators(String space)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("1" + space + "b"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1 2", "0 0", "0 1", "0 0 . 5", "0 x F F", "0 b 1 0", "1_ 2", "1 _2",
            "1 _ 2", "1. 2_", "1 . _2", "1 e _2", "1 e 2_", "1 e 2 3", "1 e + + 2", "1 e 2 .0",
            "0 x _FF", "0 b _1", "0 b 102", "0 x GG", "0 x", "0 b s", "0 b u b", "1 u", "1 s f",
            "1 b s", "1 . 0 b", "1 e 2 u i", "00 i", "0_0 i", "01 b", "0xA b s", "0xF F f"})
    void rejectsSplitDigitRunsAndMalformedComponents(String text)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(text));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("{n:" + text + "}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"128 s b", "256 u b", "32768 s s", "65536 u s", "2147483648 s i",
            "4294967296 u i", "9223372036854775808 s l", "18446744073709551616 u l", "- 0 x FF",
            "- 0 u b", "0 x FF s b", "1 e 309", "1 e 39 f"})
    void rangeFailuresRetainTheLiteralPositionAndCause(String text)
    {
        SNBTException error = assertThrows(SNBTException.class,
                () -> NBTFactory.parseFromSNBT("{n:" + text + ",after:1}"));
        assertEquals(3, error.getPosition());
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    @Test
    void typedArraysInferSuffixesAndWidenAfterSourceRangeChecks() throws SNBTException
    {
        assertArrayEquals(new byte[]{-1, -128, 1, 0},
                (byte[]) NBTFactory.parseFromSNBT("[B;0 x FF,- 128,1 u b,0 b]").getData());
        assertArrayEquals(new int[]{-1, 255, -1},
                (int[]) NBTFactory.parseFromSNBT("[I;0xFF b,0 x FF,65535 u s]").getData());
        assertArrayEquals(new long[]{2147483648L, -1, -1},
                (long[]) NBTFactory.parseFromSNBT("[L;+ 2147483648,0 x FFFF_FFFF u i,18446744073709551615 u l]").getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[B;128 s b]", "[I;1 l]", "[L;1 e 2]", "[B;1 2]", "[I;0xF F]", "[B;bool(1 b)]"})
    void typedArraysStillRejectInvalidTypesAndRanges(String input)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(input));
    }

    @Test
    void whitespaceNumbersWorkInsideConversions() throws SNBTException
    {
        assertNumber((byte) 1, NBTFactory.parseFromSNBT("bool(0 x FF b,)"));
        assertNumber((byte) 0, NBTFactory.parseFromSNBT("bool(- 0 . 0 f)"));
        assertNumber((byte) 1, NBTFactory.parseFromSNBT("bool(bool(1 e - 2))"));
    }

    @Test
    void quotedTextAndKeysKeepTheirOwnGrammar() throws SNBTException
    {
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT("{'0 x FF':'1 e + 2',0xFF:'0 x FF'}");
        assertEquals("1 e + 2", root.getString("0 x FF"));
        assertEquals("0 x FF", root.getString("0xFF"));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("{0 x FF:1}"));
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT("foo bar"));
    }

    @Test
    void tokenizerPreservesInternalWhitespaceAndStopsAtTheDelimiter() throws SNBTException
    {
        SNBTTokenizer tokenizer = new SNBTTokenizer("  - 0 x 80 s b \t,after");
        SNBTTokenizer.Token number = tokenizer.nextToken();
        assertEquals(SNBTTokenizer.TokenType.NUMBER, number.type());
        assertEquals("- 0 x 80 s b", number.value());
        assertEquals(2, number.position());
        assertEquals(SNBTTokenizer.TokenType.COMMA, tokenizer.nextToken().type());
        assertEquals("after", tokenizer.nextToken().value());
    }

    static Stream<Arguments> binaryFixtures()
    {
        return Stream.of(
                Arguments.of("0 x FF b", "0a0000 0100016e ff 00"),
                Arguments.of("0 x FF s", "0a0000 0200016e 00ff 00"),
                Arguments.of("0 x FFFF_FFFF", "0a0000 0300016e ffffffff 00"),
                Arguments.of("18446744073709551615 u l", "0a0000 0400016e ffffffffffffffff 00"),
                Arguments.of("1_2 . 5 f", "0a0000 0500016e 41480000 00"),
                Arguments.of("1 . 25 e + 2", "0a0000 0600016e 405f400000000000 00"),
                Arguments.of("[B;0 x FF,- 128]", "0a0000 0700016e 00000002 ff80 00"),
                Arguments.of("[I;0xFF b,0 x FF]", "0a0000 0b00016e 00000002 ffffffff000000ff 00"));
    }

    @ParameterizedTest
    @MethodSource("binaryFixtures")
    void matchesIndependentBinaryFixtures(String text, String hex) throws Exception
    {
        byte[] fixture = HexFormat.of().parseHex(hex.replace(" ", ""));
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT("{n:" + text + "}");
        root.setName("");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        assertArrayEquals(fixture, bytes.toByteArray());
        ICompoundTag decoded = new NBTReader(new DataInputStream(new ByteArrayInputStream(fixture))).read();
        assertEquals(NBTFactory.toSNBT(root), NBTFactory.toSNBT(decoded));
    }

    @Test
    void normalizationRespectsTextAndAllocationLimits() throws SNBTException
    {
        String text = "-" + " ".repeat(20_000) + "1";
        NBTLimits exact = new NBTLimits(1, 4096, 4L * text.length() + 64, 1, 1, text.length());
        assertNumber(-1, new SNBTParser(text, exact).parse());
        NBTLimits shortText = new NBTLimits(1, 4096, 1_000_000, 1, 1, text.length() - 1);
        assertThrows(SNBTException.class, () -> new SNBTParser(text, shortText).parse());
        String floating = "1" + " ".repeat(20_000) + "e - 1";
        NBTLimits shortAllocation = new NBTLimits(1, 4096, 4L * floating.length() + 64, 1, 1, floating.length());
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser(floating, shortAllocation).parse());
        assertTrue(error.getMessage().contains("allocation limit"), error::getMessage);
    }

    private static void assertNumber(Object expected, ITag<?> actual)
    {
        assertEquals(expected.getClass(), actual.getData().getClass());
        assertEquals(expected, actual.getData());
    }
}
