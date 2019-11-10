package uk.kukino.sgo;

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
        while (i < seq.length() &&
            Character.toUpperCase(seq.charAt(j)) >= 'A' &&
            Character.toUpperCase(seq.charAt(j)) <= 'Z')
        {
            j++;
        }
        return j;
    }
}
