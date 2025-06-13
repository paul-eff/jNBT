package de.pauleff.formats.snbt;

/**
 * Tokenizer for SNBT (Stringified NBT) format.
 * Breaks down SNBT strings into tokens for parsing.
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
                if (Character.isDigit(current) || current == '-' || current == '+')
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
        position++; // Skip opening quote

        while (position < length)
        {
            char c = input.charAt(position);

            if (c == quote)
            {
                position++; // Skip closing quote
                return new Token(TokenType.STRING, sb.toString(), tokenStart);
            } else if (c == '\\')
            {
                position++;
                if (position >= length)
                {
                    throw new SNBTException("Unterminated escape sequence", position, input);
                }

                char escaped = input.charAt(position);
                switch (escaped)
                {
                    case '"':
                        sb.append('"');
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'u':
                        // Unicode escape sequence \\uXXXX
                        if (position + 4 >= length)
                        {
                            throw new SNBTException("Incomplete unicode escape sequence", position, input);
                        }
                        try
                        {
                            String hex = input.substring(position + 1, position + 5);
                            int codePoint = Integer.parseInt(hex, 16);
                            sb.append((char) codePoint);
                            position += 4; // Skip the 4 hex digits
                        } catch (NumberFormatException e)
                        {
                            throw new SNBTException("Invalid unicode escape sequence", position, input, e);
                        }
                        break;
                    default:
                        // Unknown escape - include literally
                        sb.append(escaped);
                        break;
                }
                position++;
            } else
            {
                sb.append(c);
                position++;
            }
        }

        throw new SNBTException("Unterminated string", tokenStart, input);
    }

    /**
     * Parses a numeric value with optional type suffix.
     *
     * @param tokenStart the starting position of the token
     * @return the parsed number token
     * @throws SNBTException if the number format is invalid
     */
    private Token parseNumber(int tokenStart) throws SNBTException
    {
        StringBuilder sb = new StringBuilder();

        // Handle sign
        if (position < length && (input.charAt(position) == '-' || input.charAt(position) == '+'))
        {
            sb.append(input.charAt(position));
            position++;
        }

        boolean hasDecimal = false;

        // Parse digits and decimal point
        while (position < length)
        {
            char c = input.charAt(position);

            if (Character.isDigit(c))
            {
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

        // Check for type suffix
        if (position < length)
        {
            char suffix = input.charAt(position);
            if (isTypeSuffix(suffix))
            {
                sb.append(suffix);
                position++;
            }
        }

        String numberStr = sb.toString();
        if (numberStr.isEmpty() || numberStr.equals("-") || numberStr.equals("+"))
        {
            throw new SNBTException("Invalid number format", tokenStart, input);
        }

        return new Token(TokenType.NUMBER, numberStr, tokenStart);
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

        // Check if this is an array prefix (single letter followed by potential semicolon)
        if (value.length() == 1 && (value.equals("B") || value.equals("I") || value.equals("L")))
        {
            // Look ahead to see if there's a semicolon
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
     * Enumeration of all token types recognized by the SNBT tokenizer.
     */
    public enum TokenType
    {
        /**
         * Left brace {
         */
        LBRACE,
        /**
         * Right brace }
         */
        RBRACE,
        /**
         * Left bracket [
         */
        LBRACKET,
        /**
         * Right bracket ]
         */
        RBRACKET,
        /**
         * Comma ,
         */
        COMMA,
        /**
         * Colon :
         */
        COLON,
        /**
         * Semicolon ;
         */
        SEMICOLON,
        /**
         * String value (quoted or unquoted)
         */
        STRING,
        /**
         * Numeric value with optional type suffix
         */
        NUMBER,
        /**
         * Array type prefix (B, I, L)
         */
        ARRAY_PREFIX,
        /**
         * End of input
         */
        EOF,
        /**
         * Invalid or unrecognized character
         */
        UNKNOWN
    }

    /**
     * Represents a single token in the SNBT input.
     */
    public static class Token
    {
        private final TokenType type;
        private final String value;
        private final int position;

        /**
         * Creates a new token.
         *
         * @param type     the token type
         * @param value    the token value
         * @param position the position in the input where this token starts
         */
        public Token(TokenType type, String value, int position)
        {
            this.type = type;
            this.value = value;
            this.position = position;
        }

        /**
         * Returns the type of this token.
         *
         * @return the token type
         */
        public TokenType getType()
        {
            return type;
        }

        /**
         * Returns the string value of this token.
         *
         * @return the token value
         */
        public String getValue()
        {
            return value;
        }

        /**
         * Returns the position where this token starts in the input.
         *
         * @return the starting position
         */
        public int getPosition()
        {
            return position;
        }

        @Override
        public String toString()
        {
            return String.format("Token{%s, '%s', pos=%d}", type, value, position);
        }
    }
}