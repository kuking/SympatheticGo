package uk.kukino.sgo;

public class Move
{

    // short = 0123456789abcdef
    //         CCXXXXXXXYYYYYYY = General format
    //         1111111111111111 = Invalid
    //         CC00000000000000 = Pass

    static final short INVALID = 0xffffffff;

    private short value;

    public boolean parse(final CharSequence sequence)
    {
        value = Move.parseToVal(sequence);
        return isValid(value);
    }

    public static short parseToVal(final CharSequence seq)
    {
        short coord = Coord.INVALID;
        Color color = Color.EMPTY;

        if (seq == null)
        {
            return INVALID;
        }
        int i = Parsing.scanSpaces(seq, 0);
        if (i == seq.length())
        {
            return INVALID;
        }

        int j = Parsing.scanAlphas(seq, i);
        if (i == j)
        {
            return INVALID;
        }

        if (j - i == 1)
        {
            char ch = Character.toUpperCase(seq.charAt(i));
            if (ch == 'B')
            {
                color = Color.BLACK;
            } else if (ch == 'W')
            {
                color = Color.WHITE;
            } else
            {
                return INVALID;
            }
        } else if (j - i == 5)
        {
            char a = Character.toUpperCase(seq.charAt(i)); // not weird, on purpose to to avoid memory allocation i.e. char[]
            char b = Character.toUpperCase(seq.charAt(i + 1));
            char c = Character.toUpperCase(seq.charAt(i + 2));
            char d = Character.toUpperCase(seq.charAt(i + 3));
            char e = Character.toUpperCase(seq.charAt(i + 4));
            if (a == 'B' && b == 'L' && c == 'A' && d == 'C' && e == 'K')
            {
                color = Color.BLACK;
            } else if (a == 'W' && b == 'H' && c == 'I' && d == 'T' && e == 'E')
            {
                color = Color.WHITE;
            }
        } else
        {
            return INVALID;
        }

        i = Parsing.scanSpaces(seq, j + 1);
        if (i < seq.length())
        {
            coord = Coord.parseToVal(seq.subSequence(i, seq.length()));
        }

        if (!Coord.isValid(coord))
        { // PASS?
            j = Parsing.scanAlphas(seq, i);
            if (j - 4 == i)
            {
                char a = Character.toUpperCase(seq.charAt(i)); // not weird, on purpose to to avoid memory allocation i.e. char[]
                char b = Character.toUpperCase(seq.charAt(i + 1));
                char c = Character.toUpperCase(seq.charAt(i + 2));
                char d = Character.toUpperCase(seq.charAt(i + 3));
                if (a == 'P' && b == 'A' && c == 'S' && d == 'S')
                {
                    return Move.pass(color);
                }
            }
        }

        if (color == Color.EMPTY || !Coord.isValid(coord))
        {
            return INVALID;
        }

        return Move.move(coord, color);
    }

    public static short move(final short coord, final Color color)
    {
        return (short) ((short) ((color.b) << 14) | (short) (coord & (short) 0b11111111111111));
    }

    public static short move(final byte x, final byte y, final Color color)
    {
        return (short) ((short) ((color.b) << 14) | (short) ((x & 0x7f) << 7) | (short) (y & 0x7f));
    }

    public static short pass(final Color color)
    {
        return move((byte) 0, (byte) 0, color);
    }

    public static byte x(final short value)
    {
        return (byte) ((value >> 7) & 0x7f);
    }

    public byte x()
    {
        return Move.x(value);
    }

    public static byte y(final short value)
    {
        return (byte) (value & 0x7f);
    }

    public byte y()
    {
        return Move.y(value);
    }

    static public Color color(final short value)
    {
        return Color.fromByte((byte) (value >> 14 & 3));
    }

    public Color color()
    {
        return Move.color(value);
    }

    static public boolean isPass(short value)
    {
        return Move.x(value) == (short) 0 &&
                Move.y(value) == (short) 0 &&
                color(value) != Color.EMPTY;
    }

    public boolean isPass()
    {
        return Move.isPass(value);
    }

    public static boolean isValid(final short value)
    {
        return value != INVALID;
    }

    public boolean isValid()
    {
        return Move.isValid(value);
    }

    public static boolean isStone(final short value)
    {
        return isValid(value) && !isPass(value);
    }

    public boolean isStone()
    {
        return isStone(value);
    }

}
