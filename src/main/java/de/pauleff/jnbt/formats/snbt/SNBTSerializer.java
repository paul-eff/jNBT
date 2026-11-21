package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.core.Tag;
import de.pauleff.jnbt.internal.NBTBudget;
import de.pauleff.jnbt.internal.NBTModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Converts NBT tags to SNBT strings. Uses {@link NBTLimits#DEFAULT}
 * unless limits are supplied to the constructor. NaN, infinities, and invalid
 * tag payloads are rejected with {@link IllegalArgumentException}.
 * Compound wrappers used for mixed-list storage are emitted as ordinary compounds with empty keys.
 * Tags must use the core hierarchy described by {@link ITag}; independent implementations are rejected.
 *
 * @since 1.6.0
 */
public class SNBTSerializer
{
    private static final boolean SPACES_AROUND_COLON = true;
    private static final boolean SPACES_IN_COMPOUNDS = true;
    private static final boolean SPACES_IN_LISTS = true;
    private static final boolean SPACES_IN_ARRAYS = false;
    private static final boolean EXPLICIT_DOUBLE_TYPE = false;
    private static final boolean ADD_TRAILING_NEWLINE = true;
    private static final boolean ALWAYS_QUOTE_STRINGS = false;

    private final NBTLimits limits;

    /**
     * Creates a serializer with Minecraft-compatible formatting.
     */
    public SNBTSerializer()
    {
        this(NBTLimits.DEFAULT);
    }

    /**
     * Creates an SNBT serializer with configurable limits.
     *
     * @param limits the limits for each serialization operation
     * @throws NullPointerException if limits is null
     */
    public SNBTSerializer(NBTLimits limits)
    {
        this.limits = java.util.Objects.requireNonNull(limits, "limits");
    }

    /**
     * Returns the tag as SNBT, or the string {@code "null"} for null input.
     *
     * @param tag the NBT tag to serialize
     * @return the SNBT string representation
     * @throws IllegalArgumentException if a tag or payload is invalid, a number is non-finite,
     *                                  keys are duplicated, the graph is cyclic, or a limit is exceeded
     */
    public String serialize(ITag<?> tag)
    {
        LimitedText sb = new LimitedText(limits);
        if (tag == null)
        {
            sb.append("null");
            return sb.toString();
        }

        serializeTag(tag, sb, 0);
        return sb.toString();
    }

    /**
     * Writes an NBT tag to a text file in SNBT format. Serialization finishes before opening the file.
     *
     * @param tag  the NBT tag to serialize
     * @param file the file to write to
     * @throws IOException              if writing fails
     * @throws IllegalArgumentException if a tag or payload is invalid, a number is non-finite,
     *                                  keys are duplicated, the graph is cyclic, or a limit is exceeded
     */
    public void writeToFile(ITag<?> tag, File file) throws IOException
    {
        String snbt = serialize(tag);
        try (FileWriter writer = new FileWriter(file))
        {
            writer.write(snbt);
            if (ADD_TRAILING_NEWLINE)
            {
                writer.write(System.lineSeparator());
            }
        }
    }

    /**
     * Serializes any tag type to the string builder.
     *
     * @param tag   the tag to serialize
     * @param sb    the string builder to append to
     * @param depth the current nesting depth
     */
    private void serializeTag(ITag<?> tag, LimitedText sb, int depth)
    {
        if (tag == null) throw new IllegalArgumentException("Null collection child");
        NBTModel.requireTag(tag);
        sb.budget.tag();
        int tagId = tag.getId();
        Object data = tag.getData();
        boolean valid = switch (tagId)
        {
            case 1 -> data instanceof Byte;
            case 2 -> data instanceof Short;
            case 3 -> data instanceof Integer;
            case 4 -> data instanceof Long;
            case 5 -> data instanceof Float;
            case 6 -> data instanceof Double;
            case 7 -> data instanceof byte[];
            case 8 -> data instanceof String;
            case 9 -> tag instanceof IListTag && data instanceof List<?>;
            case 10 -> tag instanceof ICompoundTag && data instanceof List<?>;
            case 11 -> data instanceof int[];
            case 12 -> data instanceof long[];
            case 0 -> throw new IllegalArgumentException("TAG_End is a structural marker, not a value");
            default -> throw new IllegalArgumentException("Unsupported SNBT tag type: " + tagId);
        };
        if (!valid) throw new IllegalArgumentException("Invalid payload for NBT type " + tagId);

        boolean container = tagId == 9 || tagId == 10;
        if (container)
        {
            sb.budget.depth(depth + 1);
            if (!sb.active.add(tag)) throw new IllegalArgumentException("Cyclic NBT collection graph");
        }
        switch (tagId)
        {
            case 1 -> serializeByte((Byte) data, sb);
            case 2 -> serializeShort((Short) data, sb);
            case 3 -> serializeInt((Integer) data, sb);
            case 4 -> serializeLong((Long) data, sb);
            case 5 -> serializeFloat((Float) data, sb);
            case 6 -> serializeDouble((Double) data, sb);
            case 7 -> serializeByteArray((byte[]) data, sb);
            case 8 -> serializeString((String) data, sb);
            case 9 -> serializeList((IListTag) tag, sb, depth);
            case 10 -> serializeCompound((ICompoundTag) tag, sb, depth);
            case 11 -> serializeIntArray((int[]) data, sb);
            case 12 -> serializeLongArray((long[]) data, sb);
        }
        if (container) sb.active.remove(tag);
    }

    /**
     * Serializes a byte value with 'b' suffix.
     *
     * @param value the byte value
     * @param sb    the string builder to append to
     */
    private void serializeByte(Byte value, LimitedText sb)
    {
        sb.append(value).append('b');
    }

    /**
     * Serializes a short value with 's' suffix.
     *
     * @param value the short value
     * @param sb    the string builder to append to
     */
    private void serializeShort(Short value, LimitedText sb)
    {
        sb.append(value).append('s');
    }

    /**
     * Serializes an int value without suffix.
     *
     * @param value the int value
     * @param sb    the string builder to append to
     */
    private void serializeInt(Integer value, LimitedText sb)
    {
        sb.append(value);
    }

    /**
     * Serializes a long value with 'l' suffix.
     *
     * @param value the long value
     * @param sb    the string builder to append to
     */
    private void serializeLong(Long value, LimitedText sb)
    {
        sb.append(value).append('l');
    }

    /**
     * Writes a finite float with an f suffix.
     *
     * @param value the float value
     * @param sb    the string builder to append to
     * @throws IllegalArgumentException if the value is NaN or infinite
     */
    private void serializeFloat(Float value, LimitedText sb)
    {
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Non-finite SNBT float: " + value);
        sb.append(value).append('f');
    }

    /**
     * Writes a finite double. Doubles have no suffix by default.
     *
     * @param value the double value
     * @param sb    the string builder to append to
     * @throws IllegalArgumentException if the value is NaN or infinite
     */
    private void serializeDouble(Double value, LimitedText sb)
    {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite SNBT double: " + value);
        sb.append(value);
        if (EXPLICIT_DOUBLE_TYPE) sb.append('d');
    }

    /**
     * Writes a non-null string with quotes and escapes when needed.
     *
     * @param value the string value
     * @param sb    the string builder to append to
     */
    private void serializeString(String value, LimitedText sb)
    {
        if (value == null) throw new IllegalArgumentException("SNBT string cannot be null");

        sb.checkLength(value.length());
        if (needsQuoting(value))
        {
            sb.append('"');
            escapeString(value, sb);
            sb.append('"');
        } else
        {
            sb.append(value);
        }
    }

    /**
     * Quotes strings outside a conservative ASCII identifier subset, and literals
     * that could be interpreted as numbers or booleans.
     *
     * @param value the string to check
     * @return true if the string must be quoted
     */
    private boolean needsQuoting(String value)
    {
        if (value.isEmpty())
        {
            return true;
        }

        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
        {
            return true;
        }

        char first = value.charAt(0);
        if ((first >= '0' && first <= '9') || first == '-' || first == '.' || first == '+')
        {
            return true;
        }

        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z') && !(c >= '0' && c <= '9') && c != '_')
            {
                return true;
            }
        }

        return ALWAYS_QUOTE_STRINGS;
    }

    /**
     * Escapes special characters in strings for SNBT format.
     *
     * @param value the string to escape
     * @param sb    the string builder to append to
     */
    private void escapeString(String value, LimitedText sb)
    {
        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            switch (c)
            {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    // Keep output within printable ASCII, from space through tilde.
                    if (c < 32 || c > 126)
                    {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else
                    {
                        sb.append(c);
                    }
                    break;
            }
        }
    }

    /**
     * Writes a byte array with the {@code [B;} prefix.
     *
     * @param array the byte array
     * @param sb    the string builder to append to
     */
    private void serializeByteArray(byte[] array, LimitedText sb)
    {
        sb.budget.length(array.length);
        sb.budget.allocate(16L + array.length);
        sb.append("[B;");
        for (int i = 0; i < array.length; i++)
        {
            if (i > 0)
            {
                sb.append(',');
                if (SPACES_IN_ARRAYS) sb.append(' ');
            } else if (SPACES_IN_ARRAYS && array.length > 0)
            {
                sb.append(' ');
            }
            sb.append(array[i]).append('b');
        }
        if (SPACES_IN_ARRAYS && array.length > 0)
        {
            sb.append(' ');
        }
        sb.append(']');
    }

    /**
     * Writes an int array with the {@code [I;} prefix.
     *
     * @param array the int array
     * @param sb    the string builder to append to
     */
    private void serializeIntArray(int[] array, LimitedText sb)
    {
        sb.budget.length(array.length);
        sb.budget.allocate(16 + 4L * array.length);
        sb.append("[I;");
        for (int i = 0; i < array.length; i++)
        {
            if (i > 0)
            {
                sb.append(',');
                if (SPACES_IN_ARRAYS) sb.append(' ');
            } else if (SPACES_IN_ARRAYS && array.length > 0)
            {
                sb.append(' ');
            }
            sb.append(array[i]);
        }
        if (SPACES_IN_ARRAYS && array.length > 0)
        {
            sb.append(' ');
        }
        sb.append(']');
    }

    /**
     * Writes a long array with the {@code [L;} prefix.
     *
     * @param array the long array
     * @param sb    the string builder to append to
     */
    private void serializeLongArray(long[] array, LimitedText sb)
    {
        sb.budget.length(array.length);
        sb.budget.allocate(16 + 8L * array.length);
        sb.append("[L;");
        for (int i = 0; i < array.length; i++)
        {
            if (i > 0)
            {
                sb.append(',');
                if (SPACES_IN_ARRAYS) sb.append(' ');
            } else if (SPACES_IN_ARRAYS && array.length > 0)
            {
                sb.append(' ');
            }
            sb.append(array[i]).append('l');
        }
        if (SPACES_IN_ARRAYS && array.length > 0)
        {
            sb.append(' ');
        }
        sb.append(']');
    }

    /**
     * Writes a compound with named children.
     *
     * @param compound the compound tag to serialize
     * @param sb       the string builder to append to
     * @param depth    the current nesting depth
     */
    private void serializeCompound(ICompoundTag compound, LimitedText sb, int depth)
    {
        sb.budget.length(compound.size());
        sb.budget.requireTags(compound.size());
        sb.budget.allocate(64L * compound.size());
        List<?> tags = compound.getData();
        Set<String> names = new HashSet<>();
        for (Object value : tags)
        {
            Tag<?> child = NBTModel.requireTag(value);
            if (!names.add(child.getName()))
            {
                throw new IllegalArgumentException("Duplicate compound key: " + child.getName());
            }
        }

        sb.append('{');

        for (int i = 0; i < tags.size(); i++)
        {
            if (i > 0)
            {
                sb.append(',');
                if (SPACES_IN_COMPOUNDS)
                {
                    sb.append(' ');
                }
            }

            Tag<?> tag = NBTModel.requireTag(tags.get(i));

            serializeString(tag.getName(), sb);
            sb.append(':');
            if (SPACES_AROUND_COLON)
            {
                sb.append(' ');
            }

            serializeTag(tag, sb, depth + 1);
        }

        sb.append('}');
    }

    /**
     * Writes a list of values.
     *
     * @param list  the list tag to serialize
     * @param sb    the string builder to append to
     * @param depth the current nesting depth
     */
    private void serializeList(IListTag list, LimitedText sb, int depth)
    {
        sb.budget.length(list.size());
        sb.budget.requireTags(list.size());
        sb.budget.allocate(32L * list.size());
        List<?> tags = list.getData();
        int listType = list.getListTypeID();
        if (listType < 0 || listType > 12 || (listType == 0 && !tags.isEmpty()))
        {
            throw new IllegalArgumentException("Invalid list element type: " + listType);
        }
        for (Object value : tags)
        {
            Tag<?> child = NBTModel.requireTag(value);
            if (child.getId() != listType)
            {
                throw new IllegalArgumentException("Invalid collection child or list element type mismatch");
            }
        }

        sb.append('[');

        for (int i = 0; i < tags.size(); i++)
        {
            if (i > 0)
            {
                sb.append(',');
                if (SPACES_IN_LISTS)
                {
                    sb.append(' ');
                }
            } else if (SPACES_IN_LISTS && !tags.isEmpty())
            {
                sb.append(' ');
            }

            serializeTag(NBTModel.requireTag(tags.get(i)), sb, depth + 1);
        }

        if (SPACES_IN_LISTS && !tags.isEmpty())
        {
            sb.append(' ');
        }

        sb.append(']');
    }

    /**
     * Checks output growth before appending, with accounting local to each serialization.
     */
    private static final class LimitedText
    {
        private final StringBuilder text = new StringBuilder();
        private final NBTBudget budget;
        private final int maxLength;
        private final Set<ITag<?>> active = Collections.newSetFromMap(new IdentityHashMap<>());

        /**
         * Creates an empty bounded output buffer.
         *
         * @param limits the limits for this serialization
         * @throws NullPointerException if limits is null
         */
        private LimitedText(NBTLimits limits)
        {
            budget = new NBTBudget(limits);
            maxLength = limits.maxTextLength();
        }

        /**
         * Checks and appends a string or formatted scalar.
         *
         * @param value the value to format, with null written as "null"
         * @return this output buffer
         * @throws IllegalArgumentException if the text or allocation limit is exceeded
         */
        private LimitedText append(Object value)
        {
            String part = String.valueOf(value);
            reserve(part.length());
            text.append(part);
            return this;
        }

        /**
         * Checks and appends one character without a temporary string.
         *
         * @param value the character to append
         * @return this output buffer
         * @throws IllegalArgumentException if the text or allocation limit is exceeded
         */
        private LimitedText append(char value)
        {
            reserve(1);
            text.append(value);
            return this;
        }

        /**
         * Reserves output storage, including growth and the final string copy.
         *
         * @param count the additional UTF-16 code units
         * @throws IllegalArgumentException if the text or allocation limit is exceeded
         */
        private void reserve(int count)
        {
            checkLength(count);
            budget.allocate(4L * count);
        }

        /**
         * Checks the minimum output length before scanning or escaping a string.
         *
         * @param count the additional UTF-16 code units
         * @throws IllegalArgumentException if the text limit is exceeded
         */
        private void checkLength(int count)
        {
            if (count > maxLength - text.length())
                throw new IllegalArgumentException("SNBT text length limit exceeded: " + maxLength);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return text.toString();
        }
    }

}
