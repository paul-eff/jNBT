package me.paulferlitz.NBTTags;

import me.paulferlitz.Constants;

import java.util.Arrays;

/**
 * Class representing a NBT int array tag.
 *
 * @author Paul Ferlitz
 */
public class Tag_Int_Array extends Tag
{
    private final int[] value;

    /**
     * Create a int array tag with the value.
     *
     * @param value Int array value of the tag.
     */
    public Tag_Int_Array(int[] value)
    {
        super(Constants.NBTTags.Tag_Int_Array.getId());
        this.value = value;
    }

    /**
     * Create a int array tag with name and value.
     *
     * @param name  Name of the tag.
     * @param value Int array value of the tag.
     */
    public Tag_Int_Array(String name, int[] value)
    {
        super(Constants.NBTTags.Tag_Int_Array.getId(), name);
        this.value = value;
    }

    /**
     * Method returning the tag's value.
     *
     * @return The value/playload of the tag.
     */
    @Override
    public int[] getValue()
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
        return Constants.NBTTags.Tag_Int_Array.getName() + "(\"" + getName() + "\"): " + Arrays.toString(getValue());
    }
}
