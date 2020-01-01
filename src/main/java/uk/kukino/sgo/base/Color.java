package uk.kukino.sgo.base;

import uk.kukino.sgo.util.Parsing;

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
            if (Parsing.sameIgnoreCase(cs, "BLACK"))
            {
                return BLACK;
            }
            if (Parsing.sameIgnoreCase(cs, "WHITE"))
            {
                return WHITE;
            }
        }
        return null;
    }

    public void write(final StringBuilder sb, final boolean camelCase)
    {
        if (this == Color.WHITE)
        {
            sb.append(camelCase ? "White" : "WHITE");
        }
        if (this == Color.BLACK)
        {
            sb.append(camelCase ? "Black" : "BLACK");
        }
    }

    public void write(final StringBuilder sb)
    {
        write(sb, true);
    }

}
