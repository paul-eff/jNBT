package me.paulferlitz.NBTTags;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class TAG_Float
{
    public static final int ID = 5;
    public static final String NAME = "TAG_Float";
    private static final int PAYLOAD_BYTE_SIZE = 4;
    private static final int NAME_BYTE_LENGTH = 2;
    private static int lengthInBytes = -1;

    /**
     * Method assumes the currentPosition pointer is pointing at the current tag ID.
     *
     * @param nbtByteArray
     * @param currentPosition
     * @return
     */
    public static String readWholeTag(byte[] nbtByteArray, int currentPosition)
    {
        lengthInBytes = -1;
        StringBuilder sb = new StringBuilder();
        sb.append(NAME + "(\"");

        byte[] byteArr = new byte[2];
        byteArr[0] = nbtByteArray[currentPosition + 1];
        byteArr[1] = nbtByteArray[currentPosition + 2];
        int nameLength = ByteBuffer.wrap(byteArr).order(ByteOrder.BIG_ENDIAN).getShort();

        sb.append(readTagName(nbtByteArray, currentPosition, nameLength) + "\"): ");
        sb.append(readTagPayload(nbtByteArray, currentPosition, nameLength));

        lengthInBytes = 1 + NAME_BYTE_LENGTH + nameLength + PAYLOAD_BYTE_SIZE;
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

    /**
     * Method assumes the currentPosition pointer is pointing at the current tag ID.
     *
     * @param nbtByteArray
     * @param currentPosition
     * @return
     */
    private static String readTagName(byte[] nbtByteArray, int currentPosition, int nameLength)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < nameLength; i++)
        {
            sb.append(new String(new byte[]{nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + i]}, StandardCharsets.UTF_8));
        }

        return sb.toString().equals("") ? "null" : sb.toString();
    }

    /**
     * Method assumes the currentPosition pointer is pointing at the current tag ID.
     *
     * @param nbtByteArray
     * @param currentPosition
     * @return
     */
    private static float readTagPayload(byte[] nbtByteArray, int currentPosition, int nameLength)
    {
        byte[] byteArr = new byte[4];
        byteArr[0] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 1];
        byteArr[1] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 2];
        byteArr[2] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 3];
        byteArr[3] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 4];
        return ByteBuffer.wrap(byteArr).order(ByteOrder.BIG_ENDIAN).getFloat();
    }
}
