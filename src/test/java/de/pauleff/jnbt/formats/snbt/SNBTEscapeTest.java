package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.util.NBTTags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SNBTEscapeTest
{
    private static Stream<Arguments> validEscapes()
    {
        return Stream.of(
                Arguments.of("\\x41", "A"), Arguments.of("\\x00", "\0"),
                Arguments.of("\\xff", "ÿ"), Arguments.of("\\xAf", "¯"),
                Arguments.of("\\u2603", "☃"), Arguments.of("\\u00e9", "é"),
                Arguments.of("\\uFFFF", "\uFFFF"),
                Arguments.of("\\U0001F600", "😀"), Arguments.of("\\U00010000", "\uD800\uDC00"),
                Arguments.of("\\U0010FFFF", "\uDBFF\uDFFF"), Arguments.of("\\U00000000", "\0"),
                Arguments.of("\\U00002603", "☃"), Arguments.of("\\N{Snowman}", "☃"),
                Arguments.of("\\N{snowman}", "☃"), Arguments.of("\\N{GRINNING FACE}", "😀"),
                Arguments.of("\\N{LATIN CAPITAL LETTER A}", "A"),
                Arguments.of("\\n", "\n"), Arguments.of("\\t", "\t"),
                Arguments.of("\\r", "\r"), Arguments.of("\\b", "\b"),
                Arguments.of("\\f", "\f"), Arguments.of("\\s", " "),
                Arguments.of("\\'", "'"), Arguments.of("\\\"", "\""), Arguments.of("\\\\", "\\"),
                Arguments.of("prefix\\x41middle\\N{Snowman}\\U0001F600suffix", "prefixAmiddle☃😀suffix"),
                Arguments.of("\\x414\\u00420\\U000000437", "A4B0C7"),
                Arguments.of("\\x5cq", "\\q"), Arguments.of("\\\\x41", "\\x41"),
                Arguments.of("\\x74rue", "true"), Arguments.of("\\N{LATIN SMALL LETTER F}alse", "false"),
                Arguments.of("\\uD83D\\uDE00", "😀"), Arguments.of("\\uD800", "\uD800"),
                Arguments.of("\\uDC00", "\uDC00"), Arguments.of("\\uD800x\\uDC00", "\uD800x\uDC00"));
    }

    @ParameterizedTest
    @MethodSource("validEscapes")
    void decodesIndependentFixturesInBothQuoteStylesAndAllStringContexts(String escaped, String expected)
            throws SNBTException
    {
        for (char quote : new char[]{'"', '\''})
        {
            String input = quote + escaped + quote;
            SNBTTokenizer tokenizer = new SNBTTokenizer("  " + input);
            SNBTTokenizer.Token token = tokenizer.nextToken();
            assertEquals(SNBTTokenizer.TokenType.STRING, token.type());
            assertTrue(token.quoted());
            assertEquals(2, token.position());
            assertEquals(expected, token.value());
            assertEquals(SNBTTokenizer.TokenType.EOF, tokenizer.nextToken().type());

            ITag<?> tag = NBTFactory.parseFromSNBT(input);
            assertEquals(NBTTags.Tag_String.getId(), tag.getId());
            assertEquals(expected, tag.getData());
            assertEquals(expected, NBTFactory.parseFromSNBT(NBTFactory.toSNBT(tag)).getData());

            ICompoundTag compound = NBTFactory.parseCompoundFromSNBT("{" + input + ":" + input + "}");
            assertEquals(1, compound.size());
            assertEquals(expected, compound.getString(expected));
            assertEquals(expected,
                    NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(compound)).getString(expected));

            IListTag list = NBTFactory.parseListFromSNBT("[" + input + "," + input + "]");
            assertEquals(NBTTags.Tag_String.getId(), list.getListTypeID());
            assertEquals(2, list.size());
            assertEquals(expected, list.get(0).getData());
            assertEquals(expected, list.get(1).getData());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\\q", "\\a", "\\v", "\\0", "\\/", "\\X41", "\\S",
            "\\x", "\\x4", "\\xGG", "\\x+1", "\\x-1", "\\x１0",
            "\\u", "\\u123", "\\u12G4", "\\u+041", "\\u-041", "\\u１２３４",
            "\\U", "\\U0001F60", "\\U0001FG00", "\\U00110000", "\\U7FFFFFFF",
            "\\U80000000", "\\UFFFFFFFF", "\\U-0000001", "\\U+0000041",
            "\\N", "\\NSnowman", "\\N{}", "\\N{NO SUCH CHARACTER}", "\\N{Snowman",
            "\\N{Snowman\\q}", "\\N{Snowman{}}"})
    void rejectsMalformedEscapesAtTheirBackslash(String escaped)
    {
        for (char quote : new char[]{'"', '\''})
        {
            String input = " {x:" + quote + "prefix" + escaped + quote + "}";
            SNBTException error = assertThrows(SNBTException.class, () -> new SNBTParser(input).parse(), input);
            assertEquals(input.indexOf('\\'), error.getPosition(), input);
            assertEquals(input, error.getInput());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"prefix\\", "'prefix\\"})
    void rejectsDanglingBackslashes(String input)
    {
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTTokenizer(input).nextToken());
        assertEquals(input.length() - 1, error.getPosition());
        assertEquals(input, error.getInput());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"\\x", "'\\u12", "\"\\U0001F60", "'\\N{Snowman"})
    void rejectsEscapesTruncatedAtEndOfInput(String input)
    {
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTTokenizer(input).nextToken());
        assertEquals(1, error.getPosition());
        assertEquals(input, error.getInput());
    }

    @Test
    void invalidUnicodeNameRetainsLookupCause()
    {
        SNBTException error = assertThrows(SNBTException.class,
                () -> NBTFactory.parseFromSNBT("\"\\N{NO SUCH CHARACTER}\""));
        assertInstanceOf(IllegalArgumentException.class, error.getCause());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"unterminated", "'unterminated", "\"\\\"", "'\\'"})
    void rejectsUnterminatedStrings(String input)
    {
        SNBTException error = assertThrows(SNBTException.class, () -> new SNBTTokenizer(input).nextToken());
        assertEquals(0, error.getPosition());
    }
}
