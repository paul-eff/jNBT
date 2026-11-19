package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.*;
import de.pauleff.jnbt.core.Collection_Tag;
import de.pauleff.jnbt.formats.snbt.SNBTTokenizer.Token;
import de.pauleff.jnbt.formats.snbt.SNBTTokenizer.TokenType;
import de.pauleff.jnbt.internal.NBTBudget;
import de.pauleff.jnbt.util.NBTTags;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Parses SNBT into NBT tags using {@link NBTLimits#DEFAULT} unless limits are supplied.
 * Mixed lists use compounds with empty-key wrappers. Decimal integers default to signed ranges,
 * while hexadecimal/binary integers default to unsigned ranges. Unsigned values retain their NBT bits.
 * Typed arrays infer missing integer suffixes and accept smaller integer types.
 * Converts bool() to bytes and uuid() to four-int arrays.
 * Unquoted keys retain literal ASCII text, including numeric-looking names.
 * Unquoted boolean values are case-insensitive; quoted values remain strings.
 *
 * @since 1.6.0
 */
public class SNBTParser
{
    private final SNBTTokenizer tokenizer;
    private final NBTLimits limits;
    private Token currentToken;
    private NBTBudget budget;
    private int depth;
    // Container height of the last parsed value, including generated storage wrappers.
    private int valueDepth;

    /**
     * Creates a parser for the given SNBT input string.
     *
     * @param input the SNBT string to parse
     */
    public SNBTParser(String input)
    {
        this(input, NBTLimits.DEFAULT);
    }

    /**
     * Creates an SNBT parser with configurable limits.
     *
     * @param input  the SNBT string to parse, with null treated as empty input
     * @param limits the limits for this operation
     * @throws NullPointerException if limits is null
     */
    public SNBTParser(String input, NBTLimits limits)
    {
        this.limits = java.util.Objects.requireNonNull(limits, "limits");
        this.tokenizer = new SNBTTokenizer(input);
    }

    /**
     * Parses the complete SNBT string into an NBT tag. Duplicate keys within any compound are rejected.
     *
     * @return the parsed NBT tag
     * @throws SNBTException if syntax is invalid or a configured limit is exceeded
     */
    public ITag<?> parse() throws SNBTException
    {
        budget = new NBTBudget(limits);
        depth = 0;
        try
        {
            if (tokenizer.getInput().length() > limits.maxTextLength())
                throw new IllegalArgumentException("SNBT text length limit exceeded: " + limits.maxTextLength());
            budget.allocate(4L * tokenizer.getInput().length());
            advance();
            ITag<?> result = parseValue();

            if (currentToken.type() != TokenType.EOF)
            {
                throw new SNBTException("Unexpected token after parsing completed",
                        currentToken.position(), tokenizer.getInput());
            }

            return result;
        } catch (IllegalArgumentException e)
        {
            throw new SNBTException(e.getMessage(), currentToken == null ? 0 : currentToken.position(),
                    tokenizer.getInput(), e);
        }
    }

    /**
     * Parses any SNBT value (compound, list, array, or primitive).
     *
     * @return the parsed NBT tag
     * @throws SNBTException if parsing fails
     */
    private ITag<?> parseValue() throws SNBTException
    {
        budget.tag();
        valueDepth = 0;
        ITag<?> value = switch (currentToken.type())
        {
            case LBRACE ->
            {
                budget.depth(++depth);
                try
                {
                    yield parseCompound();
                } finally
                {
                    depth--;
                }
            }
            case LBRACKET -> parseListOrArray();
            case STRING -> parseStringValue();
            case NUMBER -> parseNumber();
            default -> throw new SNBTException("Unexpected token: " + currentToken.type(),
                        currentToken.position(), tokenizer.getInput());
        };
        budget.depth(depth + valueDepth);
        return value;
    }

    /**
     * Parses a compound tag in the format {key: value, key2: value2}.
     *
     * @return the parsed compound tag
     * @throws SNBTException if parsing fails
     */
    private ICompoundTag parseCompound() throws SNBTException
    {
        advanceKey();
        ICompoundTag compound = NBTFactory.createCompound();
        int children = 0;
        int childDepth = 0;

        if (currentToken.type() == TokenType.RBRACE)
        {
            advance();
            valueDepth = 1;
            return compound;
        }

        do
        {
            if (currentToken.type() != TokenType.STRING)
            {
                throw new SNBTException("Expected string key in compound",
                        currentToken.position(), tokenizer.getInput());
            }
            String key = currentToken.value();
            if (compound.hasTag(key))
            {
                throw new SNBTException("Duplicate compound key: " + key,
                        currentToken.position(), tokenizer.getInput());
            }
            advance();

            expect(TokenType.COLON);

            budget.length(++children);
            budget.allocate(64);
            ITag<?> value = parseValue();
            childDepth = Math.max(childDepth, valueDepth);
            value.setName(key);
            ((Collection_Tag) compound).addTag(value);

            if (currentToken.type() == TokenType.COMMA)
            {
                advanceKey();
            } else if (currentToken.type() == TokenType.RBRACE)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or '}' in compound",
                        currentToken.position(), tokenizer.getInput());
            }
        } while (currentToken.type() != TokenType.RBRACE);

        expect(TokenType.RBRACE);
        valueDepth = childDepth + 1;
        return compound;
    }

    /**
     * Parses a list or array in formats [value1, value2] or [B; 1b, 2b].
     *
     * @return the parsed list or array tag
     * @throws SNBTException if parsing fails
     */
    private ITag<?> parseListOrArray() throws SNBTException
    {
        expect(TokenType.LBRACKET);

        if (currentToken.type() == TokenType.ARRAY_PREFIX)
        {
            return parseArray();
        } else
        {
            budget.depth(++depth);
            try
            {
                return parseList();
            } finally
            {
                depth--;
            }
        }
    }

    /**
     * Parses a typed array with formats [B; 1b, 2b], [I; 1, 2], or [L; 1l, 2l].
     *
     * @return the parsed array tag
     * @throws SNBTException if parsing fails or array type is unknown
     */
    private ITag<?> parseArray() throws SNBTException
    {
        String prefix = currentToken.value();
        advance();
        expect(TokenType.SEMICOLON);

        return switch (prefix)
        {
            case "B" -> parseByteArray();
            case "I" -> parseIntArray();
            case "L" -> parseLongArray();
            default -> throw new SNBTException("Unknown array prefix: " + prefix,
                    currentToken.position(), tokenizer.getInput());
        };
    }

    /**
     * Parses a byte array, inferring missing byte suffixes.
     *
     * @return the parsed byte array tag
     * @throws SNBTException if parsing fails or values are not bytes
     */
    private ITag<byte[]> parseByteArray() throws SNBTException
    {
        List<Byte> values = new ArrayList<>();
        budget.allocate(16);

        if (currentToken.type() == TokenType.RBRACKET)
        {
            advance();
            return NBTFactory.createByteArray("", new byte[0]);
        }

        do
        {
            budget.length(values.size() + 1);
            budget.allocate(1);
            values.add((byte) parseArrayElement('b'));

            if (currentToken.type() == TokenType.COMMA)
            {
                advance();
            } else if (currentToken.type() == TokenType.RBRACKET)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or ']' in array",
                        currentToken.position(), tokenizer.getInput());
            }
        } while (currentToken.type() != TokenType.RBRACKET);

        expect(TokenType.RBRACKET);

        byte[] array = new byte[values.size()];
        for (int i = 0; i < values.size(); i++)
        {
            array[i] = values.get(i);
        }

        return NBTFactory.createByteArray("", array);
    }

    /**
     * Parses an int array, inferring missing suffixes and widening bytes and shorts.
     *
     * @return the parsed int array tag
     * @throws SNBTException if parsing fails or values are not integers
     */
    private ITag<int[]> parseIntArray() throws SNBTException
    {
        List<Integer> values = new ArrayList<>();
        budget.allocate(16);

        if (currentToken.type() == TokenType.RBRACKET)
        {
            advance();
            return NBTFactory.createIntArray("", new int[0]);
        }

        do
        {
            budget.length(values.size() + 1);
            budget.allocate(4);
            values.add((int) parseArrayElement('i'));

            if (currentToken.type() == TokenType.COMMA)
            {
                advance();
            } else if (currentToken.type() == TokenType.RBRACKET)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or ']' in array",
                        currentToken.position(), tokenizer.getInput());
            }
        } while (currentToken.type() != TokenType.RBRACKET);

        expect(TokenType.RBRACKET);

        int[] array = new int[values.size()];
        for (int i = 0; i < values.size(); i++)
        {
            array[i] = values.get(i);
        }

        return NBTFactory.createIntArray("", array);
    }

    /**
     * Parses a long array, inferring missing suffixes and widening smaller integers.
     *
     * @return the parsed long array tag
     * @throws SNBTException if parsing fails or values are not longs
     */
    private ITag<long[]> parseLongArray() throws SNBTException
    {
        List<Long> values = new ArrayList<>();
        budget.allocate(16);

        if (currentToken.type() == TokenType.RBRACKET)
        {
            advance();
            return NBTFactory.createLongArray("", new long[0]);
        }

        do
        {
            budget.length(values.size() + 1);
            budget.allocate(8);
            values.add(parseArrayElement('l'));

            if (currentToken.type() == TokenType.COMMA)
            {
                advance();
            } else if (currentToken.type() == TokenType.RBRACKET)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or ']' in array",
                        currentToken.position(), tokenizer.getInput());
            }
        } while (currentToken.type() != TokenType.RBRACKET);

        expect(TokenType.RBRACKET);

        long[] array = new long[values.size()];
        for (int i = 0; i < values.size(); i++)
        {
            array[i] = values.get(i);
        }

        return NBTFactory.createLongArray("", array);
    }

    /**
     * Parses a compatible integer and preserves its signed value when widening.
     *
     * @throws SNBTException if the element is invalid, incompatible, or out of range
     */
    private long parseArrayElement(char arrayType) throws SNBTException
    {
        if (currentToken.type() != TokenType.NUMBER)
        {
            throw new SNBTException("Expected integer scalar value in typed array",
                    currentToken.position(), tokenizer.getInput());
        }
        budget.tag();
        String literal = currentToken.value();
        int position = currentToken.position();
        try
        {
            SNBTNumber number = SNBTNumber.parse(literal, arrayType);
            boolean compatible = switch (number.type())
            {
                case 'b' -> true;
                case 's', 'i' -> arrayType != 'b';
                case 'l' -> arrayType == 'l';
                default -> false;
            };
            if (!compatible)
                throw new SNBTException("Incompatible integer type in typed array", position, tokenizer.getInput());
            long value = number.integerValue();
            // Narrow to the source type before widening, including unsigned source bit patterns.
            long result = switch (number.type())
            {
                case 'b' -> (byte) value;
                case 's' -> (short) value;
                case 'i' -> (int) value;
                default -> value;
            };
            advance();
            return result;
        } catch (NumberFormatException e)
        {
            throw new SNBTException("Invalid number format: " + literal, position, tokenizer.getInput(), e);
        }
    }

    /**
     * Parses a list, wrapping non-compound elements if types differ. Empty lists use compound type.
     *
     * @return the parsed list tag
     * @throws SNBTException if parsing fails or a configured limit is exceeded
     */
    private IListTag parseList() throws SNBTException
    {
        List<ITag<?>> values = new ArrayList<>();

        if (currentToken.type() == TokenType.RBRACKET)
        {
            advance();
            // Empty list defaults to compound type
            valueDepth = 1;
            return NBTFactory.createList("", NBTTags.Tag_Compound.getId());
        }

        budget.length(1);
        budget.allocate(32);
        ITag<?> firstValue = parseValue();
        values.add(firstValue);
        int compoundType = NBTTags.Tag_Compound.getId();
        int listType = firstValue.getId();
        int compoundDepth = listType == compoundType ? valueDepth : 0;
        int nonCompoundDepth = listType == compoundType ? -1 : valueDepth;

        while (currentToken.type() == TokenType.COMMA)
        {
            advance();

            if (currentToken.type() == TokenType.RBRACKET)
            {
                break; // Trailing comma
            }

            budget.length(values.size() + 1);
            budget.allocate(32);
            ITag<?> value = parseValue();
            int valueType = value.getId();

            if (valueType != listType)
            {
                listType = compoundType;
            }
            if (valueType == compoundType) compoundDepth = Math.max(compoundDepth, valueDepth);
            else nonCompoundDepth = Math.max(nonCompoundDepth, valueDepth);

            values.add(value);
        }

        expect(TokenType.RBRACKET);

        // Wrapping a list adds a container above its entire subtree. Track stored height rather
        // than syntax depth so a later mismatch cannot bypass the configured nesting limit.
        boolean wrap = listType == compoundType && nonCompoundDepth >= 0;
        int childDepth = Math.max(compoundDepth, nonCompoundDepth + (wrap ? 1 : 0));
        budget.depth(depth + childDepth);
        IListTag list = NBTFactory.createList("", listType);
        for (ITag<?> value : values)
        {
            if (wrap && value.getId() != compoundType)
            {
                budget.tag();
                budget.allocate(64);
                ICompoundTag wrapper = NBTFactory.createCompound("");
                value.setName("");
                ((Collection_Tag) wrapper).addTag(value);
                value = wrapper;
            }
            ((Collection_Tag) list).addTag(value);
        }

        valueDepth = childDepth + 1;
        return list;
    }

    /**
     * Parses a string, boolean literal, or conversion call.
     */
    private ITag<?> parseStringValue() throws SNBTException
    {
        Token token = currentToken;
        advance();
        if (!token.quoted() && currentToken.type() == TokenType.LPAREN)
            return parseConversion(token.value(), token.position());
        return stringValue(token);
    }

    /**
     * Creates a string tag, converting only unquoted boolean literals to bytes.
     */
    private ITag<?> stringValue(Token token)
    {
        if (!token.quoted() && "true".equalsIgnoreCase(token.value()))
        {
            return NBTFactory.createByte("", (byte) 1);
        } else if (!token.quoted() && "false".equalsIgnoreCase(token.value()))
        {
            return NBTFactory.createByte("", (byte) 0);
        }
        return NBTFactory.createString("", token.value());
    }

    /**
     * Parses nested conversion calls iteratively to avoid Java stack growth.
     */
    private ITag<?> parseConversion(String name, int position) throws SNBTException
    {
        List<Conversion> calls = new ArrayList<>();
        ITag<?> value;
        while (true)
        {
            if (!name.equals("bool") && !name.equals("uuid"))
                throw new SNBTException("Unknown conversion: " + name, position, tokenizer.getInput());
            budget.allocate(32);
            calls.add(new Conversion(name, position));
            expect(TokenType.LPAREN);
            budget.tag();
            if (currentToken.type() == TokenType.STRING)
            {
                Token argument = currentToken;
                advance();
                if (!argument.quoted() && currentToken.type() == TokenType.LPAREN)
                {
                    name = argument.value();
                    position = argument.position();
                    continue;
                }
                value = stringValue(argument);
            } else if (currentToken.type() == TokenType.NUMBER)
            {
                value = parseNumber();
            } else
            {
                throw new SNBTException("Expected a scalar conversion argument",
                        currentToken.position(), tokenizer.getInput());
            }
            break;
        }
        for (int index = calls.size() - 1; index >= 0; index--)
        {
            if (currentToken.type() == TokenType.COMMA) advance();
            expect(TokenType.RPAREN);
            value = applyConversion(calls.get(index), value);
        }
        return value;
    }

    /**
     * Evaluates bool() or uuid() and checks the result's allocation limits.
     */
    private ITag<?> applyConversion(Conversion call, ITag<?> argument) throws SNBTException
    {
        if (call.name().equals("bool"))
        {
            if (!(argument.getData() instanceof Number number))
                throw new SNBTException("bool() requires a number or boolean", call.position(), tokenizer.getInput());
            return NBTFactory.createByte("", (byte) (number.doubleValue() != 0 ? 1 : 0));
        }
        if (!(argument.getData() instanceof String text))
            throw new SNBTException("uuid() requires a UUID string", call.position(), tokenizer.getInput());
        budget.length(4);
        budget.allocate(16);
        try
        {
            UUID uuid = UUID.fromString(text);
            long most = uuid.getMostSignificantBits();
            long least = uuid.getLeastSignificantBits();
            return NBTFactory.createIntArray("", new int[]{(int) (most >>> 32), (int) most,
                    (int) (least >>> 32), (int) least});
        } catch (IllegalArgumentException e)
        {
            throw new SNBTException("Invalid UUID", call.position(), tokenizer.getInput(), e);
        }
    }

    /**
     * A pending conversion and its input position.
     */
    private record Conversion(String name, int position)
    {
    }

    /**
     * Parses the current numeric token with range and finite-value checks.
     *
     * @throws SNBTException if the number is invalid or out of range
     */
    private ITag<?> parseNumber() throws SNBTException
    {
        String numberStr = currentToken.value();
        int numberPosition = currentToken.position();
        advance();

        try
        {
            SNBTNumber number = SNBTNumber.parse(numberStr);
            if (number.type() == 'f' || number.type() == 'd')
            {
                String digits = numberStr.substring(0, number.digitsEnd());
                if (digits.indexOf('_') >= 0)
                {
                    budget.allocate(2L * digits.length());
                    digits = digits.replace("_", "");
                }
                if (number.type() == 'f') return NBTFactory.createFloat("", parseFiniteFloat(digits));
                return NBTFactory.createDouble("", parseFiniteDouble(digits));
            }
            long value = number.integerValue();
            return switch (number.type())
            {
                case 'b' -> NBTFactory.createByte("", (byte) value);
                case 's' -> NBTFactory.createShort("", (short) value);
                case 'i' -> NBTFactory.createInt("", (int) value);
                case 'l' -> NBTFactory.createLong("", value);
                default -> throw new NumberFormatException("Unsupported numeric type");
            };
        } catch (NumberFormatException e)
        {
            throw new SNBTException("Invalid number format: " + numberStr,
                    numberPosition, tokenizer.getInput(), e);
        }
    }

    /**
     * Parses a float and rejects overflow to infinity.
     *
     * @param value the numeric text without a type suffix
     * @return the finite float value
     * @throws NumberFormatException if the text is invalid or the result is non-finite
     */
    private float parseFiniteFloat(String value)
    {
        float result = Float.parseFloat(value);
        if (!Float.isFinite(result)) throw new NumberFormatException("Non-finite SNBT float");
        return result;
    }

    /**
     * Parses a double and rejects overflow to infinity.
     *
     * @param value the numeric text without a type suffix
     * @return the finite double value
     * @throws NumberFormatException if the text is invalid or the result is non-finite
     */
    private double parseFiniteDouble(String value)
    {
        double result = Double.parseDouble(value);
        if (!Double.isFinite(result)) throw new NumberFormatException("Non-finite SNBT double");
        return result;
    }

    /**
     * Advances to the next token in the input stream.
     *
     * @throws SNBTException if tokenization fails
     */
    private void advance() throws SNBTException
    {
        currentToken = tokenizer.nextToken();
    }

    /**
     * Advances after a compound's opening brace or separator, where a key is expected.
     */
    private void advanceKey() throws SNBTException
    {
        currentToken = tokenizer.nextKeyToken();
    }

    /**
     * Expects a specific token type and advances to the next token.
     *
     * @param expectedType the expected token type
     * @throws SNBTException if the current token does not match the expected type
     */
    private void expect(TokenType expectedType) throws SNBTException
    {
        if (currentToken.type() != expectedType)
        {
            throw new SNBTException("Expected " + expectedType + " but got " + currentToken.type(),
                    currentToken.position(), tokenizer.getInput());
        }
        advance();
    }
}
