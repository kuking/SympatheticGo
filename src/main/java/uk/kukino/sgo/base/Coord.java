package uk.kukino.sgo.base;

import net.openhft.chronicle.bytes.Bytes;
import uk.kukino.sgo.util.Parsing;

import java.nio.ByteBuffer;

public final class Coord
{

    // short = 0123456789abcdef
    //         CCXXXXXXXYYYYYYY = General format
    //         1111111111111111 = Invalid
    //         CC00000000000000 = Pass

    public static final short INVALID = 0xffffffff;

    private Coord()
    {
    }

    public static String shortToString(final short value)
    {
        final byte x = Coord.X(value);
        final byte y = Coord.Y(value);

        final char symb = (x <= 'I' - 'A' - 1) ? (char) (65 + x) : (char) (65 + 1 + x);
        final int yToUse = y + 1; //FIXME: size - y;

        return String.valueOf(symb) + yToUse;
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

    /***
     * Adjacent cells are returned in wall clock order, if the coordinate does not have four adjacent, it will return
     * the number of adjacent interceptions, and the array will be filled up to four elements of invalid coordinates.
     * @param result
     * @param coord
     * @param boardSize
     * @return
     */
    @Deprecated
    public static byte adjacents(final short[] result, final short coord, final byte boardSize)
    {
        int c = 0;
        final byte x = Coord.X(coord);
        final byte y = Coord.Y(coord);
        if (y - 1 >= 0)
        {
            result[c++] = Coord.XY(x, (byte) (y - 1));
        }
        if (x + 1 < boardSize)
        {
            result[c++] = Coord.XY((byte) (x + 1), y);
        }
        if (y + 1 < boardSize)
        {
            result[c++] = Coord.XY(x, (byte) (y + 1));
        }
        if (x - 1 >= 0)
        {
            result[c++] = Coord.XY((byte) (x - 1), y);
        }
        return (byte) c;
    }


    public static void adjacents(final Bytes<ByteBuffer> result, final short coord, final byte boardSize)
    {
        result.clear();
        final byte x = Coord.X(coord);
        final byte y = Coord.Y(coord);
        if (y - 1 >= 0)
        {
            result.writeShort(Coord.XY(x, (byte) (y - 1)));
        }
        if (x + 1 < boardSize)
        {
            result.writeShort(Coord.XY((byte) (x + 1), y));
        }
        if (y + 1 < boardSize)
        {
            result.writeShort(Coord.XY(x, (byte) (y + 1)));
        }
        if (x - 1 >= 0)
        {
            result.writeShort(Coord.XY((byte) (x - 1), y));
        }
    }

}
