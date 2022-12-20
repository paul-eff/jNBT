package me.paulferlitz.NBTTags;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class TAG_List
{
    public static final int ID = 9;
    public static final String NAME = "TAG_List";
    private static final int TYPE_PREFIX_BYTE_LENGTH = 1;
    private static final int LENGTH_PREFIX_BYTE_LENGTH = 4;
    private static int PAYLOAD_TYPE_ID = 0;
    private static int PAYLOAD_BYTE_SIZE = 0;
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
        PAYLOAD_BYTE_SIZE = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(NAME + "(\"");

        byte[] byteArr = new byte[2];
        byteArr[0] = nbtByteArray[currentPosition + 1];
        byteArr[1] = nbtByteArray[currentPosition + 2];
        int nameLength = ByteBuffer.wrap(byteArr).order(ByteOrder.BIG_ENDIAN).getShort();
        sb.append(readTagName(nbtByteArray, currentPosition, nameLength) + "\"):\n");
        sb.append(readTagPayload(nbtByteArray, currentPosition, nameLength));

        lengthInBytes = 1 + NAME_BYTE_LENGTH + nameLength + TYPE_PREFIX_BYTE_LENGTH + LENGTH_PREFIX_BYTE_LENGTH + PAYLOAD_BYTE_SIZE;
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
    private static String readTagPayload(byte[] nbtByteArray, int currentPosition, int nameLength)
    {
        StringBuilder sb = new StringBuilder();

        byte[] byteArr = new byte[4];
        byteArr[3] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength];
        PAYLOAD_TYPE_ID = ByteBuffer.wrap(byteArr).order(ByteOrder.BIG_ENDIAN).getInt();

        byteArr[0] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 1];
        byteArr[1] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 2];
        byteArr[2] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 3];
        byteArr[3] = nbtByteArray[currentPosition + 1 + NAME_BYTE_LENGTH + nameLength + 4];
        int tagListSize = ByteBuffer.wrap(byteArr).order(ByteOrder.BIG_ENDIAN).getInt();

        if (PAYLOAD_TYPE_ID == 8)
        {
            for (int i = 0; i < tagListSize; i++)
            {
                int offset = NAME_BYTE_LENGTH + nameLength + TYPE_PREFIX_BYTE_LENGTH + LENGTH_PREFIX_BYTE_LENGTH + PAYLOAD_BYTE_SIZE;
                sb.append(TAG_String.readWholeTag(nbtByteArray, currentPosition + offset));
                if (i < tagListSize - 1) sb.append(",\n");
                PAYLOAD_BYTE_SIZE += (TAG_String.getLastTagsLength() - 1);
            }
        } else
        {

        }
        return sb.toString();
    }
}
