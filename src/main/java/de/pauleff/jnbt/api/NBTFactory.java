package de.pauleff.jnbt.api;

import de.pauleff.jnbt.core.*;
import de.pauleff.jnbt.formats.snbt.SNBTException;
import de.pauleff.jnbt.formats.snbt.SNBTParser;
import de.pauleff.jnbt.formats.snbt.SNBTSerializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Creates NBT tags and converts tags to and from SNBT. Null names become the string {@code "null"}.
 * Empty names and whitespace are kept.
 *
 * @since 1.3
 */
public class NBTFactory
{
    /**
     * Creates an empty compound tag.
     *
     * @return the new tag
     */
    public static ICompoundTag createCompound()
    {
        return new Tag_Compound();
    }

    /**
     * Creates a named compound tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ICompoundTag createCompound(String name)
    {
        return new Tag_Compound(name);
    }

    /**
     * Creates a compound tag with initial child tags.
     *
     * @param name the tag name
     * @param data Initial child tags to add
     * @return the new tag
     */
    public static ICompoundTag createCompound(String name, ArrayList<ITag<?>> data)
    {
        if (data == null) throw new IllegalArgumentException("Compound data cannot be null");
        ArrayList<Tag<?>> tagData = new ArrayList<>();
        for (ITag<?> tag : data)
        {
            tagData.add((Tag<?>) tag);
        }
        return new Tag_Compound(name, tagData);
    }

    /**
     * Creates an empty list tag with the given element type.
     *
     * @param listTypeID the NBT type ID of elements this list will contain
     * @return the new tag
     */
    public static IListTag createList(int listTypeID)
    {
        return new Tag_List(listTypeID);
    }

    /**
     * Creates a named list tag with the given element type.
     *
     * @param name       the tag name
     * @param listTypeID the NBT type ID of elements this list will contain
     * @return the new tag
     */
    public static IListTag createList(String name, int listTypeID)
    {
        return new Tag_List(name, listTypeID);
    }

    /**
     * Creates an empty string tag.
     *
     * @return the new tag
     */
    public static ITag<String> createString()
    {
        return new Tag_String();
    }

    /**
     * Creates a named string tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<String> createString(String name)
    {
        return new Tag_String(name);
    }

    /**
     * Creates a string tag with name and value.
     *
     * @param name  the tag name
     * @param value the string value
     * @return the new tag
     */
    public static ITag<String> createString(String name, String value)
    {
        return new Tag_String(name, value);
    }

    /**
     * Creates an empty integer tag.
     *
     * @return the new tag
     */
    public static ITag<Integer> createInt()
    {
        return new Tag_Int();
    }

    /**
     * Creates a named integer tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<Integer> createInt(String name)
    {
        return new Tag_Int(name);
    }

    /**
     * Creates an integer tag with name and value.
     *
     * @param name  the tag name
     * @param value the integer value
     * @return the new tag
     */
    public static ITag<Integer> createInt(String name, int value)
    {
        return new Tag_Int(name, value);
    }

    /**
     * Creates an empty double tag.
     *
     * @return the new tag
     */
    public static ITag<Double> createDouble()
    {
        return new Tag_Double();
    }

    /**
     * Creates a named double tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<Double> createDouble(String name)
    {
        return new Tag_Double(name);
    }

    /**
     * Creates a double tag with name and value.
     *
     * @param name  the tag name
     * @param value the double value
     * @return the new tag
     */
    public static ITag<Double> createDouble(String name, double value)
    {
        return new Tag_Double(name, value);
    }

    /**
     * Creates an empty float tag.
     *
     * @return the new tag
     */
    public static ITag<Float> createFloat()
    {
        return new Tag_Float();
    }

    /**
     * Creates a named float tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<Float> createFloat(String name)
    {
        return new Tag_Float(name);
    }

    /**
     * Creates a float tag with name and value.
     *
     * @param name  the tag name
     * @param value the float value
     * @return the new tag
     */
    public static ITag<Float> createFloat(String name, float value)
    {
        return new Tag_Float(name, value);
    }

    /**
     * Creates an empty byte tag.
     *
     * @return the new tag
     */
    public static ITag<Byte> createByte()
    {
        return new Tag_Byte();
    }

    /**
     * Creates a named byte tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<Byte> createByte(String name)
    {
        return new Tag_Byte(name);
    }

    /**
     * Creates a byte tag with name and value.
     *
     * @param name  the tag name
     * @param value the byte value
     * @return the new tag
     */
    public static ITag<Byte> createByte(String name, byte value)
    {
        return new Tag_Byte(name, value);
    }

    /**
     * Creates an empty short tag.
     *
     * @return the new tag
     */
    public static ITag<Short> createShort()
    {
        return new Tag_Short();
    }

    /**
     * Creates a named short tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<Short> createShort(String name)
    {
        return new Tag_Short(name);
    }

    /**
     * Creates a short tag with name and value.
     *
     * @param name  the tag name
     * @param value the short value
     * @return the new tag
     */
    public static ITag<Short> createShort(String name, short value)
    {
        return new Tag_Short(name, value);
    }

    /**
     * Creates an empty long tag.
     *
     * @return the new tag
     */
    public static ITag<Long> createLong()
    {
        return new Tag_Long();
    }

