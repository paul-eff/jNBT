package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.*;
import de.pauleff.jnbt.core.Collection_Tag;
import de.pauleff.jnbt.formats.snbt.SNBTTokenizer.Token;
import de.pauleff.jnbt.formats.snbt.SNBTTokenizer.TokenType;
import de.pauleff.jnbt.internal.NBTBudget;
import de.pauleff.jnbt.util.NBTTags;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses SNBT strings into NBT tags. Uses {@link NBTLimits#DEFAULT}
 * unless limits are supplied to the constructor. Mixed lists become homogeneous compound lists:
 * each non-compound element is stored in a compound under an empty key.
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
        expect(TokenType.LBRACE);
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
                advance();
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
     * Parses a byte array in the format [B; 1b, 2b, 3b].
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
            ITag<?> value = parseArrayElement();
            if (!(value.getData() instanceof Byte))
            {
                throw new SNBTException("Expected byte value in byte array",
                        currentToken.position(), tokenizer.getInput());
            }
            values.add((Byte) value.getData());

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
     * Parses an int array in the format [I; 1, 2, 3].
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
            ITag<?> value = parseArrayElement();
            if (!(value.getData() instanceof Integer))
            {
                throw new SNBTException("Expected int value in int array",
                        currentToken.position(), tokenizer.getInput());
            }
            values.add((Integer) value.getData());

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
     * Parses a long array in the format [L; 1l, 2l, 3l].
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
            ITag<?> value = parseArrayElement();
            if (!(value.getData() instanceof Long))
            {
                throw new SNBTException("Expected long value in long array",
                        currentToken.position(), tokenizer.getInput());
            }
            values.add((Long) value.getData());

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
     * Rejects nested containers in typed arrays before entering another recursive parse.
     *
     * @return the parsed scalar array element
     * @throws SNBTException            if the next value is a container or is not a valid scalar
     * @throws IllegalArgumentException if a configured limit is exceeded
     */
    private ITag<?> parseArrayElement() throws SNBTException
    {
        if (currentToken.type() != TokenType.NUMBER && currentToken.type() != TokenType.STRING)
        {
            throw new SNBTException("Expected scalar value in typed array",
                    currentToken.position(), tokenizer.getInput());
        }
        return parseValue();
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
     * Parses a string. Only unquoted "true" and "false" become bytes 1 and 0.
     *
     * @return the parsed string or byte tag
     * @throws SNBTException if parsing fails
     */
    private ITag<?> parseStringValue() throws SNBTException
    {
        String value = currentToken.value();
        boolean quoted = currentToken.quoted();
        advance();

        if (!quoted && "true".equals(value))
        {
            return NBTFactory.createByte("", (byte) 1);
        } else if (!quoted && "false".equals(value))
        {
            return NBTFactory.createByte("", (byte) 0);
        }

        return NBTFactory.createString("", value);
    }

    /**
     * Parses a finite number with an optional b, s, l, f, or d suffix. Without a suffix,
     * decimals and exponent forms use double. Whole numbers use int.
     *
     * @return the parsed numeric tag
     * @throws SNBTException if parsing fails or number format is invalid
     */
    private ITag<?> parseNumber() throws SNBTException
    {
        String numberStr = currentToken.value();
        int numberPosition = currentToken.position();
        advance();

        try
        {
            char lastChar = numberStr.charAt(numberStr.length() - 1);

            if (Character.isLetter(lastChar))
            {
                String numPart = numberStr.substring(0, numberStr.length() - 1);
                char suffix = Character.toLowerCase(lastChar);

                return switch (suffix)
                {
                    case 'b' -> NBTFactory.createByte("", Byte.parseByte(numPart));
                    case 's' -> NBTFactory.createShort("", Short.parseShort(numPart));
                    case 'l' -> NBTFactory.createLong("", Long.parseLong(numPart));
                    case 'f' -> NBTFactory.createFloat("", parseFiniteFloat(numPart));
                    case 'd' -> NBTFactory.createDouble("", parseFiniteDouble(numPart));
                    default -> throw new SNBTException("Unknown numeric suffix: " + suffix,
                            numberPosition, tokenizer.getInput());
                };
            } else
            {
                if (numberStr.contains(".") || numberStr.contains("e") || numberStr.contains("E"))
                {
                    return NBTFactory.createDouble("", parseFiniteDouble(numberStr));
                } else
                {
                    return NBTFactory.createInt("", Integer.parseInt(numberStr));
                }
            }
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
