package de.pauleff.jnbt.formats.snbt;

/**
 * Splits SNBT input into tokens. Quoted strings support the modern SNBT escapes.
 * Unknown or malformed escapes are rejected.
 *
 * @since 1.6.0
 */
public class SNBTTokenizer
{
    private final String input;
    private final int length;
    private int position;

    /**
     * Creates a tokenizer for SNBT input.
     *
     * @param input the SNBT string to tokenize, null is treated as empty string
     */
    public SNBTTokenizer(String input)
    {
        this.input = input != null ? input : "";
        this.position = 0;
        this.length = this.input.length();
    }

    /**
     * Returns the next token from the input stream.
     *
     * @return the next token
     * @throws SNBTException if tokenization fails due to invalid syntax
     */
    public Token nextToken() throws SNBTException
    {
        while (position < length && Character.isWhitespace(input.charAt(position)))
        {
            position++;
        }

        if (position >= length)
        {
            return new Token(TokenType.EOF, "", position);
        }

        char current = input.charAt(position);
        int tokenStart = position;

        switch (current)
        {
            case '{':
                position++;
                return new Token(TokenType.LBRACE, "{", tokenStart);
            case '}':
                position++;
                return new Token(TokenType.RBRACE, "}", tokenStart);
            case '[':
                position++;
                return new Token(TokenType.LBRACKET, "[", tokenStart);
            case ']':
                position++;
                return new Token(TokenType.RBRACKET, "]", tokenStart);
            case ',':
                position++;
                return new Token(TokenType.COMMA, ",", tokenStart);
            case ':':
                position++;
                return new Token(TokenType.COLON, ":", tokenStart);
            case ';':
                position++;
                return new Token(TokenType.SEMICOLON, ";", tokenStart);
            case '"':
                return parseQuotedString('"', tokenStart);
            case '\'':
                return parseQuotedString('\'', tokenStart);
            default:
                if ((current >= '0' && current <= '9') || current == '-' || current == '+' || current == '.')
                {
                    return parseNumber(tokenStart);
                } else if (Character.isLetter(current) || current == '_')
                {
                    return parseUnquotedString(tokenStart);
                } else
                {
                    position++;
                    return new Token(TokenType.UNKNOWN, String.valueOf(current), tokenStart);
                }
        }
    }

    /**
     * Parses a quoted string with escape sequence support.
     *
     * @param quote      the quote character (" or ')
     * @param tokenStart the starting position of the token
     * @return the parsed string token
     * @throws SNBTException if the string is unterminated or has invalid escape sequences
     */
    private Token parseQuotedString(char quote, int tokenStart) throws SNBTException
    {
        StringBuilder sb = new StringBuilder();
        position++;

        while (position < length)
        {
            char c = input.charAt(position++);

            if (c == quote)
            {
                return new Token(TokenType.STRING, sb.toString(), tokenStart, true);
            } else if (c != '\\')
            {
                sb.append(c);
                continue;
            }

            int escapeStart = position - 1;
            if (position >= length)
            {
                throw new SNBTException("Unterminated escape sequence", escapeStart, input);
            }

            char escaped = input.charAt(position++);
            switch (escaped)
            {
                case '"', '\'', '\\' -> sb.append(escaped);
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 's' -> sb.append(' ');
                case 'x' -> sb.appendCodePoint(parseHexEscape(2, escapeStart));
                case 'u' -> sb.appendCodePoint(parseHexEscape(4, escapeStart));
                case 'U' -> sb.appendCodePoint(parseHexEscape(8, escapeStart));
                case 'N' -> sb.appendCodePoint(parseNamedEscape(quote, escapeStart));
                default -> throw new SNBTException("Unknown escape sequence: \\" + escaped, escapeStart, input);
            }
        }

        throw new SNBTException("Unterminated string", tokenStart, input);
    }

    /**
     * Reads exactly the required number of ASCII hexadecimal digits after an escape prefix.
     * Four-digit escapes preserve UTF-16 code units, including surrogates.
     *
     * @param digits      the number of digits to consume (2, 4, or 8)
     * @param escapeStart the position of the escape's backslash
     * @return the decoded code point, in the range 0 through 0x10FFFF
     * @throws SNBTException if digits are missing or invalid, or the value is out of range
     */
    private int parseHexEscape(int digits, int escapeStart) throws SNBTException
    {
        if (length - position < digits)
        {
            throw new SNBTException("Incomplete hexadecimal escape", escapeStart, input);
        }
        long value = 0;
        for (int i = 0; i < digits; i++)
        {
            char c = input.charAt(position++);
            int digit;
            if (c >= '0' && c <= '9') digit = c - '0';
            else if (c >= 'a' && c <= 'f') digit = c - 'a' + 10;
            else if (c >= 'A' && c <= 'F') digit = c - 'A' + 10;
            else throw new SNBTException("Invalid hexadecimal digit in escape", escapeStart, input);
            value = (value << 4) | digit;
        }
        if (value > Character.MAX_CODE_POINT)
        {
            throw new SNBTException("Unicode escape code point out of range", escapeStart, input);
        }
        return (int) value;
    }

    /**
     * Reads a braced Unicode character name after the named-escape prefix.
     *
     * @param quote       the enclosing string's quote character
     * @param escapeStart the position of the escape's backslash
     * @return the code point from the runtime's Unicode name database
     * @throws SNBTException if braces are missing or the character name is invalid
     */
    private int parseNamedEscape(char quote, int escapeStart) throws SNBTException
    {
        if (position >= length || input.charAt(position) != '{')
        {
            throw new SNBTException("Expected '{' after named Unicode escape", escapeStart, input);
        }
        int nameStart = ++position;
        while (position < length && input.charAt(position) != '}')
        {
            if (input.charAt(position) == quote || input.charAt(position) == '\\')
            {
                throw new SNBTException("Invalid named Unicode escape", escapeStart, input);
            }
            position++;
        }
        if (position >= length)
        {
            throw new SNBTException("Unterminated named Unicode escape", escapeStart, input);
        }
        String name = input.substring(nameStart, position++);
        try
        {
            return Character.codePointOf(name);
        } catch (IllegalArgumentException e)
        {
            throw new SNBTException("Unknown Unicode character name: " + name, escapeStart, input, e);
        }
    }

