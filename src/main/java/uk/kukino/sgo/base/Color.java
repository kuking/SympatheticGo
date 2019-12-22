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

}
