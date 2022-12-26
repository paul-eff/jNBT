package me.paulferlitz;

import me.paulferlitz.Helpers.BinaryHelpers;
import me.paulferlitz.Helpers.FileHelper;
import me.paulferlitz.NBTTags.*;

import java.io.File;
import java.io.IOException;

public class Main
{
    private final static File nbtFile = new File("./src/main/resources/playerTestData.dat");

    public static void main(String[] args)
    {
        System.out.println("Loading jNBT...");

        try
        {
            byte[] nbtByteArray = FileHelper.loadNBTFileToBytes(nbtFile);
            //String s = new String(nbtFileByteArray, StandardCharsets.UTF_8);

            int mainPointer = 0;
            StringBuilder toBePrinted;

            while (mainPointer < nbtByteArray.length)
            {
                toBePrinted = new StringBuilder();
                byte currTagID = BinaryHelpers.readNextByte(nbtByteArray, mainPointer++);
                int nameLength = 0;
                int stringLength = 0;

                switch (currTagID)
                {
                    case TAG_End.ID:
                        System.out.println("Tag_END");
                        mainPointer++;
                        break;
                    case TAG_Byte.ID:
                        toBePrinted.append("Tag_Byte");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        toBePrinted.append(": " + BinaryHelpers.readNextByte(nbtByteArray, mainPointer));
                        mainPointer += 1;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Short.ID:
                        toBePrinted.append("Tag_Short");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        toBePrinted.append(": " + BinaryHelpers.readNextShort(nbtByteArray, mainPointer));
                        mainPointer += 2;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Int.ID:
                        toBePrinted.append("Tag_Int");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        toBePrinted.append(": " + BinaryHelpers.readNextInt(nbtByteArray, mainPointer));
                        mainPointer += 4;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Long.ID:
                        toBePrinted.append("Tag_Long");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        toBePrinted.append(": " + BinaryHelpers.readNextLong(nbtByteArray, mainPointer));
                        mainPointer += 8;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Float.ID:
                        toBePrinted.append("Tag_Float");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        toBePrinted.append(": " + BinaryHelpers.readNextFloat(nbtByteArray, mainPointer));
                        mainPointer += 4;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Double.ID:
                        toBePrinted.append("Tag_Double");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        toBePrinted.append(": " + BinaryHelpers.readNextDouble(nbtByteArray, mainPointer));
                        mainPointer += 8;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Byte_Array.ID:
                        System.out.println(TAG_Byte_Array.readWholeTag(nbtByteArray, mainPointer));
                        mainPointer += TAG_Byte_Array.getLastTagsLength();
                        break;
                    case TAG_String.ID:
                        toBePrinted.append("Tag_String");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        stringLength = BinaryHelpers.readNextUnsignedShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append(": " + BinaryHelpers.readNextString(nbtByteArray, mainPointer, stringLength));
                        mainPointer += stringLength;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_List.ID:
                        toBePrinted.append("Tag_List");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\"):\n");
                        mainPointer += nameLength;
                        int listTagType = BinaryHelpers.readNextByte(nbtByteArray, mainPointer++);
                        int listLength = BinaryHelpers.readNextInt(nbtByteArray, mainPointer);
                        mainPointer += 4;

                        if (listLength <= 0) System.out.println("Encountered list length 0. This is not supported yet!");

                        for (int i = 0; i < listLength; i++)
                        {
                            switch (listTagType)
                            {
                                case 0:
                                    System.out.println("Encountered Tag_End in List. This is not supported yet!");
                                    break;
                                case 1:
                                    toBePrinted.append("\tTag_Byte: " + BinaryHelpers.readNextByte(nbtByteArray, mainPointer++));
                                    break;
                                case 2:
                                    toBePrinted.append("\tTag_Short: " + BinaryHelpers.readNextShort(nbtByteArray, mainPointer));
                                    mainPointer += 2;
                                    break;
                                case 3:
                                    toBePrinted.append("\tTag_Int: " + BinaryHelpers.readNextInt(nbtByteArray, mainPointer));
                                    mainPointer += 4;
                                    break;
                                case 4:
                                    toBePrinted.append("\tTag_Long: " + BinaryHelpers.readNextLong(nbtByteArray, mainPointer));
                                    mainPointer += 8;
                                    break;
                                case 5:
                                    toBePrinted.append("\tTag_Float: " + BinaryHelpers.readNextFloat(nbtByteArray, mainPointer));
                                    mainPointer += 4;
                                    break;
                                case 6:
                                    toBePrinted.append("\tTag_Double: " + BinaryHelpers.readNextDouble(nbtByteArray, mainPointer));
                                    mainPointer += 8;
                                    break;
                                case 7:
                                case 11:
                                case 12:
                                    System.out.println("Encountered Array in List. This is not supported yet!");
                                    break;
                                case 8:
                                    toBePrinted.append("\tTag_String: ");
                                    stringLength = BinaryHelpers.readNextUnsignedShort(nbtByteArray, mainPointer);
                                    mainPointer += 2;
                                    toBePrinted.append("\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, stringLength) + "\"");
                                    mainPointer += stringLength;
                                    break;
                                case 9:
                                    System.out.println("Encountered List in List. This is not supported yet!");
                                    break;
                                case 10:
                                    System.out.println("Encountered Tag_Compound in List. This is not supported yet!");
                                    break;
                                default:
                                    System.out.println("Error in List at " + mainPointer);
                                    break;
                            }
                            if (i < listLength - 1)
                            {
                                toBePrinted.append(",\n");
                            }
                        }
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Compound.ID:
                        toBePrinted.append("Tag_Compound");
                        nameLength = BinaryHelpers.readNextShort(nbtByteArray, mainPointer);
                        mainPointer += 2;
                        toBePrinted.append("(\"" + BinaryHelpers.readNextString(nbtByteArray, mainPointer, nameLength) + "\")");
                        mainPointer += nameLength;
                        System.out.println(toBePrinted);
                        break;
                    case TAG_Int_Array.ID:
                        System.out.println(TAG_Int_Array.readWholeTag(nbtByteArray, mainPointer));
                        mainPointer += TAG_Int_Array.getLastTagsLength();
                        break;
                    case TAG_Long_Array.ID:
                        System.out.println(TAG_Long_Array.readWholeTag(nbtByteArray, mainPointer));
                        mainPointer += TAG_Long_Array.getLastTagsLength();
                        break;
                    default:
                        System.out.println("THERE WAS AN ERROR WHILST READING POINTER AT " + mainPointer + ". VALUE READ IS NOT A VALID ID!");
                        mainPointer = nbtByteArray.length;
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