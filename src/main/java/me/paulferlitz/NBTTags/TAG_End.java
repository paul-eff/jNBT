package me.paulferlitz.NBTTags;

public class TAG_End
{
    public static final int ID = 0;
    public static final String NAME = "TAG_End";
    private static int lengthInBytes = 1;

    public static String readWholeTag()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NAME);

        return sb.toString();
    }

    /**
     * Method to get the amount of bytes in the last read TAG_Byte tag.
     *
     * @return
     */
    public static int getLastTagsLength()
    {
        return lengthInBytes;
    }
}
