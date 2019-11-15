package uk.kukino.sgo;

import java.util.Arrays;

public class Board
{
    private final byte size;
    private byte[] board;
    // 2 bits per intersection i.e. 19x19 ~= 90 bytes, 9x9 ~= 20 bytes -- everything fits well in L1

    public Board(final byte size)
    {
        this.size = size;
        board = new byte[(this.size * this.size * 2 / 8) + 1];
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
            set(Move.x(value), Move.y(value), Move.color(value));
        }
    }

    public void set(final short move, final Color color)
    {
        set(Coord.x(move), Coord.y(move), color);
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
        final int ofs = ofs(x, y);
        return getColorByteOffset(board[ofs >> 2], (byte) (ofs & 0b11));
    }

    public Color get(final short coord)
    {
        return get(Coord.x(coord), Coord.y(coord));
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

    public void copyTo(final Board toBoard)
    {
        System.arraycopy(this.board, 0, toBoard.board, 0, this.board.length);
    }

    public byte adjacentsWithColor(final short[] arr, final short coord, final Color color)
    {
        byte res = Coord.adjacents(arr, coord, size);
        int i = 0;
        boolean done = false;
        while (!done)
        {
            done = true;
            while (i < res && get(arr[i]) == color)
            {
                i++;
            }
            if (i + 1 < res)
            {
                for (int ii = i; ii + 1 < res; ii++)
                {
                    arr[ii] = arr[ii + 1];
                }
                res--;
                done = false;
            }
            else if (i + 1 == res)
            {
                if (get(arr[i]) != color)
                {
                    res--;
                }
            }
        }
        return res;
    }

    public int count(final Color color)
    {
        int count = 0;
        for (int i = 0; i < board.length; i++)
        {
            final byte b = board[i];
            if (b == 0)
            {
                if (color == Color.EMPTY)
                {
                    count = count << 2;
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
        return count;
    }

    public boolean countIsZero(final Color color)
    {
        for (final byte b : board)
        {
            if (b == 0)
            {
                if (color == Color.EMPTY)
                {
                    return false;
                }
            }
            else
            {
                for (byte idx = 0; idx < 4; idx++)
                {
                    if (getColorByteOffset(b, idx) == color)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
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
            if (y < 9)
            {
                sb.append(' ');
            }
            sb.append((y + 1));
            sb.append(' ');
            for (byte x = 0; x < size; x++)
            {
                sb.append(get(x, y).symbol);
                sb.append(' ');
            }
            sb.append((y + 1) % 10).append("\n");
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
        for (int i = 0; i < board.length; i++)
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
// Adler algorithm: https://en.wikipedia.org/wiki/Adler-32
// a & b should be unsigned, not perfect but OK enough.
        final int modAdler = 65521;
        int a = 1;
        int b = 0;
        for (int i = 0; i < board.length; i++)
        {
            a = (a + board[i]) % modAdler;
            b = (b + a) % modAdler;
        }
        return (b << 16) | a;
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
