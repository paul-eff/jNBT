package de.pauleff.formats.snbt;

/**
 * Exception thrown when parsing or generating SNBT (Stringified NBT) data fails.
 * Provides position information for better error reporting.
 */
public class SNBTException extends Exception
{
    private final int position;
    private final String input;

    /**
     * Creates an SNBT exception with a message.
     *
     * @param message the error message
     */
    public SNBTException(String message)
    {
        super(message);
        this.position = -1;
        this.input = null;
    }

    /**
     * Creates an SNBT exception with position information.
     *
     * @param message  the error message
     * @param position the character position where the error occurred
     * @param input    the input string being parsed
     */
    public SNBTException(String message, int position, String input)
    {
        super(formatMessage(message, position, input));
        this.position = position;
        this.input = input;
    }

    /**
     * Creates an SNBT exception with a message and underlying cause.
     *
     * @param message the error message
     * @param cause   the underlying exception that caused this error
     */
    public SNBTException(String message, Throwable cause)
    {
        super(message, cause);
        this.position = -1;
        this.input = null;
    }

    /**
     * Creates an SNBT exception with position information and underlying cause.
     *
     * @param message  the error message
     * @param position the character position where the error occurred
     * @param input    the input string being parsed
     * @param cause    the underlying exception that caused this error
     */
    public SNBTException(String message, int position, String input, Throwable cause)
    {
        super(formatMessage(message, position, input), cause);
        this.position = position;
        this.input = input;
    }

    /**
     * Formats the error message with position information.
     *
     * @param message  the base error message
     * @param position the error position
     * @param input    the input string
     * @return the formatted error message
     */
    private static String formatMessage(String message, int position, String input)
    {
        if (position < 0 || input == null)
        {
            return message;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(" at position ").append(position);

        if (position < input.length())
        {
            sb.append(" (near '").append(input.charAt(position)).append("')");
        }

        return sb.toString();
    }

    /**
     * Returns the character position where the error occurred.
     *
     * @return the error position, or -1 if not available
     */
    public int getPosition()
    {
        return position;
    }

    /**
     * Returns the input string that was being parsed when the error occurred.
     *
     * @return the input string, or null if not available
     */
    public String getInput()
    {
        return input;
    }
}