    /**
     * Parses a decimal number with an optional exponent and type suffix.
     *
     * @param tokenStart the starting position of the token
     * @return the parsed number token
     * @throws SNBTException if the number format is invalid
     */
    private Token parseNumber(int tokenStart) throws SNBTException
    {
        StringBuilder sb = new StringBuilder();

        if (position < length && (input.charAt(position) == '-' || input.charAt(position) == '+'))
        {
            sb.append(input.charAt(position));
            position++;
        }

        boolean hasDecimal = false;
        boolean hasDigits = false;

        while (position < length)
        {
            char c = input.charAt(position);

            if (c >= '0' && c <= '9')
            {
                hasDigits = true;
                sb.append(c);
                position++;
            } else if (c == '.' && !hasDecimal)
            {
                sb.append(c);
                hasDecimal = true;
                position++;
            } else
            {
                break;
            }
        }

        if (!hasDigits)
        {
            throw new SNBTException("Invalid number format", tokenStart, input);
        }

        if (position < length && (input.charAt(position) == 'e' || input.charAt(position) == 'E'))
        {
            sb.append(input.charAt(position++));
            if (position < length && (input.charAt(position) == '-' || input.charAt(position) == '+'))
            {
                sb.append(input.charAt(position++));
            }
            int exponentStart = position;
            while (position < length && input.charAt(position) >= '0' && input.charAt(position) <= '9')
            {
                sb.append(input.charAt(position++));
            }
            if (position == exponentStart)
            {
                throw new SNBTException("Expected digits in numeric exponent", tokenStart, input);
            }
        }

        if (position < length)
        {
            char suffix = input.charAt(position);
            if (isTypeSuffix(suffix))
            {
                sb.append(suffix);
                position++;
            }
        }

        return new Token(TokenType.NUMBER, sb.toString(), tokenStart);
    }

    /**
     * Parses an unquoted string and detects array prefixes.
     *
     * @param tokenStart the starting position of the token
     * @return the parsed string or array prefix token
     */
    private Token parseUnquotedString(int tokenStart)
    {
        StringBuilder sb = new StringBuilder();

        while (position < length)
        {
            char c = input.charAt(position);

            if (Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-')
            {
                sb.append(c);
                position++;
            } else
            {
                break;
            }
        }

        String value = sb.toString();

        if (value.length() == 1 && (value.equals("B") || value.equals("I") || value.equals("L")))
        {
            int nextPos = position;
            while (nextPos < length && Character.isWhitespace(input.charAt(nextPos)))
            {
                nextPos++;
            }
            if (nextPos < length && input.charAt(nextPos) == ';')
            {
                return new Token(TokenType.ARRAY_PREFIX, value, tokenStart);
            }
        }

        return new Token(TokenType.STRING, value, tokenStart);
    }

    /**
     * Checks if a character is a valid numeric type suffix.
     *
     * @param c the character to check
     * @return true if the character is a valid type suffix (b, s, l, f, d)
     */
    private boolean isTypeSuffix(char c)
    {
        return c == 'b' || c == 'B' ||
                c == 's' || c == 'S' ||
                c == 'l' || c == 'L' ||
                c == 'f' || c == 'F' ||
                c == 'd' || c == 'D';
    }

    /**
     * Returns the original input string.
     *
     * @return the input string being tokenized
     */
    public String getInput()
    {
        return input;
    }

    /**
     * SNBT token types.
     *
     * @since 1.6.0
     */
    public enum TokenType
    {
        LBRACE,
        RBRACE,
        LBRACKET,
        RBRACKET,
        COMMA,
        COLON,
        SEMICOLON,
        STRING,
        NUMBER,
        ARRAY_PREFIX,
        EOF,
        UNKNOWN
    }

    /**
         * An SNBT token with its input position and quote information.
         *
         * @param type     the token type
         * @param value    the decoded token value
         * @param position the starting position in the input
         * @param quoted   whether the token was read from a quoted string
         *
         * @since 1.6.0
         */
        public record Token(TokenType type, String value, int position, boolean quoted)
        {
            /**
             * Creates a token.
             *
             * @param type     the token type
             * @param value    the token value
             * @param position the position in the input where this token starts
             * @param quoted   whether the token was read from a quoted string
             */
            public Token
            {
            }

            /**
             * Creates an unquoted token.
             *
             * @param type     the token type
             * @param value    the token value
             * @param position the starting position in the input
             */
            public Token(TokenType type, String value, int position)
            {
                this(type, value, position, false);
            }

            /**
             * Returns the type of this token.
             *
             * @return the token type
             */
            @Override
            public TokenType type()
            {
                return type;
            }

            /**
             * Returns the string value of this token.
             *
             * @return the token value
             */
            @Override
            public String value()
            {
                return value;
            }

            /**
             * Returns the position where this token starts in the input.
             *
             * @return the starting position
             */
            @Override
            public int position()
            {
                return position;
            }

            /**
             * Returns a string with the stored values.
             */
            @Override
            public String toString()
            {
                return String.format("Token{%s, '%s', pos=%d, quoted=%s}", type, value, position, quoted);
            }
        }
}
