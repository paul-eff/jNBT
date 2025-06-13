package de.pauleff.formats.snbt;

import de.pauleff.core.Collection_Tag;
import de.pauleff.api.ICompoundTag;
import de.pauleff.api.IListTag;
import de.pauleff.api.ITag;
import de.pauleff.api.NBTFactory;
import de.pauleff.formats.snbt.SNBTTokenizer.Token;
import de.pauleff.formats.snbt.SNBTTokenizer.TokenType;
import de.pauleff.util.NBTTags;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for SNBT (Stringified NBT) format.
 * Converts SNBT strings into NBT tag structures using recursive descent parsing.
 * <p>
 * Supports all SNBT syntax including:
 * - Basic types with suffixes (42b, 1.5f, etc.)
 * - Strings (quoted and unquoted)
 * - Compounds {key: value}
 * - Lists [value1, value2]
 * - Arrays [B; 1b, 2b] [I; 1, 2] [L; 1l, 2l]
 */
public class SNBTParser
{

    private final SNBTTokenizer tokenizer;
    private Token currentToken;

    /**
     * Creates a parser for the given SNBT input string.
     *
     * @param input the SNBT string to parse
     */
    public SNBTParser(String input)
    {
        this.tokenizer = new SNBTTokenizer(input);
    }

    /**
     * Parses the complete SNBT string into an NBT tag.
     * The root element can be a compound, list, or primitive value.
     *
     * @return the parsed NBT tag
     * @throws SNBTException if parsing fails due to syntax errors
     */
    public ITag<?> parse() throws SNBTException
    {
        advance(); // Get first token
        ITag<?> result = parseValue();

        if (currentToken.getType() != TokenType.EOF)
        {
            throw new SNBTException("Unexpected token after parsing completed",
                    currentToken.getPosition(), tokenizer.getInput());
        }

        return result;
    }

