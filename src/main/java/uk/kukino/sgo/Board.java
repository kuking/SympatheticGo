package uk.kukino.sgo;

import net.openhft.chronicle.bytes.Bytes;

import java.nio.ByteBuffer;

public class Board
{
    private final byte size;
    private final int boardBits;
    private final int boardSize;
    private Bytes<ByteBuffer> board;
    // 2 bits per intersection i.e. 19x19 ~= 90 bytes, 9x9 ~= 20 bytes -- everything fits well in L1

    public Board(final byte size)
    {
        this.size = size;
        boardBits = this.size * this.size * 2;
        boardSize = (boardBits / 8) + ((boardBits % 8 == 0) ? 0 : 1);
        board = Bytes.elasticByteBuffer(boardSize);
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
        board.writeByte(ofs >> 2, setColorByteOffset(board.readByte(ofs >> 2), (byte) (ofs & 0b11), color));
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
        final int ofs = ofs(x, y);
        return getColorByteOffset(board.readByte(ofs >> 2), (byte) (ofs & 0b11));
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

    public void copyTo(final Board toBoard)
    {
        toBoard.board.writePosition(0).write(board.readPosition(0));
    }


    public long adjacentsWithColor(final short coord, final Color color)
    {
        long adjs = Adjacent.asVal(coord, size);
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
        board.readPosition(0);
        for (int i = 0; i < boardSize - (incompleteFinalByte ? 1 : 0); i++)
        {
            final byte b = board.readByte();
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
            final byte b = board.readByte(); // last byte
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
        board.writePosition(0);
        for (int i = 0; i < boardSize; i++)
        {
            board.writeByte((byte) 0);
        }
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

    public String toHexString()
    {
        return board.toHexString(0, boardSize);
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
            if (other.board.readByte(i) != 0)
            {
                for (byte j = 0; j < 4; j++)
                {
                    final Color bColor = getColorByteOffset(board.readByte(i), j);
                    final Color oColor = getColorByteOffset(other.board.readByte(i), j);
                    if (bColor != Color.EMPTY && bColor == oColor)
                    {
                        board.writeByte(i, setColorByteOffset(board.readByte(i), j, Color.EMPTY));
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
        board.readPosition(0);
        final int modAdler = 65521;
        int a = 1;
        int b = 0;
        for (int i = 0; i < boardSize; i++)
        {
            a = (a + board.readByte()) % modAdler;
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
