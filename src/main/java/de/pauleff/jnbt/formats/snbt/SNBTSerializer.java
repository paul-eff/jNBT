package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTLimits;
import de.pauleff.jnbt.internal.NBTBudget;
import de.pauleff.jnbt.core.Tag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 *  Converts NBT tags to SNBT strings. Uses {@link NBTLimits#DEFAULT}
 * unless limits are supplied to the constructor.
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

    /** Creates a serializer with Minecraft-compatible formatting. */
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
     * @throws IllegalArgumentException if keys are duplicated, the graph is cyclic, or a limit is exceeded
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
     * @throws IOException if writing fails
     * @throws IllegalArgumentException if keys are duplicated, the graph is cyclic, or a limit is exceeded
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
        sb.budget.tag();
        boolean container = tag instanceof ICompoundTag || tag instanceof IListTag;
        if (container)
        {
            sb.budget.depth(depth + 1);
            if (!sb.active.add(tag)) throw new IllegalArgumentException("Cyclic NBT collection graph");
        }
        if (tag instanceof ICompoundTag)
        {
            serializeCompound((ICompoundTag) tag, sb, depth);
        } else if (tag instanceof IListTag)
        {
            serializeList((IListTag) tag, sb, depth);
        } else
        {
            Object data = tag.getData();

            int tagId = tag.getId();

            switch (tagId)
            {
                case 1: // NBT_Byte
                    if (data instanceof Byte)
                    {
                        serializeByte((Byte) data, sb);
                    } else
                    {
                        sb.append("\"invalid_byte\"");
                    }
                    break;
                case 2: // NBT_Short
                    if (data instanceof Short)
                    {
                        serializeShort((Short) data, sb);
                    } else
                    {
                        sb.append("\"invalid_short\"");
                    }
                    break;
                case 3: // NBT_Int
                    if (data instanceof Integer)
                    {
                        serializeInt((Integer) data, sb);
                    } else
                    {
                        sb.append("\"invalid_int\"");
                    }
                    break;
                case 4: // NBT_Long
                    if (data instanceof Long)
                    {
                        serializeLong((Long) data, sb);
                    } else
                    {
                        sb.append("\"invalid_long\"");
                    }
                    break;
                case 5: // NBT_Float
                    if (data instanceof Float)
                    {
                        serializeFloat((Float) data, sb);
                    } else
                    {
                        sb.append("\"invalid_float\"");
                    }
                    break;
                case 6: // NBT_Double
                    if (data instanceof Double)
                    {
                        serializeDouble((Double) data, sb);
                    } else
                    {
                        sb.append("\"invalid_double\"");
                    }
                    break;
                case 7: // NBT_Byte_Array
                    if (data instanceof byte[])
                    {
                        serializeByteArray((byte[]) data, sb);
                    } else
                    {
                        sb.append("\"invalid_byte_array\"");
                    }
                    break;
                case 8: // NBT_String
                    serializeString((String) data, sb);
                    break;
                case 11: // NBT_Int_Array
                    if (data instanceof int[])
                    {
                        serializeIntArray((int[]) data, sb);
                    } else
                    {
                        sb.append("\"invalid_int_array\"");
                    }
                    break;
                case 12: // NBT_Long_Array
                    if (data instanceof long[])
                    {
                        serializeLongArray((long[]) data, sb);
                    } else
                    {
                        sb.append("\"invalid_long_array\"");
                    }
                    break;
                default:
                    sb.append("\"unknown_type_").append(tagId).append("\"");
                    break;
            }
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
     * Writes a float, NaN, or Infinity with an f suffix.
     *
     * @param value the float value
     * @param sb    the string builder to append to
     */
    private void serializeFloat(Float value, LimitedText sb)
    {
        if (Float.isNaN(value))
        {
            sb.append("NaNf");
        } else if (Float.isInfinite(value))
        {
            sb.append(value > 0 ? "Infinityf" : "-Infinityf");
        } else
        {
            sb.append(value).append('f');
        }
    }

    /**
     * Writes a double, NaN, or Infinity. Doubles have no suffix by default.
     *
     * @param value the double value
     * @param sb    the string builder to append to
     */
    private void serializeDouble(Double value, LimitedText sb)
    {
        if (Double.isNaN(value))
        {
            sb.append("NaN");
        } else if (Double.isInfinite(value))
        {
            sb.append(value > 0 ? "Infinity" : "-Infinity");
        } else
        {
            if (EXPLICIT_DOUBLE_TYPE)
            {
                sb.append(value).append('d');
            } else
            {
                sb.append(value);
            }
        }
    }

    /**
     * Writes a string with quotes and escapes when needed. Null becomes an empty quoted string.
     *
     * @param value the string value
     * @param sb    the string builder to append to
     */
    private void serializeString(String value, LimitedText sb)
    {
        if (value == null)
        {
            sb.append("\"\"");
            return;
        }

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
     * Determines if a string needs to be quoted based on SNBT rules.
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

        if (value.equals("true") || value.equals("false"))
        {
            return true;
        }

        if (looksLikeNumber(value))
        {
            return true;
        }

        char first = value.charAt(0);
        if (Character.isDigit(first) || first == '-' || first == '.' || first == '+')
        {
            return true;
        }

        for (int i = 0; i < value.length(); i++)
        {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_')
            {
                return true;
            }
        }

        return ALWAYS_QUOTE_STRINGS;
    }

    /**
     * Checks whether a string starts like a number.
     *
     * @param value the string to check
     * @return true if the string looks like a number
     */
    private boolean looksLikeNumber(String value)
    {
        if (value.isEmpty())
        {
            return false;
        }

        char first = value.charAt(0);
        return Character.isDigit(first) || first == '-' || first == '+';
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
        sb.budget.allocate(16 + 1L * array.length);
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
        List<Tag<?>> tags = compound.getData();
        Set<String> names = new HashSet<>();
        for (Tag<?> child : tags)
        {
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

            Tag<?> tag = tags.get(i);

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
        List<Tag<?>> tags = list.getData();

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

            serializeTag(tags.get(i), sb, depth + 1);
        }

        if (SPACES_IN_LISTS && !tags.isEmpty())
        {
            sb.append(' ');
        }

        sb.append(']');
    }

    /** Checks output growth before appending, with accounting local to each serialization. */
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

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return text.toString();
        }
    }

}
