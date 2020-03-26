package uk.kukino.sgo.base;

import uk.kukino.sgo.util.Parsing;

public final class Coord
{

    // short = 0123456789abcdef
    //         CCXXXXXXXYYYYYYY = General format
    //         1111111111111111 = Invalid
    //         CC00000000000000 = Pass

    public static final short INVALID = 0xffffffff;

    public static final short[] HIGHLIGHTS_9X9 = new short[]
        {
            parseToVal("C7"), parseToVal("E7"), parseToVal("G7"),
            parseToVal("C5"), parseToVal("E5"), parseToVal("G5"),
            parseToVal("C3"), parseToVal("E3"), parseToVal("G3")
        };

    public static final short[] HIGHLIGHTS_13X13 = new short[]
        {
            parseToVal("D10"), parseToVal("G10"), parseToVal("K10"),
            parseToVal("D7"), parseToVal("G7"), parseToVal("K7"),
            parseToVal("D4"), parseToVal("G4"), parseToVal("K4")
        };

    public static final short[] HIGHLIGHTS_19X19 = new short[]
        {
            parseToVal("D16"), parseToVal("K16"), parseToVal("Q16"),
            parseToVal("D10"), parseToVal("K10"), parseToVal("Q10"),
            parseToVal("D4"), parseToVal("K4"), parseToVal("Q4")
        };

    public static final int NW = 0;
    public static final int N = 1;
    public static final int NE = 2;
    public static final int W = 3;
    public static final int C = 4;
    public static final int E = 5;
    public static final int SW = 6;
    public static final int S = 7;
    public static final int SE = 8;


    public static final short A1 = parseToVal("A1");
    public static final short A2 = parseToVal("A2");
    public static final short A3 = parseToVal("A3");
    public static final short A4 = parseToVal("A4");
    public static final short B1 = parseToVal("B1");
    public static final short B2 = parseToVal("B2");
    public static final short B3 = parseToVal("B3");
    public static final short B4 = parseToVal("B4");
    public static final short C1 = parseToVal("C1");
    public static final short C2 = parseToVal("C2");
    public static final short C3 = parseToVal("C3");
    public static final short C4 = parseToVal("C4");
    public static final short D1 = parseToVal("D1");
    public static final short D2 = parseToVal("D2");
    public static final short D3 = parseToVal("D3");
    public static final short D4 = parseToVal("D4");

    private Coord()
    {
    }

    public static String shortToString(final short value)
    {
        final StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    public static void write(final StringBuilder sb, final short value)
    {
        final byte x = Coord.X(value);
        final byte y = Coord.Y(value);

        final char symb = (x <= 'I' - 'A' - 1) ? (char) (65 + x) : (char) (65 + 1 + x);
        final int yToUse = y + 1; //FIXME: size - y;

        sb.append(symb).append(yToUse);
    }

    public static short parseToVal(final CharSequence seq)
    {
        if (seq == null)
        {
            return INVALID;
        }
        int i = Parsing.scanSpaces(seq, 0);
        if (i == seq.length())
        {
            return INVALID;
        }
        final int j = Parsing.scanAlphas(seq, i);
        if (i == j || i + 2 < j)
        {
            return INVALID;
        }

        final byte x;
        byte y;
        byte a = (byte) (Character.toUpperCase(seq.charAt(i)) - 64);
        if (a > 'I' - 65)
        {
            a--;
        }
        if (i + 1 == j)
        {
            x = a;
        }
        else
        {
            byte b = (byte) (Character.toUpperCase(seq.charAt(j - 1)) - 64);
            if (b > 'I' - 65)
            {
                b--;
            }
            x = (byte) ((a * (byte) 26) + b);
        }

        y = 0;
        i = j;
        while (i < seq.length() && seq.charAt(i) >= '0' && seq.charAt(i) <= '9')
        {
            y = (byte) ((y * 10) + (seq.charAt(i) - '0'));
            i++;
            if (i - j > 3)
            {
                return INVALID;
            }
        }
        if (i == j)
        {
            return INVALID;
        }

        return XY((byte) (x - 1), (byte) (y - 1));
    }


    private static byte mirror(final byte value, final byte size)
    {
        return (byte) Math.abs(size - value - 1);
    }

    public static int allRotationsAndReflections(final short[] buffer, final short coord, final byte boardSize)
    {
        if (buffer.length < 8)
        {
            throw new IllegalArgumentException("Please provide an array of at least size 8.");
        }
        final byte centre = (byte) (boardSize >> 1);
        final byte x = X(coord);
        final byte y = Y(coord);
        if (centre == x && centre == y)
        {
            buffer[0] = coord;
            buffer[1] = Coord.INVALID;
            return 1;
        }

        buffer[0] = XY(x, y);
        buffer[1] = XY(mirror(x, boardSize), y);
        buffer[2] = XY(mirror(x, boardSize), mirror(y, boardSize));
        buffer[3] = XY(x, mirror(y, boardSize));
        if (Math.abs(centre - x) == Math.abs(centre - y))
        {
            buffer[4] = Coord.INVALID;
            return 4;
        }

        final byte rx = y;
        final byte ry = x;
        buffer[4] = XY(rx, ry);
        buffer[5] = XY(mirror(rx, boardSize), ry);
        buffer[6] = XY(mirror(rx, boardSize), mirror(ry, boardSize));
        buffer[7] = XY(rx, mirror(ry, boardSize));
        if (buffer.length > 8)
        {
            buffer[8] = Coord.INVALID;
        }
        return 8;
    }

    public static short XY(final byte x, final byte y)
    {
        return (short) ((short) ((x & 0x7f) << 7) | (short) (y & 0x7f));
    }

    public static byte X(final short val)
    {
        return (byte) ((val >> 7) & 0x7f);
    }

    public static byte Y(final short val)
    {
        return (byte) (val & 0x7f);
    }

    public static int linealOffset(final short value, final byte boardSize)
    {
        return Y(value) * boardSize + X(value);
    }

    public static boolean isValid(final short val)
    {
        return val != INVALID;
    }

}
