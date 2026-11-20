package de.pauleff.jnbt.formats.snbt;

/**
 * Holds validated numeric syntax and conversion details. Digit offsets refer to the original literal.
 */
record SNBTNumber(String literal, int digitsStart, int digitsEnd, int radix, char type,
                  boolean unsigned, boolean negative)
{
    /**
     * Validates a numeric literal and identifies its type and range. Whitespace is allowed
     * between grammar components, but digit runs and their separators stay contiguous.
     *
     * @throws NumberFormatException if the syntax is invalid
     */
    static SNBTNumber parse(String literal)
    {
        return parse(literal, 'i');
    }

    /**
     * Uses the supplied integer type when no suffix is present.
     */
    static SNBTNumber parse(String literal, char defaultIntegerType)
    {
        int length = literal.length();
        int start = 0;
        boolean negative = literal.charAt(start) == '-';
        if (negative || literal.charAt(start) == '+') start = skipWhitespace(literal, start + 1);

        int radix = 10;
        int prefix = skipWhitespace(literal, start + 1);
        if (start < length && literal.charAt(start) == '0' && prefix < length)
        {
            char marker = lowerCase(literal.charAt(prefix));
            int numeral = skipWhitespace(literal, prefix + 1);
            if (marker == 'x') radix = 16;
            // Bare 0b is byte zero, including in the serializer's output.
            else if (marker == 'b' && numeral < length) radix = 2;
            if (radix != 10) start = numeral;
        }

        int end = digitRun(literal, start, radix, radix != 10);
        int wholeEnd = end;
        int digitsEnd = end;
        end = skipWhitespace(literal, end);
        boolean floating = false;
        if (radix == 10)
        {
            boolean hasDigits = wholeEnd > start;
            if (end < length && literal.charAt(end) == '.')
            {
                floating = true;
                int fractionStart = skipWhitespace(literal, end + 1);
                end = digitRun(literal, fractionStart, 10, false);
                hasDigits |= end > fractionStart;
                digitsEnd = end;
                end = skipWhitespace(literal, end);
            }
            if (!hasDigits) throw new NumberFormatException("Expected numeric digits");
            if (end < length && (literal.charAt(end) == 'e' || literal.charAt(end) == 'E'))
            {
                floating = true;
                end = skipWhitespace(literal, end + 1);
                if (end < length && (literal.charAt(end) == '+' || literal.charAt(end) == '-'))
                    end = skipWhitespace(literal, end + 1);
                end = digitRun(literal, end, 10, true);
                digitsEnd = end;
                end = skipWhitespace(literal, end);
            }
        }

        char type = floating ? 'd' : defaultIntegerType;
        boolean unsigned = radix != 10;
        if (end < length)
        {
            char suffix = lowerCase(literal.charAt(end));
            end = skipWhitespace(literal, end + 1);
            if ((suffix == 's' || suffix == 'u') && end < length)
            {
                unsigned = suffix == 'u';
                suffix = lowerCase(literal.charAt(end));
                end = skipWhitespace(literal, end + 1);
                if (!isIntegerType(suffix)) throw new NumberFormatException("Expected integer type after range prefix");
            }
            if (isIntegerType(suffix))
            {
                if (floating) throw new NumberFormatException("Integer suffix on floating literal");
                type = suffix;
            } else if (radix == 10 && (suffix == 'f' || suffix == 'd'))
            {
                floating = true;
                type = suffix;
            } else
            {
                throw new NumberFormatException("Invalid numeric suffix");
            }
        }
        if (end != length) throw new NumberFormatException("Invalid numeric continuation");
        if (!floating && radix == 10 && wholeEnd - start > 1 && literal.charAt(start) == '0')
            throw new NumberFormatException("Leading zeros in decimal integer");
        return new SNBTNumber(literal, start, digitsEnd, radix, type, unsigned, negative);
    }

    /**
     * Skips the same whitespace accepted between SNBT tokens.
     */
    private static int skipWhitespace(String literal, int position)
    {
        while (position < literal.length() && Character.isWhitespace(literal.charAt(position))) position++;
        return position;
    }

    /**
     * Scans ASCII digits, allowing separators only between digits.
     *
     * @return the first character after the run
     * @throws NumberFormatException if required digits are missing or separators are misplaced
     */
    private static int digitRun(String literal, int start, int radix, boolean required)
    {
        int end = start;
        while (end < literal.length() && (digit(literal.charAt(end), radix) >= 0 || literal.charAt(end) == '_')) end++;
        if (end == start)
        {
            if (required) throw new NumberFormatException("Expected numeric digits");
        } else if (literal.charAt(start) == '_' || literal.charAt(end - 1) == '_')
        {
            throw new NumberFormatException("Separator must occur between digits");
        }
        return end;
    }

    /**
     * Returns an ASCII digit's value in the given radix, or -1 if invalid.
     */
    private static int digit(char c, int radix)
    {
        int value;
        if (c >= '0' && c <= '9') value = c - '0';
        else if (c >= 'a' && c <= 'f') value = c - 'a' + 10;
        else if (c >= 'A' && c <= 'F') value = c - 'A' + 10;
        else return -1;
        return value < radix ? value : -1;
    }

    /**
     * Checks whether a lowercase suffix denotes an integer type.
     */
    private static boolean isIntegerType(char type)
    {
        return type == 'b' || type == 's' || type == 'i' || type == 'l';
    }

    /**
     * Lowercases ASCII letters, leaving other characters unchanged.
     */
    private static char lowerCase(char c)
    {
        return c >= 'A' && c <= 'Z' ? (char) (c + ('a' - 'A')) : c;
    }

    /**
     * Converts the integer with range checks. Narrow to the selected NBT type to retain unsigned bits.
     *
     * @throws NumberFormatException if the sign or magnitude is outside the selected range
     */
    long integerValue()
    {
        if (unsigned && negative) throw new NumberFormatException("Unsigned integer cannot have a minus sign");
        int bits = switch (type)
        {
            case 'b' -> 8;
            case 's' -> 16;
            case 'i' -> 32;
            case 'l' -> 64;
            default -> throw new NumberFormatException("Expected integer type");
        };
        long limit = unsigned ? (bits == 64 ? -1L : (1L << bits) - 1)
                : negative ? 1L << (bits - 1) : (1L << (bits - 1)) - 1;
        long quotient = Long.divideUnsigned(limit, radix);
        int remainder = (int) Long.remainderUnsigned(limit, radix);
        long value = 0;
        for (int index = digitsStart; index < digitsEnd; index++)
        {
            char c = literal.charAt(index);
            if (c == '_') continue;
            int digit = digit(c, radix);
            int comparison = Long.compareUnsigned(value, quotient);
            if (comparison > 0 || (comparison == 0 && digit > remainder))
                throw new NumberFormatException("Integer outside " + (unsigned ? "unsigned " : "signed ") + bits + "-bit range");
            value = value * radix + digit;
        }
        return negative ? -value : value;
    }
}