    /**
     * Creates a named long tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<Long> createLong(String name)
    {
        return new Tag_Long(name);
    }

    /**
     * Creates a long tag with name and value.
     *
     * @param name  the tag name
     * @param value the long value
     * @return the new tag
     */
    public static ITag<Long> createLong(String name, long value)
    {
        return new Tag_Long(name, value);
    }

    /**
     * Creates an empty byte array tag.
     *
     * @return the new tag
     */
    public static ITag<byte[]> createByteArray()
    {
        return new Tag_Byte_Array();
    }

    /**
     * Creates a named byte array tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<byte[]> createByteArray(String name)
    {
        return new Tag_Byte_Array(name);
    }

    /**
     * Creates a byte array tag with name and value.
     *
     * @param name  the tag name
     * @param value the byte array value
     * @return the new tag
     */
    public static ITag<byte[]> createByteArray(String name, byte[] value)
    {
        return new Tag_Byte_Array(name, value);
    }

    /**
     * Creates an empty integer array tag.
     *
     * @return the new tag
     */
    public static ITag<int[]> createIntArray()
    {
        return new Tag_Int_Array();
    }

    /**
     * Creates a named integer array tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<int[]> createIntArray(String name)
    {
        return new Tag_Int_Array(name);
    }

    /**
     * Creates an integer array tag with name and value.
     *
     * @param name  the tag name
     * @param value the integer array value
     * @return the new tag
     */
    public static ITag<int[]> createIntArray(String name, int[] value)
    {
        return new Tag_Int_Array(name, value);
    }

    /**
     * Creates an empty long array tag.
     *
     * @return the new tag
     */
    public static ITag<long[]> createLongArray()
    {
        return new Tag_Long_Array();
    }

    /**
     * Creates a named long array tag.
     *
     * @param name the tag name
     * @return the new tag
     */
    public static ITag<long[]> createLongArray(String name)
    {
        return new Tag_Long_Array(name);
    }

    /**
     * Creates a long array tag with name and value.
     *
     * @param name  the tag name
     * @param value the long array value
     * @return the new tag
     */
    public static ITag<long[]> createLongArray(String name, long[] value)
    {
        return new Tag_Long_Array(name, value);
    }

    /**
     * Creates a simple NBT structure with key-value string pairs.
     *
     * @param rootName      the name of the root compound tag
     * @param keyValuePairs Alternating keys and values (key1, value1, key2, value2,...)
     * @return the new tag
     * @throws IllegalArgumentException if an odd number of arguments is provided
     */
    public static ICompoundTag createSimpleCompound(String rootName, String... keyValuePairs)
    {
        if (keyValuePairs.length % 2 != 0)
        {
            throw new IllegalArgumentException("Must provide an even number of arguments (key-value pairs)");
        }

        ICompoundTag compound = createCompound(rootName);
        for (int i = 0; i < keyValuePairs.length; i += 2)
        {
            compound.addString(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return compound;
    }

    /**
     * Parses SNBT (Stringified NBT) text into an NBT tag structure.
     *
     * @param snbt the SNBT string to parse
     * @return the parsed NBT tag structure
     * @throws SNBTException if the SNBT syntax is invalid
     */
    public static ITag<?> parseFromSNBT(String snbt) throws SNBTException
    {
        if (snbt == null || snbt.trim().isEmpty())
        {
            throw new SNBTException("SNBT string cannot be null or empty");
        }

        SNBTParser parser = new SNBTParser(snbt.trim());
        return parser.parse();
    }

    /**
     * Parses SNBT text into a compound tag.
     *
     * @param snbt the SNBT string to parse (must represent a compound)
     * @return the parsed compound tag
     * @throws SNBTException if the SNBT syntax is invalid or doesn't represent a compound
     */
    public static ICompoundTag parseCompoundFromSNBT(String snbt) throws SNBTException
    {
        ITag<?> tag = parseFromSNBT(snbt);
        if (!(tag instanceof ICompoundTag))
        {
            throw new SNBTException("SNBT does not represent a compound tag");
        }
        return (ICompoundTag) tag;
    }

    /**
     * Parses SNBT text into a list tag.
     *
     * @param snbt the SNBT string to parse (must represent a list)
     * @return the parsed list tag
     * @throws SNBTException if the SNBT syntax is invalid or doesn't represent a list
     */
    public static IListTag parseListFromSNBT(String snbt) throws SNBTException
    {
        ITag<?> tag = parseFromSNBT(snbt);
        if (!(tag instanceof IListTag))
        {
            throw new SNBTException("SNBT does not represent a list tag");
        }
        return (IListTag) tag;
    }

    /**
     * Converts an NBT tag to SNBT (Stringified NBT) format.
     *
     * @param tag the NBT tag to serialize
     * @return the SNBT string representation
     */
    public static String toSNBT(ITag<?> tag)
    {
        SNBTSerializer serializer = new SNBTSerializer();
        return serializer.serialize(tag);
    }

    /**
     * Writes an NBT tag to a text file in SNBT format.
     *
     * @param tag  the NBT tag to write
     * @param file the output file
     * @throws IOException if writing to the file fails
     */
    public static void writeToSNBTFile(ITag<?> tag, File file) throws IOException
    {
        SNBTSerializer serializer = new SNBTSerializer();
        serializer.writeToFile(tag, file);
    }
}
