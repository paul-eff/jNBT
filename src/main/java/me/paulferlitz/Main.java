package me.paulferlitz;

import me.paulferlitz.Helpers.FileHelper;
import me.paulferlitz.NBTTags.*;

import java.io.File;
import java.io.IOException;

public class Main
{
    private final static File nbtFile = new File("./src/main/resources/playerTestData.dat");
    private final static int NAME_LENGTH_BYTES = 2;

    public static void main(String[] args)
    {
        System.out.println("Loading jNBT...");

        try
        {
            byte[] nbtFileByteArray = FileHelper.loadNBTFileToBytes(nbtFile);
            //String s = new String(nbtFileByteArray, StandardCharsets.UTF_8);

            int byteArrayCounter = 0;

            while (byteArrayCounter < nbtFileByteArray.length)
            {
                byte currPosByte = nbtFileByteArray[byteArrayCounter];

                switch (currPosByte)
                {
                    case TAG_End.ID:
                        System.out.println(TAG_End.readWholeTag());
                        byteArrayCounter += TAG_End.getLastTagsLength();
                        continue;
                    case TAG_Byte.ID:
                        System.out.println(TAG_Byte.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Byte.getLastTagsLength();
                        continue;
                    case TAG_Short.ID:
                        System.out.println(TAG_Short.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Short.getLastTagsLength();
                        continue;
                    case TAG_Int.ID:
                        System.out.println(TAG_Int.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Int.getLastTagsLength();
                        continue;
                    case TAG_Long.ID:
                        System.out.println(TAG_Long.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Long.getLastTagsLength();
                        continue;
                    case TAG_Float.ID:
                        System.out.println(TAG_Float.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Float.getLastTagsLength();
                        continue;
                    case TAG_Double.ID:
                        System.out.println(TAG_Double.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Double.getLastTagsLength();
                        continue;
                    case TAG_Byte_Array.ID:
                        System.out.println(TAG_Byte_Array.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Byte_Array.getLastTagsLength();
                        continue;
                    case TAG_String.ID:
                        System.out.println(TAG_String.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_String.getLastTagsLength();
                        continue;
                    case TAG_List.ID:
                        System.out.println(TAG_List.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_List.getLastTagsLength();
                        System.out.println("List End Position " + byteArrayCounter);
                        continue;
                    case TAG_Compound.ID:
                        System.out.println(TAG_Compound.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Compound.getLastTagsLength();
                        continue;
                    /*case TAG_Int_Array.ID:
                        System.out.println(TAG_Int_Array.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Int_Array.getLastTagsLength();
                        continue;
                    case TAG_Long_Array.ID:
                        System.out.println(TAG_Long_Array.readWholeTag(nbtFileByteArray, byteArrayCounter));
                        byteArrayCounter += TAG_Long_Array.getLastTagsLength();
                        continue;*/
                    default:
                        System.out.println(currPosByte);
                        byteArrayCounter++;
                }
            }
            System.out.println("END");
            //String hexByte = String.format("%02x", b);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}