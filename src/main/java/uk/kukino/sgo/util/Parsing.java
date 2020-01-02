package uk.kukino.sgo.util;

public class Parsing
{

    public static int scanSpaces(final CharSequence seq, final int start)
    {
        int j = start;
        while (j < seq.length() && seq.charAt(j) == ' ')
        {
            j++;
        }
        return j;
    }

    public static int scanNonSpaces(final CharSequence seq, final int start)
    {
        int j = start;
        while (j < seq.length() && seq.charAt(j) != ' ')
        {
            j++;
        }
        return j;
    }

    public static int scanAlphas(final CharSequence seq, final int start)
    {
        int j = start;
        while (j < seq.length() &&
            Character.toUpperCase(seq.charAt(j)) >= 'A' &&
            Character.toUpperCase(seq.charAt(j)) <= 'Z')
        {
            j++;
        }
        return j;
    }

    public static int scanToChar(final CharSequence seq, final char ch, final int start)
    {
        int j = start;
        while (j < seq.length() && ch != seq.charAt(j))
        {
            j++;
        }
        return j;
    }

    public static boolean sameIgnoreCase(final CharSequence left, final CharSequence right)
    {
        if (left == null || right == null || left.length() != right.length())
        {
            return false;
        }

        for (int i = 0; i < left.length(); i++)
        {
            if (Character.toUpperCase(left.charAt(i)) != Character.toUpperCase(right.charAt(i)))
            {
                return false;
            }
        }
        return true;
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

    public static float parseFloat(final CharSequence input, final int start, final int end)
    {
        int j = start;
        long value = 0;
        int divider = 1;
        boolean dotfound = false;

        if (j < end && input.charAt(j) == '-')
        {
            j++;
            divider = -divider;
            if (j == end)
            {
                return Float.NaN;
            }
        }
        while (j < end && ((input.charAt(j) >= '0' && input.charAt(j) <= '9') || input.charAt(j) == '.'))
        {
            if (input.charAt(j) == '.')
            {
                if (dotfound)
                {
                    return Float.NaN; // double dots?!
                }
                dotfound = true;
            }
            else
            {
                value = value * 10 + (input.charAt(j) - '0');
                if (dotfound)
                {
                    divider = divider * 10;
                }
            }

            j++;
        }
        if (j != end || start == end)
        {
            return Float.NaN;
        }
        return (float) value / (float) divider;
    }

    public static float parseFloat(final CharSequence input)
    {
        return parseFloat(input, 0, input.length());
    }
}
