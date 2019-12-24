package uk.kukino.sgo.sgf;

public enum Rule
{
    AGA, GOE, Japanese, Chinese;

    public static Rule fromCs(final CharSequence cs)
    {
        if (cs.length() < 1)
        {
            return null;
        }
        final char fl = cs.charAt(0);
        if (fl == 'J')
        {
            return Japanese;
        }
        else if (fl == 'C')
        {
            return Chinese;
        }
        else if (fl == 'G')
        {
            return GOE;
        }
        else if (fl == 'A')
        {
            return AGA;
        }
        return null;

    }
}
