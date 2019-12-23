package uk.kukino.sgo.base;

public final class Adjacent
{
    /*
         Int = 0123456789abcdef0123456789abcdef
               |centre coord. |        NESW0nnn

        N|E|S|W = bit determining if that position 'exist' in the Adjacents iterator
        nn = iterator position, 0, 1, 2, 3, natural positions; 7 signals end of iterator

        Semantics like this:

        int adjs = Adjacent.asVal(Coord.parseToVal("E5"), (byte) 9);
        while (Adjacent.iterHasNext(adjs))
        {
            short adj = Adjacent.iterPosition(adjs);
            adjs = Adjacent.iterMoveNext(adjs);
        }
     */

    private static final byte ITER_END_VALUE = (byte) 7;
    public static final byte EMPTY_ITERATOR = ITER_END_VALUE;

    private Adjacent()
    {
    }

    private static int build(final short coord, final boolean N, final boolean E, final boolean S, final boolean W, final byte iterPos)
    {
        byte result = 0;
        if (N)
        {
            result |= 0b10000000;
        }
        if (E)
        {
            result |= 0b01000000;
        }
        if (S)
        {
            result |= 0b00100000;
        }
        if (W)
        {
            result |= 0b00010000;
        }
        return (coord << 16) + (result & 0b11110000) + iterPos;
    }

    public static int asVal(short coord, byte size)
    {
        final byte x = Coord.X(coord);
        final byte y = Coord.Y(coord);
        final boolean N = y + 1 < size;
        final boolean E = x + 1 < size;
        final boolean S = y - 1 >= 0;
        final boolean W = x - 1 >= 0;

        byte iterPos = -1;
        if (N)
        {
            iterPos = 0;
        }
        else if (E)
        {
            iterPos = 1;
        }
        else if (S)
        {
            iterPos = 2;
        }
        else if (W)
        {
            iterPos = 3;
        }
        else
        {
            iterPos = ITER_END_VALUE;
        }

        return build(coord, N, E, S, W, iterPos);
    }

    public static String valToStr(final int adjs)
    {
        return "{" + Coord.shortToString(readIterCentre(adjs)) + " " +
            (readIterFlag(adjs, (byte) 0) ? 'N' : '.') +
            (readIterFlag(adjs, (byte) 1) ? 'E' : '.') +
            (readIterFlag(adjs, (byte) 2) ? 'S' : '.') +
            (readIterFlag(adjs, (byte) 3) ? 'W' : '.') +
            " #" + readIterPosition(adjs) +
            "->" + Coord.shortToString(iterPosition(adjs)) + "}";
    }


    // Iterator impl

    private static byte readIterPosition(final int adjs)
    {
        return (byte) (adjs & 0b111);
    }

    private static boolean readIterFlag(final int adjs, final byte flag)
    {
        return (((adjs & 0b11110000) >> (7 - flag)) & 0b1) == 0b1;
    }

    private static short readIterCentre(final int adjs)
    {
        return (short) ((adjs >> 16) & 0xffff);
    }

    public static boolean iterHasNext(final int adjs)
    {
        return readIterPosition(adjs) != ITER_END_VALUE;
    }

    public static int iterMoveNext(final int adjs)
    {
        byte pos = (byte) (readIterPosition(adjs) + 1);
        while (pos < 5 && !readIterFlag(adjs, pos))
        {
            pos++;
        }
        if (pos > 4)
        {
            pos = ITER_END_VALUE;
        }
        return (adjs & 0xffff0000) + (adjs & 0b11110000) + pos;
    }

    public static int iterReset(final int adjs)
    {
        int adjs2 = (adjs & 0xffff0000) + (adjs & 0b11110000);
        if (!readIterFlag(adjs2, (byte) 0))  // first position unset?
        {
            adjs2 = iterMoveNext(adjs2);
        }
        return adjs2;
    }

    public static short iterPosition(final int adjs)
    {
        final byte pos = readIterPosition(adjs);
        final short centre = (short) (adjs >>> 16);

        if (pos == 0)
        {
            return Coord.XY(Coord.X(centre), (byte) (Coord.Y(centre) + 1));
        }
        else if (pos == 1)
        {
            return Coord.XY((byte) (Coord.X(centre) + 1), Coord.Y(centre));

        }
        else if (pos == 2)
        {
            return Coord.XY(Coord.X(centre), (byte) (Coord.Y(centre) - 1));

        }
        else if (pos == 3)
        {
            return Coord.XY((byte) (Coord.X(centre) - 1), Coord.Y(centre));
        }
        return Coord.INVALID;
    }

    public static int iterUnset(final int adjs)
    {
        final byte pos = readIterPosition(adjs);
        final byte flags = (byte) (adjs & 0b11110000 ^ (0b1 << (7 - pos)));
        return (adjs & 0xffff0000) + (flags & 0b11110000) + pos;
    }
}