    /**
     * Parses any SNBT value (compound, list, array, or primitive).
     *
     * @return the parsed NBT tag
     * @throws SNBTException if parsing fails
     */
    private ITag<?> parseValue() throws SNBTException
    {
        switch (currentToken.getType())
        {
            case LBRACE:
                return parseCompound();
            case LBRACKET:
                return parseListOrArray();
            case STRING:
                return parseStringValue();
            case NUMBER:
                return parseNumber();
            default:
                throw new SNBTException("Unexpected token: " + currentToken.getType(),
                        currentToken.getPosition(), tokenizer.getInput());
        }
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

        // Handle empty compound
        if (currentToken.getType() == TokenType.RBRACE)
        {
            advance();
            return compound;
        }

        do
        {
            // Parse key
            if (currentToken.getType() != TokenType.STRING)
            {
                throw new SNBTException("Expected string key in compound",
                        currentToken.getPosition(), tokenizer.getInput());
            }
            String key = currentToken.getValue();
            advance();

            // Expect colon
            expect(TokenType.COLON);

            // Parse value
            ITag<?> value = parseValue();
            value.setName(key);
            ((Collection_Tag) compound).addTag(value);

            // Check for comma or end
            if (currentToken.getType() == TokenType.COMMA)
            {
                advance();
            } else if (currentToken.getType() == TokenType.RBRACE)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or '}' in compound",
                        currentToken.getPosition(), tokenizer.getInput());
            }
        } while (currentToken.getType() != TokenType.RBRACE);

        expect(TokenType.RBRACE);
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

        // Check for array prefix (B;, I;, L;)
        if (currentToken.getType() == TokenType.ARRAY_PREFIX)
        {
            return parseArray();
        } else
        {
            return parseList();
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
        String prefix = currentToken.getValue();
        advance();
        expect(TokenType.SEMICOLON);

        switch (prefix)
        {
            case "B":
                return parseByteArray();
            case "I":
                return parseIntArray();
            case "L":
                return parseLongArray();
            default:
                throw new SNBTException("Unknown array prefix: " + prefix,
                        currentToken.getPosition(), tokenizer.getInput());
        }
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

        // Handle empty array
        if (currentToken.getType() == TokenType.RBRACKET)
        {
            advance();
            return NBTFactory.createByteArray("", new byte[0]);
        }

        do
        {
            ITag<?> value = parseValue();
            if (!(value.getData() instanceof Byte))
            {
                throw new SNBTException("Expected byte value in byte array",
                        currentToken.getPosition(), tokenizer.getInput());
            }
            values.add((Byte) value.getData());

            if (currentToken.getType() == TokenType.COMMA)
            {
                advance();
            } else if (currentToken.getType() == TokenType.RBRACKET)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or ']' in array",
                        currentToken.getPosition(), tokenizer.getInput());
            }
        } while (currentToken.getType() != TokenType.RBRACKET);

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

        // Handle empty array
        if (currentToken.getType() == TokenType.RBRACKET)
        {
            advance();
            return NBTFactory.createIntArray("", new int[0]);
        }

        do
        {
            ITag<?> value = parseValue();
            if (!(value.getData() instanceof Integer))
            {
                throw new SNBTException("Expected int value in int array",
                        currentToken.getPosition(), tokenizer.getInput());
            }
            values.add((Integer) value.getData());

            if (currentToken.getType() == TokenType.COMMA)
            {
                advance();
            } else if (currentToken.getType() == TokenType.RBRACKET)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or ']' in array",
                        currentToken.getPosition(), tokenizer.getInput());
            }
        } while (currentToken.getType() != TokenType.RBRACKET);

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

        // Handle empty array
        if (currentToken.getType() == TokenType.RBRACKET)
        {
            advance();
            return NBTFactory.createLongArray("", new long[0]);
        }

        do
        {
            ITag<?> value = parseValue();
            if (!(value.getData() instanceof Long))
            {
                throw new SNBTException("Expected long value in long array",
                        currentToken.getPosition(), tokenizer.getInput());
            }
            values.add((Long) value.getData());

            if (currentToken.getType() == TokenType.COMMA)
            {
                advance();
            } else if (currentToken.getType() == TokenType.RBRACKET)
            {
                break;
            } else
            {
                throw new SNBTException("Expected ',' or ']' in array",
                        currentToken.getPosition(), tokenizer.getInput());
            }
        } while (currentToken.getType() != TokenType.RBRACKET);

        expect(TokenType.RBRACKET);

        long[] array = new long[values.size()];
        for (int i = 0; i < values.size(); i++)
        {
            array[i] = values.get(i);
        }

        return NBTFactory.createLongArray("", array);
    }

    /**
     * Parses a list in the format [value1, value2, value3].
     * All values must be of the same type.
     *
     * @return the parsed list tag
     * @throws SNBTException if parsing fails or mixed types are found
     */
    private IListTag parseList() throws SNBTException
    {
        List<ITag<?>> values = new ArrayList<>();

        // Handle empty list
        if (currentToken.getType() == TokenType.RBRACKET)
        {
            advance();
            // Empty list defaults to compound type
            return NBTFactory.createList("", NBTTags.Tag_Compound.getId());
        }

        // Parse first element to determine list type
        ITag<?> firstValue = parseValue();
        values.add(firstValue);
        int listType = getTagTypeId(firstValue);

        // Parse remaining elements
        while (currentToken.getType() == TokenType.COMMA)
        {
            advance();

            if (currentToken.getType() == TokenType.RBRACKET)
            {
                break; // Trailing comma
            }

            ITag<?> value = parseValue();
            int valueType = getTagTypeId(value);

            if (valueType != listType)
            {
                throw new SNBTException("Mixed types in list not allowed. Expected type " +
                        listType + " but got " + valueType,
                        currentToken.getPosition(), tokenizer.getInput());
            }

            values.add(value);
        }

        expect(TokenType.RBRACKET);

        IListTag list = NBTFactory.createList("", listType);
        for (ITag<?> value : values)
        {
            ((Collection_Tag) list).addTag(value);
        }

        return list;
    }

    /**
     * Parses a string value and handles special cases like boolean keywords.
     * Converts "true" and "false" to byte values following Minecraft convention.
     *
     * @return the parsed string or byte tag
     * @throws SNBTException if parsing fails
     */
    private ITag<?> parseStringValue() throws SNBTException
    {
        String value = currentToken.getValue();
        advance();

        // Handle boolean values as bytes (Minecraft convention)
        if ("true".equals(value))
        {
            return NBTFactory.createByte("", (byte) 1);
        } else if ("false".equals(value))
        {
            return NBTFactory.createByte("", (byte) 0);
        }

        return NBTFactory.createString("", value);
    }

    /**
     * Parses a numeric value with optional type suffix.
     * Supports suffixes: b (byte), s (short), l (long), f (float), d (double).
     *
     * @return the parsed numeric tag
     * @throws SNBTException if parsing fails or number format is invalid
     */
    private ITag<?> parseNumber() throws SNBTException
    {
        String numberStr = currentToken.getValue();
        advance();

        try
        {
            // Check for type suffix
            char lastChar = numberStr.charAt(numberStr.length() - 1);

            if (Character.isLetter(lastChar))
            {
                String numPart = numberStr.substring(0, numberStr.length() - 1);
                char suffix = Character.toLowerCase(lastChar);

                switch (suffix)
                {
                    case 'b':
                        return NBTFactory.createByte("", Byte.parseByte(numPart));
                    case 's':
                        return NBTFactory.createShort("", Short.parseShort(numPart));
                    case 'l':
                        return NBTFactory.createLong("", Long.parseLong(numPart));
                    case 'f':
                        return NBTFactory.createFloat("", Float.parseFloat(numPart));
                    case 'd':
                        return NBTFactory.createDouble("", Double.parseDouble(numPart));
                    default:
                        throw new SNBTException("Unknown numeric suffix: " + suffix,
                                currentToken.getPosition(), tokenizer.getInput());
                }
            } else
            {
                // No suffix - determine type by content
                if (numberStr.contains("."))
                {
                    // Floating point - default to double
                    return NBTFactory.createDouble("", Double.parseDouble(numberStr));
                } else
                {
                    // Integer - default to int
                    return NBTFactory.createInt("", Integer.parseInt(numberStr));
                }
            }
        } catch (NumberFormatException e)
        {
            throw new SNBTException("Invalid number format: " + numberStr,
                    currentToken.getPosition(), tokenizer.getInput(), e);
        }
    }

    /**
     * Returns the NBT type ID for a tag based on its data type.
     *
     * @param tag the tag to get the type ID for
     * @return the NBT type ID as defined in {@link NBTTags}
     */
    private int getTagTypeId(ITag<?> tag)
    {
        Object value = tag.getData();

        if (value instanceof Byte) return NBTTags.Tag_Byte.getId();
        if (value instanceof Short) return NBTTags.Tag_Short.getId();
        if (value instanceof Integer) return NBTTags.Tag_Int.getId();
        if (value instanceof Long) return NBTTags.Tag_Long.getId();
        if (value instanceof Float) return NBTTags.Tag_Float.getId();
        if (value instanceof Double) return NBTTags.Tag_Double.getId();
        if (value instanceof String) return NBTTags.Tag_String.getId();
        if (value instanceof byte[]) return NBTTags.Tag_Byte_Array.getId();
        if (value instanceof int[]) return NBTTags.Tag_Int_Array.getId();
        if (value instanceof long[]) return NBTTags.Tag_Long_Array.getId();
        if (tag instanceof IListTag) return NBTTags.Tag_List.getId();
        if (tag instanceof ICompoundTag) return NBTTags.Tag_Compound.getId();

        return NBTTags.Tag_End.getId(); // Unknown type
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
        if (currentToken.getType() != expectedType)
        {
            throw new SNBTException("Expected " + expectedType + " but got " + currentToken.getType(),
                    currentToken.getPosition(), tokenizer.getInput());
        }
        advance();
    }
}