package me.paulferlitz.NBTTags;

import java.util.Arrays;

/**
 * Class representing a NBT byte array tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Byte_Array extends Tag
{
    private final byte[] value;

    /**
     * Create a byte array tag with the value.
     *
     * @param value Byte array value of the tag.
     */
    public Tag_Byte_Array(byte[] value)
    {
        super(7);
        this.value = value;
    }

    /**
     * Create a byte array tag with name and value.
     *
     * @param name Name of the tag.
     * @param value Byte array value of the tag.
     */
    public Tag_Byte_Array(String name, byte[] value)
    {
        super(7, name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public byte[] getValue()
    {
        return value;
    }

    /**
     * Method for generating a formatable string representation of the tag.
     *
     * @return String representatin of tag.
     */
    @Override
    public String toString()
    {
        return "Tag_Byte_Array(\"" + getName() + "\"): " + Arrays.toString(getValue());
    }
}