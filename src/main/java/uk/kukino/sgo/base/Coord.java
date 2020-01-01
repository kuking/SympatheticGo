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

    public static boolean isValid(final short val)
    {
        return val != INVALID;
    }

}
