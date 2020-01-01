package uk.kukino.sgo.base;

public enum Color
{
    EMPTY((byte) 0, '.'), BLACK((byte) 1, 'X'), WHITE((byte) 2, 'O'), MARK((byte) 3, '*');

    public final byte b;
    public final char symbol;

    Color(final byte value, final char sym)
    {
        b = value;
        symbol = sym;
    }

    public static Color fromByte(final byte b)
    {
        switch (b)
        {
            case 0:
                return EMPTY;
            case 1:
                return BLACK;
            case 2:
                return WHITE;
            case 3:
                return MARK;
            default:
                throw new IllegalStateException("Unexpected Color byte value: " + b);
        }
    }

    public Color opposite()
    {
        switch (this)
        {
            case WHITE:
                return BLACK;
            case BLACK:
                return WHITE;
            case EMPTY:
                return MARK;
            case MARK:
                return EMPTY;
        }
        throw new IllegalArgumentException("This needs further implementation, missing opposite Color configuration");
    }

    public static Color parse(final CharSequence cs)
    {
        if (cs == null)
        {
            return null;
        }
        if (cs.length() == 1)
        {
            if (Character.toUpperCase(cs.charAt(0)) == 'W')
            {
                return WHITE;
            }

            else if (Character.toUpperCase(cs.charAt(0)) == 'B')
            {
                return BLACK;

            }
        }
        else if (cs.length() == 5)
        {
            if (Character.toUpperCase(cs.charAt(0)) == 'B' &&
                Character.toUpperCase(cs.charAt(1)) == 'L' &&
                Character.toUpperCase(cs.charAt(2)) == 'A' &&
                Character.toUpperCase(cs.charAt(3)) == 'C' &&
                Character.toUpperCase(cs.charAt(4)) == 'K')
            {
                return BLACK;
            }
            if (Character.toUpperCase(cs.charAt(0)) == 'W' &&
                Character.toUpperCase(cs.charAt(1)) == 'H' &&
                Character.toUpperCase(cs.charAt(2)) == 'I' &&
                Character.toUpperCase(cs.charAt(3)) == 'T' &&
                Character.toUpperCase(cs.charAt(4)) == 'E')
            {
                return WHITE;
            }
        }
        return null;
    }

}
