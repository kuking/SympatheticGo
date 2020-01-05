package uk.kukino.sgo.base;

import java.util.Arrays;

public class Board
{
    private final byte size;
    private final int boardBits;
    private final int boardSize;
    private byte[] board;

    // 2 bits per intersection i.e. 19x19 ~= 90 bytes, 9x9 ~= 20 bytes -- everything fits well in L1

    public Board(final byte size)
    {
        this.size = size;
        boardBits = this.size * this.size * 2;
        boardSize = (boardBits / 8) + ((boardBits % 8 == 0) ? 0 : 1);
        board = new byte[boardSize];
        clear();
    }

    private byte setColorByteOffset(final byte origByte, final byte ofs, final Color color)
    {
        switch (ofs)
        {
            case 0:
                return (byte) ((origByte & (byte) 0b00111111) | (color.b << 6));
            case 1:
                return (byte) ((origByte & (byte) 0b11001111) | (color.b << 4));
            case 2:
                return (byte) ((origByte & (byte) 0b11110011) | (color.b << 2));
            case 3:
                return (byte) ((origByte & (byte) 0b11111100) | color.b);
            default:
                return 0;
        }
    }

    public void set(final byte x, final byte y, final Color color)
    {
        final int ofs = ofs(x, y);
        board[ofs >> 2] = setColorByteOffset(board[ofs >> 2], (byte) (ofs & 0b11), color);
    }

    public void set(final short value)
    {
        if (Move.isStone(value))
        {
            set(Move.X(value), Move.Y(value), Move.color(value));
        }
    }

    public void set(final short move, final Color color)
    {
        set(Coord.X(move), Coord.Y(move), color);
    }

    private Color getColorByteOffset(final byte fullByte, final byte ofs)
    {
        switch (ofs)
        {
            case 0:
                return Color.fromByte((byte) (fullByte >> 6 & 0b11));
            case 1:
                return Color.fromByte((byte) ((fullByte & 0b00110000) >>> 4));
            case 2:
                return Color.fromByte((byte) ((fullByte & 0b00001100) >>> 2));
            case 3:
                return Color.fromByte((byte) (fullByte & 0b00000011));
            default:
                return null;
        }
    }

    public Color get(final byte x, final byte y)
    {
        return getLineal(ofs(x, y));
    }

    private Color getLineal(final int ofs)
    {
        return getColorByteOffset(board[ofs >> 2], (byte) (ofs & 0b11));
    }

    public Color get(final short coord)
    {
        return get(Coord.X(coord), Coord.Y(coord));
    }

    public Color get(final CharSequence coord)
    {
        return get(Coord.parseToVal(coord));
    }

    private int ofs(final byte x, final byte y)
    {
        return y * size + x;
    }

    public byte size()
    {
        return this.size;
    }

    private int linealSize()
    {
        return this.size * this.size;
    }

    public void copyTo(final Board toBoard)
    {
        System.arraycopy(board, 0, toBoard.board, 0, board.length);
    }

    public short emptyRandom()
    {
        final int linealSize = linealSize();
        final int center = Move.RND.nextInt(linealSize);
        int delta = 0;
        boolean inc = true;
        while (true)
        {
            if ((center + delta < linealSize) && (center + delta >= 0) && getLineal(center + delta) == Color.EMPTY)
            {
                return Coord.XY((byte) ((center + delta) % size), (byte) ((center + delta) / size));
            }
            if (inc)
            {
                delta = -delta;
                delta++;
                if (delta > linealSize / 2)
                {
                    return Move.INVALID;
                }
            }
            else
            {
                delta = -delta;
            }
            inc = !inc;
        }
    }


    public int adjacentsWithColor(final short coord, final Color color)
    {
        int adjs = Adjacent.asVal(coord, size);
        while (Adjacent.iterHasNext(adjs))
        {
            if (get(Adjacent.iterPosition(adjs)) != color)
            {
                adjs = Adjacent.iterUnset(adjs);
            }
            adjs = Adjacent.iterMoveNext(adjs);
        }
        return Adjacent.iterReset(adjs);
    }

