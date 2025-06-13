package de.paulferlitz.formats.snbt;

import de.paulferlitz.api.ICompoundTag;
import de.paulferlitz.api.IListTag;
import de.paulferlitz.api.ITag;
import de.paulferlitz.core.Tag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Serializer for converting NBT tags into SNBT (Stringified NBT) format.
 * Produces valid SNBT strings that can be used in Minecraft commands and debugging.
 * Uses Minecraft-compatible formatting by default.
 */
public class SNBTSerializer
{

    // Default Minecraft-style formatting settings
    private static final boolean SPACES_AROUND_COLON = true;
    private static final boolean SPACES_IN_COMPOUNDS = true;
    private static final boolean SPACES_IN_LISTS = true;
    private static final boolean SPACES_IN_ARRAYS = false;
    private static final boolean EXPLICIT_DOUBLE_TYPE = false;
    private static final boolean ADD_TRAILING_NEWLINE = true;
    private static final boolean ALWAYS_QUOTE_STRINGS = false;

    /**
     * Creates a serializer with Minecraft-compatible formatting.
     */
    public SNBTSerializer()
    {
        // Uses default Minecraft-style formatting
    }

    /**
     * Serializes an NBT tag to SNBT string format.
     *
     * @param tag the NBT tag to serialize
     * @return the SNBT string representation
     */
    public String serialize(ITag<?> tag)
    {
        if (tag == null)
        {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        serializeTag(tag, sb, 0);
        return sb.toString();
    }

    /**
     * Writes an NBT tag to a text file in SNBT format.
     *
     * @param tag  the NBT tag to serialize
     * @param file the file to write to
     * @throws IOException if writing fails
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
     * @param depth the current nesting depth (for potential formatting)
     */
    private void serializeTag(ITag<?> tag, StringBuilder sb, int depth)
    {
        if (tag instanceof ICompoundTag)
        {
            serializeCompound((ICompoundTag) tag, sb, depth);
        } else if (tag instanceof IListTag)
        {
            serializeList((IListTag) tag, sb, depth);
        } else
        {
            Object data = tag.getData();

            // Use NBT type ID for precise type detection
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
    }

    /**
     * Serializes a byte value with 'b' suffix.
     *
     * @param value the byte value
     * @param sb    the string builder to append to
     */
    private void serializeByte(Byte value, StringBuilder sb)
    {
        sb.append(value).append('b');
    }

    /**
     * Serializes a short value with 's' suffix.
     *
     * @param value the short value
     * @param sb    the string builder to append to
     */
    private void serializeShort(Short value, StringBuilder sb)
    {
        sb.append(value).append('s');
    }

    /**
     * Serializes an int value without suffix.
     *
     * @param value the int value
     * @param sb    the string builder to append to
     */
    private void serializeInt(Integer value, StringBuilder sb)
    {
        sb.append(value);
    }

    /**
     * Serializes a long value with 'l' suffix.
     *
     * @param value the long value
     * @param sb    the string builder to append to
     */
    private void serializeLong(Long value, StringBuilder sb)
    {
        sb.append(value).append('l');
    }

    /**
     * Serializes a float value with 'f' suffix.
     * Handles special values like NaN and Infinity.
     *
     * @param value the float value
     * @param sb    the string builder to append to
     */
    private void serializeFloat(Float value, StringBuilder sb)
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
     * Serializes a double value without suffix by default.
     * Handles special values like NaN and Infinity.
     *
     * @param value the double value
     * @param sb    the string builder to append to
     */
    private void serializeDouble(Double value, StringBuilder sb)
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
     * Serializes a string with smart quoting and escaping.
     * Only quotes when necessary based on SNBT rules.
     *
     * @param value the string value
     * @param sb    the string builder to append to
     */
    private void serializeString(String value, StringBuilder sb)
    {
        if (value == null)
        {
            sb.append("\"\"");
            return;
        }

        // Check if we need to quote the string
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

        // Check for reserved words
        if (value.equals("true") || value.equals("false"))
        {
            return true;
        }

        // Check if it looks like a number
        if (looksLikeNumber(value))
        {
            return true;
        }

        // According to SNBT spec: quotes optional if string contains only 0-9, A-Z, a-z, _, and doesn't start with digit, -, ., +
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

        // Based on test expectations, it seems we should quote most strings for safety
        // Only leave very simple identifier-like strings unquoted
        return ALWAYS_QUOTE_STRINGS;
    }

    /**
     * Checks if a string looks like a number that might be confused with numeric literals.
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
     * Handles standard escape sequences and unicode characters.
     *
     * @param value the string to escape
     * @param sb    the string builder to append to
     */
    private void escapeString(String value, StringBuilder sb)
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
                    if (c < 32 || c > 126)
                    {
                        // Non-printable ASCII characters as unicode escapes
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
     * Serializes a byte array with [B; prefix.
     *
     * @param array the byte array
     * @param sb    the string builder to append to
     */
    private void serializeByteArray(byte[] array, StringBuilder sb)
    {
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
     * Serializes an int array with [I; prefix.
     *
     * @param array the int array
     * @param sb    the string builder to append to
     */
    private void serializeIntArray(int[] array, StringBuilder sb)
    {
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
     * Serializes a long array with [L; prefix.
     *
     * @param array the long array
     * @param sb    the string builder to append to
     */
    private void serializeLongArray(long[] array, StringBuilder sb)
    {
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
     * Serializes a compound tag with proper key-value formatting.
     *
     * @param compound the compound tag to serialize
     * @param sb       the string builder to append to
     * @param depth    the current nesting depth
     */
    private void serializeCompound(ICompoundTag compound, StringBuilder sb, int depth)
    {
        List<Tag<?>> tags = compound.getData();

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

            // Serialize key
            serializeString(tag.getName(), sb);
            sb.append(':');
            if (SPACES_AROUND_COLON)
            {
                sb.append(' ');
            }

            // Serialize value
            serializeTag(tag, sb, depth + 1);
        }

        sb.append('}');
    }

    /**
     * Serializes a list tag with proper element formatting.
     *
     * @param list  the list tag to serialize
     * @param sb    the string builder to append to
     * @param depth the current nesting depth
     */
    private void serializeList(IListTag list, StringBuilder sb, int depth)
    {
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
}