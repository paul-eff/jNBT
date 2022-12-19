package me.paulferlitz.Helpers;

public class BinaryConverters
{
    public static short bytesToUnsignedShort(byte[] data)
    {
        return (short)((data[1] & 0xff) << 8 | (data[0] & 0xff));
    }
}
