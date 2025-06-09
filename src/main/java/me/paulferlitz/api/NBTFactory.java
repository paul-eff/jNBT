package me.paulferlitz.api;

import me.paulferlitz.core.*;
import java.util.ArrayList;

/**
 * Factory for creating NBT tags through the public API.
 * Provides convenient static methods for building NBT structures without dealing with constructors directly.
 * Use this when you need to create individual tags programmatically.
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * // Create a player data structure
 * ICompoundTag player = NBTFactory.createCompound("Player");
 * player.addString("Name", "Steve")
 *       .addInt("Level", 42)
 *       .addDouble("Health", 20.0);
 *
 * // Create inventory items
 * IListTag inventory = NBTFactory.createList("Inventory", NBTTags.Tag_Compound.getId());
 * }</pre>
 *
 * @author Paul Ferlitz
 * @see ITag
 * @see ICompoundTag
 * @see IListTag
 */
public class NBTFactory
{
    /**
     * Creates an empty compound tag.
     *
     * @return New {@link ICompoundTag} instance
     */
    public static ICompoundTag createCompound()
    {
        return new Tag_Compound();
    }

    /**
     * Creates a named compound tag.
     *
     * @param name The tag name
     * @return New {@link ICompoundTag} with the specified name
     */
    public static ICompoundTag createCompound(String name)
    {
        return new Tag_Compound(name);
    }

    /**
     * Creates a compound tag with initial child tags.
     *
     * @param name The tag name
     * @param data Initial child tags to add
     * @return New {@link ICompoundTag} containing the provided children
     */
    public static ICompoundTag createCompound(String name, ArrayList<ITag<?>> data)
    {
        ArrayList<Tag<?>> tagData = new ArrayList<>();
        for (ITag<?> tag : data) {
            tagData.add((Tag<?>) tag);
        }
        return new Tag_Compound(name, tagData);
    }

    /**
     * Creates an empty list tag for the specified element type.
     *
     * @param listTypeID The NBT type ID of elements this list will contain
     * @return New {@link IListTag} instance
     */
    public static IListTag createList(int listTypeID)
    {
        return new Tag_List(listTypeID);
    }

    /**
     * Creates a named list tag for the specified element type.
     *
     * @param name The tag name
     * @param listTypeID The NBT type ID of elements this list will contain
     * @return New {@link IListTag} with the specified name and type
     */
    public static IListTag createList(String name, int listTypeID)
    {
        return new Tag_List(name, listTypeID);
    }

    /**
     * Creates an empty string tag.
     *
     * @return New string {@link ITag}
     */
    public static ITag<String> createString()
    {
        return new Tag_String();
    }

    /**
     * Creates a named string tag.
     *
     * @param name The tag name
     * @return New string {@link ITag} with the specified name
     */
    public static ITag<String> createString(String name)
    {
        return new Tag_String(name);
    }

    /**
     * Creates a string tag with name and value.
     *
     * @param name The tag name
     * @param value The string value
     * @return New string {@link ITag} with the specified name and value
     */
    public static ITag<String> createString(String name, String value)
    {
        return new Tag_String(name, value);
    }

    /**
     * Creates an empty integer tag.
     *
     * @return New integer {@link ITag}
     */
    public static ITag<Integer> createInt()
    {
        return new Tag_Int();
    }

    /**
     * Creates a named integer tag.
     *
     * @param name The tag name
     * @return New integer {@link ITag} with the specified name
     */
    public static ITag<Integer> createInt(String name)
    {
        return new Tag_Int(name);
    }

    /**
     * Creates an integer tag with name and value.
     *
     * @param name The tag name
     * @param value The integer value
     * @return New integer {@link ITag} with the specified name and value
     */
    public static ITag<Integer> createInt(String name, int value)
    {
        return new Tag_Int(name, value);
    }

    /**
     * Creates an empty double tag.
     *
     * @return New double {@link ITag}
     */
    public static ITag<Double> createDouble()
    {
        return new Tag_Double();
    }

    /**
     * Creates a named double tag.
     *
     * @param name The tag name
     * @return New double {@link ITag} with the specified name
     */
    public static ITag<Double> createDouble(String name)
    {
        return new Tag_Double(name);
    }

    /**
     * Creates a double tag with name and value.
     *
     * @param name The tag name
     * @param value The double value
     * @return New double {@link ITag} with the specified name and value
     */
    public static ITag<Double> createDouble(String name, double value)
    {
        return new Tag_Double(name, value);
    }

    /**
     * Creates an empty float tag.
     *
     * @return New float {@link ITag}
     */
    public static ITag<Float> createFloat()
    {
        return new Tag_Float();
    }

    /**
     * Creates a named float tag.
     *
     * @param name The tag name
     * @return New float {@link ITag} with the specified name
     */
    public static ITag<Float> createFloat(String name)
    {
        return new Tag_Float(name);
    }

