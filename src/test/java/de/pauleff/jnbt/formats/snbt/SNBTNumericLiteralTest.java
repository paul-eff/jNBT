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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SNBTNumericLiteralTest
{
    static Stream<Arguments> literals()
    {
        // Expected values include ambiguous forms checked with Mojang's 1.21.5 server parser.
        return Stream.of(
                Arguments.of("0xFF", 255), Arguments.of("0XCAFE", 51966),
                Arguments.of("0b101", 5), Arguments.of("0B10_01", 9),
                Arguments.of("1_000", 1000), Arguments.of("1__2", 12),
                Arguments.of("0xAB_CD", 43981), Arguments.of("3i", 3),
                Arguments.of("3SI", 3), Arguments.of("3uI", 3),
                Arguments.of("240ub", (byte) -16), Arguments.of("240UB", (byte) -16),
                Arguments.of("240uB", (byte) -16), Arguments.of("240Ub", (byte) -16),
                Arguments.of("-16sb", (byte) -16), Arguments.of("+255ub", (byte) -1),
                Arguments.of("0x11ub", (byte) 17), Arguments.of("0xffb", 4091),
                Arguments.of("0x1d", 29), Arguments.of("0x1e2", 482),
                Arguments.of("0xfff", 4095), Arguments.of("0xffffffff", -1),
                Arguments.of("0x80000000", Integer.MIN_VALUE),
                Arguments.of("0xffffffffffffffffl", -1L),
                Arguments.of("-0x80sb", Byte.MIN_VALUE), Arguments.of("+0xffub", (byte) -1),
                Arguments.of("0b11111111b", (byte) -1), Arguments.of("0xFFFFs", (short) -1),
                Arguments.of("3ss", (short) 3), Arguments.of("3us", (short) 3),
                Arguments.of("0b", (byte) 0), Arguments.of("-0b", (byte) 0),
                Arguments.of("+0B", (byte) 0), Arguments.of("0b0", 0),
                Arguments.of("0b0b", (byte) 0), Arguments.of("1_0b", (byte) 10),
                Arguments.of("1_2.3_4__5f", 12.345f), Arguments.of("1_2e3_0", 12e30d),
                Arguments.of(".5_0e+1_0", 5e9d), Arguments.of("-0_0.0f", -0.0f),
                Arguments.of("00.5", 0.5d), Arguments.of("01f", 1f),
                Arguments.of("01e1", 10d), Arguments.of("0__0e0", 0d),
                Arguments.of("0x0000000000000000000000000000000000001", 1));
    }

    @ParameterizedTest
    @MethodSource("literals")
    void parsesLiteralsWithExactTypesInEveryValueContext(String literal, Object expected) throws Exception
    {
        assertNumber(expected, NBTFactory.parseFromSNBT(literal));
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT("{n:" + literal + ",after:1}");
        assertNumber(expected, compound.getTag("n"));
        assertEquals(1, compound.getInt("after"));
        IListTag homogeneous = NBTFactory.parseListFromSNBT("[" + literal + "," + literal + ",]");
        assertNumber(expected, homogeneous.get(0));
        assertNumber(expected, homogeneous.get(1));
        IListTag mixed = NBTFactory.parseListFromSNBT("[" + literal + ",'text']");
        assertEquals(10, mixed.getListTypeID());
        assertNumber(expected, assertInstanceOf(ICompoundTag.class, mixed.get(0)).getTag(""));
        assertNumber(expected, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(compound.getTag("n"))));
    }

    static Stream<Arguments> boundaries()
    {
        List<Arguments> cases = new ArrayList<>();
        char[] suffixes = {'b', 's', 'i', 'l'};
        for (int index = 0; index < suffixes.length; index++)
        {
            int bits = 8 << index;
            char suffix = suffixes[index];
            BigInteger min = BigInteger.ONE.shiftLeft(bits - 1).negate();
            BigInteger max = min.negate().subtract(BigInteger.ONE);
            BigInteger unsignedMax = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);
            for (int radix : new int[]{2, 10, 16})
            {
                cases.add(Arguments.of(literal(min, radix, "s" + suffix), boxed(min.longValue(), suffix)));
                cases.add(Arguments.of(literal(max, radix, "s" + suffix), boxed(max.longValue(), suffix)));
                cases.add(Arguments.of(literal(unsignedMax, radix, "u" + suffix), boxed(-1, suffix)));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest
    @MethodSource("boundaries")
    void acceptsSignedAndUnsignedWidthBoundaries(String literal, Object expected) throws SNBTException
    {
        assertNumber(expected, NBTFactory.parseFromSNBT(literal));
    }

    static Stream<String> overflow()
    {
        List<String> cases = new ArrayList<>();
        char[] suffixes = {'b', 's', 'i', 'l'};
        for (int index = 0; index < suffixes.length; index++)
        {
            int bits = 8 << index;
            char suffix = suffixes[index];
            BigInteger signedOverflow = BigInteger.ONE.shiftLeft(bits - 1);
            for (int radix : new int[]{2, 10, 16})
            {
                cases.add(literal(signedOverflow, radix, "s" + suffix));
                cases.add(literal(signedOverflow.negate().subtract(BigInteger.ONE), radix, "s" + suffix));
                cases.add(literal(BigInteger.ONE.shiftLeft(bits), radix, "u" + suffix));
                cases.add(literal(BigInteger.ONE.negate(), radix, "u" + suffix));
                cases.add(literal(BigInteger.ZERO, radix, "u" + suffix).replaceFirst("^", "-"));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest
    @MethodSource("overflow")
    void rejectsOutOfRangeValuesWithoutTruncation(String literal)
    {
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser("{n:" + literal + "}").parse());
        assertEquals(3, error.getPosition());
        assertInstanceOf(NumberFormatException.class, error.getCause());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0x", "0b_1", "0x_FF", "0xFF_", "0b102", "0xGG", "1_", "1_.0",
            "1._0", "1e_2", "1_e2", "1e2_", "1e+_2", "+_1", ".", "+.", "1..0", "1e2e3",
            "1e2i", "1.0b", "1.0si", "3u", "3sF", "3bs", "3bfoo", "1_000x", "0x1p2",
            "0x1.0", "00", "000", "01", "0_0", "0_1", "01s", "00i", "-0xff", "-0b1",
            "0x100000000", "0b100000000000000000000000000000000", "2147483648", "1e309", "1e39f",
            "1_0e3_09", "1\u0661", "0x\uff21", "0b\uff11", "1\u0130", "1\u0131", "1\u017f", "1\uff49"})
    void rejectsMalformedOrOverflowingLiteralsAtTheirStart(String literal)
    {
        SNBTException error = assertThrows(SNBTException.class,
                () -> new SNBTParser("{n:" + literal + ",after:1}").parse());
        assertEquals(3, error.getPosition());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0xFF", "240ub", "1_000", "3i", "00", "1e309", "NaN", "Infinity"})
    void quotedNumericTextStaysAString(String literal) throws SNBTException
    {
        ITag<?> value = NBTFactory.parseFromSNBT("'" + literal + "'");
        assertEquals(8, value.getId());
        assertEquals(literal, value.getData());
        assertEquals(literal, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(value)).getData());
    }

    @Test
    void unquotedNamesAndNonFiniteWordsKeepTheirExistingMeaning() throws SNBTException
    {
        for (String text : new String[]{"_1", "NaN", "Infinity", "ordinary"})
            assertEquals(text, NBTFactory.parseFromSNBT(text).getData());
        assertEquals(1, NBTFactory.parseCompoundFromSNBT("{'0xFF':1}").getInt("0xFF"));
        assertEquals(1, NBTFactory.parseCompoundFromSNBT("{0xFF:1}").getInt("0xFF"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0xFF", "0b101", "240ub", "1_2.3_4__5f", "3SI"})
    void tokenizerKeepsTheWholeNumericLiteralAndPosition(String literal) throws SNBTException
    {
        SNBTTokenizer tokenizer = new SNBTTokenizer("  " + literal + ",");
        SNBTTokenizer.Token token = tokenizer.nextToken();
        assertEquals(SNBTTokenizer.TokenType.NUMBER, token.type());
        assertEquals(literal, token.value());
        assertEquals(2, token.position());
        assertFalse(token.quoted());
        assertEquals(SNBTTokenizer.TokenType.COMMA, tokenizer.nextToken().type());
    }

    @Test
    void explicitlyTypedArrayEntriesUseTheModernIntegerGrammar() throws SNBTException
    {
        assertArrayEquals(new byte[]{-16, -1, -128},
                (byte[]) NBTFactory.parseFromSNBT("[B;240ub,0xffub,-0x80sb]").getData());
        assertArrayEquals(new int[]{255, 5, 1000, -1},
                (int[]) NBTFactory.parseFromSNBT("[I;0xFF,0b101,1_000i,4294967295ui]").getData());
        assertArrayEquals(new long[]{-1, Long.MIN_VALUE},
                (long[]) NBTFactory.parseFromSNBT("[L;18446744073709551615ul,-0x8000000000000000sl]").getData());
        assertArrayEquals(new byte[]{1}, (byte[]) NBTFactory.parseFromSNBT("[B;1]").getData());
        assertArrayEquals(new int[]{1}, (int[]) NBTFactory.parseFromSNBT("[I;1b]").getData());
        assertArrayEquals(new long[]{1}, (long[]) NBTFactory.parseFromSNBT("[L;1]").getData());
    }

    static Stream<Arguments> binaryFixtures()
    {
        // Hand-encoded compound root with one field "n". Payloads are independent of jNBT output.
        return Stream.of(
                Arguments.of("240ub", "0a0000 0100016e f0 00"),
                Arguments.of("65535us", "0a0000 0200016e ffff 00"),
                Arguments.of("0xffffffff", "0a0000 0300016e ffffffff 00"),
                Arguments.of("18446744073709551615ul", "0a0000 0400016e ffffffffffffffff 00"),
                Arguments.of("1_2.5f", "0a0000 0500016e 41480000 00"),
                Arguments.of("1_2.5", "0a0000 0600016e 4029000000000000 00"));
    }

    @ParameterizedTest
    @MethodSource("binaryFixtures")
    void matchesIndependentBinaryPayloads(String literal, String hex) throws Exception
    {
        byte[] fixture = HexFormat.of().parseHex(hex.replace(" ", ""));
        ICompoundTag root = NBTFactory.parseCompoundFromSNBT("{n:" + literal + "}");
        root.setName("");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new NBTWriter(new DataOutputStream(bytes)).write(root);
        assertArrayEquals(fixture, bytes.toByteArray());
        ICompoundTag decoded = new NBTReader(new DataInputStream(new ByteArrayInputStream(fixture))).read();
        assertNumber(root.getTag("n").getData(), decoded.getTag("n"));
    }

    @Test
    void separatorNormalizationHonorsTheAllocationBudget()
    {
        String text = "1" + "_0".repeat(100) + "e-100";
        NBTLimits limits = new NBTLimits(1, 4096, 4L * text.length() + 64, 1, 1, 4096);
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser(text, limits).parse());
        assertTrue(error.getMessage().contains("allocation limit"), error::getMessage);
    }

    private static void assertNumber(Object expected, ITag<?> actual)
    {
        assertEquals(expected.getClass(), actual.getData().getClass());
        assertEquals(expected, actual.getData());
    }

    private static String literal(BigInteger value, int radix, String suffix)
    {
        String prefix = radix == 16 ? "0x" : radix == 2 ? "0b" : "";
        return (value.signum() < 0 ? "-" : "") + prefix + value.abs().toString(radix) + suffix;
    }

    private static Object boxed(long value, char suffix)
    {
        return switch (suffix)
        {
            case 'b' -> Byte.valueOf((byte) value);
            case 's' -> Short.valueOf((short) value);
            case 'i' -> Integer.valueOf((int) value);
            case 'l' -> Long.valueOf(value);
            default -> throw new IllegalArgumentException();
        };
    }
}
