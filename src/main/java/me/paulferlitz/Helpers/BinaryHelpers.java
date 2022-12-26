package me.paulferlitz.Helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BinaryHelpers
{
    public static ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    /**
     * Method to read the next signed byte in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The signed byte which was read.
     */
    public static byte readNextByte(byte[] byteArray, int pointer)
    {
        return byteArray[pointer];
    }

    /**
     * Method to read the next unsigned byte in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The unsigned byte which was read as an int.
     */
    public static int readNextUnsignedByte(byte[] byteArray, int pointer)
    {
        return Byte.toUnsignedInt(byteArray[pointer]);
    }

    /**
     * Method to read the next signed short in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The signed short which was read.
     */
    public static short readNextShort(byte[] byteArray, int pointer)
    {
        byte[] byteStorage = new byte[2];
        byteStorage[0] = byteArray[pointer];
        byteStorage[1] = byteArray[pointer + 1];

        return ByteBuffer.wrap(byteStorage).order(byteOrder).getShort();
    }

    /**
     * Method to read the next unsigned short in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The unsigned short which was read as an int.
     */
    public static int readNextUnsignedShort(byte[] byteArray, int pointer)
    {
        byte[] byteStorage = new byte[2];
        byteStorage[0] = byteArray[pointer];
        byteStorage[1] = byteArray[pointer + 1];

        return Short.toUnsignedInt(ByteBuffer.wrap(byteStorage).order(byteOrder).getShort());
    }

    /**
     * Method to read the next signed int in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The signed int which was read.
     */
    public static int readNextInt(byte[] byteArray, int pointer)
    {
        byte[] byteStorage = new byte[4];
        byteStorage[0] = byteArray[pointer];
        byteStorage[1] = byteArray[pointer + 1];
        byteStorage[2] = byteArray[pointer + 2];
        byteStorage[3] = byteArray[pointer + 3];

        return ByteBuffer.wrap(byteStorage).order(byteOrder).getInt();
    }

    /**
     * Method to read the next unsigned int in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The unsigned int which was read as a long.
     */
    public static long readNextUnsignedInt(byte[] byteArray, int pointer)
    {
        byte[] byteStorage = new byte[4];
        byteStorage[0] = byteArray[pointer];
        byteStorage[1] = byteArray[pointer + 1];
        byteStorage[2] = byteArray[pointer + 2];
        byteStorage[3] = byteArray[pointer + 3];

        return Integer.toUnsignedLong(ByteBuffer.wrap(byteStorage).order(byteOrder).getInt());
    }

    /**
     * Method to read the next signed long in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The signed long which was read.
     */
    public static long readNextLong(byte[] byteArray, int pointer)
    {
        byte[] byteStorage = new byte[8];
        byteStorage[0] = byteArray[pointer];
        byteStorage[1] = byteArray[pointer + 1];
        byteStorage[2] = byteArray[pointer + 2];
        byteStorage[3] = byteArray[pointer + 3];
        byteStorage[4] = byteArray[pointer + 4];
        byteStorage[5] = byteArray[pointer + 5];
        byteStorage[6] = byteArray[pointer + 6];
        byteStorage[7] = byteArray[pointer + 7];

        return ByteBuffer.wrap(byteStorage).order(byteOrder).getLong();
    }

    /**
     * Method to read the next signed float in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The signed float which was read.
     */
    public static float readNextFloat(byte[] byteArray, int pointer)
    {
        byte[] byteStorage = new byte[4];
        byteStorage[0] = byteArray[pointer];
        byteStorage[1] = byteArray[pointer + 1];
        byteStorage[2] = byteArray[pointer + 2];
        byteStorage[3] = byteArray[pointer + 3];

        return ByteBuffer.wrap(byteStorage).order(byteOrder).getFloat();
    }

    /**
     * Method to read the next signed double in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @return The signed double which was read.
     */
    public static double readNextDouble(byte[] byteArray, int pointer)
    {
        byte[] byteStorage = new byte[8];
        byteStorage[0] = byteArray[pointer];
        byteStorage[1] = byteArray[pointer + 1];
        byteStorage[2] = byteArray[pointer + 2];
        byteStorage[3] = byteArray[pointer + 3];
        byteStorage[4] = byteArray[pointer + 4];
        byteStorage[5] = byteArray[pointer + 5];
        byteStorage[6] = byteArray[pointer + 6];
        byteStorage[7] = byteArray[pointer + 7];

        return ByteBuffer.wrap(byteStorage).order(byteOrder).getDouble();
    }

    /**
     * Method to read the next string in the given data array.
     * It is assumed that the pointer is pointing at the first byte of the next value to be read.
     *
     * @param byteArray Byte array holding data.
     * @param pointer Pointer to the current position in the data array.
     * @param stringLength Number of characters in the to read string.
     * @return The final string which was read.
     */
    public static String readNextString(byte[] byteArray, int pointer, int stringLength)
    {
        String finalString = new String(Arrays.copyOfRange(byteArray, pointer, pointer + stringLength), StandardCharsets.UTF_8);

        return finalString.equals("") ? "null" : finalString;
    }
}
