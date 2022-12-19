package me.paulferlitz.NBTTags;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class TAG_String
{
    public static final int ID = 8;
    public static final String NAME = "TAG_String";
    private static int PAYLOAD_BYTE_SIZE = 0;
    private static final int LENGTH_PREFIX_BYTE_LENGTH = 2;
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
        sb.append(NAME);
        sb.append(": ");
        sb.append(readTagPayload(nbtByteArray, currentPosition));

        lengthInBytes = 1 + LENGTH_PREFIX_BYTE_LENGTH + PAYLOAD_BYTE_SIZE;
        sb.append("START:" + currentPosition + " - END:" + (currentPosition + lengthInBytes));
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
    private static String readTagPayload(byte[] nbtByteArray, int currentPosition)
    {
        StringBuilder sb = new StringBuilder();

        byte[] byteArr = new byte[2];
        byteArr[0] = nbtByteArray[currentPosition + 1];
        byteArr[1] = nbtByteArray[currentPosition + 2];
        PAYLOAD_BYTE_SIZE = Short.toUnsignedInt(ByteBuffer.wrap(byteArr).order(ByteOrder.BIG_ENDIAN).getShort());

        for (int i = 0; i < PAYLOAD_BYTE_SIZE; i++)
        {
            sb.append(new String(new byte[]{nbtByteArray[currentPosition + 1 + LENGTH_PREFIX_BYTE_LENGTH + i]}, StandardCharsets.UTF_8));
        }

        return sb.toString();
    }
}
