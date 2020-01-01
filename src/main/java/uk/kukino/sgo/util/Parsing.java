package uk.kukino.sgo.util;

public class Parsing
{

    public static int scanSpaces(final CharSequence seq, final int i)
    {
        int j = i;
        while (j < seq.length() && seq.charAt(j) == ' ')
        {
            j++;
        }
        return j;
    }

    public static int scanAlphas(final CharSequence seq, final int i)
    {
        int j = i;
        while (j < seq.length() &&
            Character.toUpperCase(seq.charAt(j)) >= 'A' &&
            Character.toUpperCase(seq.charAt(j)) <= 'Z')
        {
            j++;
        }
        return j;
    }

    public static int parseInteger(final CharSequence input, final int start, final int end)
    {
        int j = start;
        int value = 0;
        boolean negative = false;

        if (j < end && input.charAt(j) == '-')
        {
            j++;
            negative = true;
        }
        while (j < end && input.charAt(j) >= '0' && input.charAt(j) <= '9')
        {
            value = value * 10 + (input.charAt(j) - '0');
            j++;
        }
        if (j != end || start == end)
        {
            return Integer.MIN_VALUE;
        }
        else if (negative)
        {
            return -value;
        }
        else
        {
            return value;
        }
    }

}