    public int count(final Color color)
    {

        final boolean incompleteFinalByte = boardBits % 8 != 0;
        int count = 0;
        int i = 0;
        for (; i < boardSize - (incompleteFinalByte ? 1 : 0); i++)
        {
            final byte b = board[i];
            if (b == 0)
            {
                if (color == Color.EMPTY)
                {
                    count = count + (8 / 2); // 8 bits, 2 bits per byte, generalize
                }
            }
            else
            {
                for (byte idx = 0; idx < 4; idx++)
                {
                    if (getColorByteOffset(b, idx) == color)
                    {
                        count++;
                    }
                }
            }
        }
        if (incompleteFinalByte)
        {
            final byte b = board[i];
            for (byte idx = 0; idx < (boardBits % 8) / 2; idx++)
            {
                if (getColorByteOffset(b, idx) == color)
                {
                    count++;
                }
            }
        }
        return count;
    }

    public void clear()
    {
        Arrays.fill(board, (byte) 0);
    }


    public String toString()
    {
        // nice to have: + in special places in the board
        final StringBuffer sb = new StringBuffer();
        sb.append("hash: ").append(hashCode());
        sb.append("\n   ");

        for (int x = 0; x < size; x++)
        {
            final char symb = (x <= 'I' - 'A' - 1) ? (char) (65 + x) : (char) (65 + 1 + x);
            sb.append(symb).append(' ');
        }
        sb.append("\n");

        for (byte y = 0; y < size; y++)
        {
            final int yToUse = size - y;
            if (yToUse < 10)
            {
                sb.append(' ');
            }
            sb.append(yToUse);
            sb.append(' ');
            for (byte x = 0; x < size; x++)
            {
                sb.append(get(x, (byte) (yToUse - 1)).symbol);
                sb.append(' ');
            }
            sb.append(yToUse).append("\n");
        }

        sb.append("   ");
        for (byte x = 0; x < size; x++)
        {
            final char symb = (x <= 'I' - 'A' - 1) ? (char) (65 + x) : (char) (65 + 1 + x);
            sb.append(symb).append(' ');
        }

        sb.append("\n");

        return sb.toString();
    }

    public int extract(final Board other)
    {
        int result = 0;
        if (other == null || other.size != size)
        {
            throw new IllegalArgumentException("Can't remove using different board sizes, or null boards.");
        }
        for (int i = 0; i < boardSize; i++)
        {
            if (other.board[i] != 0)
            {
                for (byte j = 0; j < 4; j++)
                {
                    final Color bColor = getColorByteOffset(board[i], j);
                    final Color oColor = getColorByteOffset(other.board[i], j);
                    if (bColor != Color.EMPTY && bColor == oColor)
                    {
                        board[i] = setColorByteOffset(board[i], j, Color.EMPTY);
                        result++;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int hashCode()
    {
        return murmur32(board, board.length, 377373);
    }

    public static int murmur32(final byte[] data, final int length, final int seed)
    {
        // MurMur32 hash
        final int m = 0x5bd1e995;
        final int r = 24;

        // Initialize the hash to a random value
        int h = seed ^ length;
        final int length4 = length / 4;

        for (int i = 0; i < length4; i++)
        {
            final int i4 = i * 4;
            int k = (data[i4 + 0] & 0xff) + ((data[i4 + 1] & 0xff) << 8) + ((data[i4 + 2] & 0xff) << 16) + ((data[i4 + 3] & 0xff) << 24);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // Handle the last few bytes of the input array
        switch (length % 4)
        {
            case 3:
                h ^= (data[(length & ~3) + 2] & 0xff) << 16;
            case 2:
                h ^= (data[(length & ~3) + 1] & 0xff) << 8;
            case 1:
                h ^= (data[length & ~3] & 0xff);
                h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (!(obj instanceof Board))
        {
            return false;
        }
        return this.hashCode() == obj.hashCode();
    }
}