    /**
     * Creates a float tag with name and value.
     *
     * @param name The tag name
     * @param value The float value
     * @return New float {@link ITag} with the specified name and value
     */
    public static ITag<Float> createFloat(String name, float value)
    {
        return new Tag_Float(name, value);
    }

    /**
     * Creates an empty byte tag.
     *
     * @return New byte {@link ITag}
     */
    public static ITag<Byte> createByte()
    {
        return new Tag_Byte();
    }

    /**
     * Creates a named byte tag.
     *
     * @param name The tag name
     * @return New byte {@link ITag} with the specified name
     */
    public static ITag<Byte> createByte(String name)
    {
        return new Tag_Byte(name);
    }

    /**
     * Creates a byte tag with name and value.
     *
     * @param name The tag name
     * @param value The byte value
     * @return New byte {@link ITag} with the specified name and value
     */
    public static ITag<Byte> createByte(String name, byte value)
    {
        return new Tag_Byte(name, value);
    }

    /**
     * Creates an empty short tag.
     *
     * @return New short {@link ITag}
     */
    public static ITag<Short> createShort()
    {
        return new Tag_Short();
    }

    /**
     * Creates a named short tag.
     *
     * @param name The tag name
     * @return New short {@link ITag} with the specified name
     */
    public static ITag<Short> createShort(String name)
    {
        return new Tag_Short(name);
    }

    /**
     * Creates a short tag with name and value.
     *
     * @param name The tag name
     * @param value The short value
     * @return New short {@link ITag} with the specified name and value
     */
    public static ITag<Short> createShort(String name, short value)
    {
        return new Tag_Short(name, value);
    }

    /**
     * Creates an empty long tag.
     *
     * @return New long {@link ITag}
     */
    public static ITag<Long> createLong()
    {
        return new Tag_Long();
    }

    /**
     * Creates a named long tag.
     *
     * @param name The tag name
     * @return New long {@link ITag} with the specified name
     */
    public static ITag<Long> createLong(String name)
    {
        return new Tag_Long(name);
    }

    /**
     * Creates a long tag with name and value.
     *
     * @param name The tag name
     * @param value The long value
     * @return New long {@link ITag} with the specified name and value
     */
    public static ITag<Long> createLong(String name, long value)
    {
        return new Tag_Long(name, value);
    }

    /**
     * Creates an empty byte array tag.
     *
     * @return New byte array {@link ITag}
     */
    public static ITag<byte[]> createByteArray()
    {
        return new Tag_Byte_Array();
    }

    /**
     * Creates a named byte array tag.
     *
     * @param name The tag name
     * @return New byte array {@link ITag} with the specified name
     */
    public static ITag<byte[]> createByteArray(String name)
    {
        return new Tag_Byte_Array(name);
    }

    /**
     * Creates a byte array tag with name and value.
     *
     * @param name The tag name
     * @param value The byte array value
     * @return New byte array {@link ITag} with the specified name and value
     */
    public static ITag<byte[]> createByteArray(String name, byte[] value)
    {
        return new Tag_Byte_Array(name, value);
    }

    /**
     * Creates an empty integer array tag.
     *
     * @return New integer array {@link ITag}
     */
    public static ITag<int[]> createIntArray()
    {
        return new Tag_Int_Array();
    }

    /**
     * Creates a named integer array tag.
     *
     * @param name The tag name
     * @return New integer array {@link ITag} with the specified name
     */
    public static ITag<int[]> createIntArray(String name)
    {
        return new Tag_Int_Array(name);
    }

    /**
     * Creates an integer array tag with name and value.
     *
     * @param name The tag name
     * @param value The integer array value
     * @return New integer array {@link ITag} with the specified name and value
     */
    public static ITag<int[]> createIntArray(String name, int[] value)
    {
        return new Tag_Int_Array(name, value);
    }

    /**
     * Creates an empty long array tag.
     *
     * @return New long array {@link ITag}
     */
    public static ITag<long[]> createLongArray()
    {
        return new Tag_Long_Array();
    }

    /**
     * Creates a named long array tag.
     *
     * @param name The tag name
     * @return New long array {@link ITag} with the specified name
     */
    public static ITag<long[]> createLongArray(String name)
    {
        return new Tag_Long_Array(name);
    }

    /**
     * Creates a long array tag with name and value.
     *
     * @param name The tag name
     * @param value The long array value
     * @return New long array {@link ITag} with the specified name and value
     */
    public static ITag<long[]> createLongArray(String name, long[] value)
    {
        return new Tag_Long_Array(name, value);
    }

    /*
     * ========== CONVENIENCE METHODS FOR QUICK CREATION ==========
     */

    /**
     * Creates a simple NBT structure with key-value string pairs.
     * Perfect for configuration files or simple data storage.
     *
     * @param rootName The name of the root compound tag
     * @param keyValuePairs Alternating keys and values (key1, value1, key2, value2, ...)
     * @return A {@link ICompoundTag} containing the key-value pairs
     * @throws IllegalArgumentException If an odd number of arguments is provided
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
}