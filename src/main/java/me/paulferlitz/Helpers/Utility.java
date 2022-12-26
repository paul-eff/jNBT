package me.paulferlitz.Helpers;

public class Utility
{
    public static String leftFill(String source, int amount, char fill)
    {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < amount; i++)
        {
            sb.append(fill);
        }
        sb.append(source);

        return sb.toString();
    }


}
