package uk.kukino.sgo;

public class Board {

    private final byte size;
    private byte[] board;
    // 2 bits per intersection i.e. 19x19 ~= 90 bytes, 9x9 ~= 20 bytes -- everything fits well in L1

    public Board(final byte size) {
        this.size = size;
        board = new byte[(this.size * this.size * 2 / 8) + 1];
    }

    public void set(final byte x, final byte y, final Color color) {
        int ofs = ofs(x, y);
        switch (ofs % 4) {
            case 0:
                board[ofs / 4] = (byte) ((board[ofs / 4] & (byte) 0b00111111) | (color.b << 6));
                break;
            case 1:
                board[ofs / 4] = (byte) ((board[ofs / 4] & (byte) 0b11001111) | (color.b << 4));
                break;
            case 2:
                board[ofs / 4] = (byte) ((board[ofs / 4] & (byte) 0b11110011) | (color.b << 2));
                break;
            case 3:
                board[ofs / 4] = (byte) ((board[ofs / 4] & (byte) 0b11111100) | color.b);
                break;
            default:
        }
    }

    public void set(final short value) {
        if (Move.isStone(value)) {
            set(Move.x(value), Move.y(value), Move.color(value));
        }
    }

    public Color get(final byte x, final byte y) {
        int ofs = ofs(x, y);
        switch (ofs % 4) {
            case 0:
                return Color.fromByte((byte) (board[ofs / 4] >> 6 & 0b11));
            case 1:
                return Color.fromByte((byte) ((board[ofs / 4] & 0b00110000) >> 4));
            case 2:
                return Color.fromByte((byte) ((board[ofs / 4] & 0b00001100) >>> 2));
            case 3:
                return Color.fromByte((byte) (board[ofs / 4] & 0b00000011));
            default:
                return null;
        }
    }

    public Color get(final short coord) {
        return get(Coord.x(coord), Coord.y(coord));
    }

    public Color get(final CharSequence coord) {
        return get(Coord.parseToVal(coord));
    }

    private int ofs(final byte x, final byte y) {
        return y * size + x;
    }

    public byte size() {
        return this.size;
    }

    public void copyTo(Board toBoard) {
        System.arraycopy(this.board, 0, toBoard.board, 0, this.board.length);
    }

    public byte adjacentsWithColor(short[] arr, short coord, Color color) {
        byte res = Coord.adjacents(arr, coord, size);
        byte newRes = res;
        for (byte i = 0; i < res; i++) {
            if (get(arr[i]) != color) {
                arr[i] = Coord.INVALID;
                newRes--;
            }
        }

        if (newRes == 0) {
            return 0;
        }

        boolean done = false;
        boolean somethingMoved = false;
        byte i = 0;
        while (!done) {
            if (i < 3 && arr[i] == Coord.INVALID && arr[i + 1] != Coord.INVALID) {
                arr[i] = arr[i + 1];
                arr[i + 1] = Coord.INVALID;
                somethingMoved = true;
            }
            i++;
            if (i == res) {
                if (somethingMoved) {
                    somethingMoved = false;
                    i = 0;
                } else {
                    done = true;
                }
            }
        }
        return newRes;
    }

    //TODO: This can be way optimised by scanning empty bytes
    public int count(final Color color) {
        int count = 0;
        for (byte x = 0; x < size; x++) {
            for (byte y = 0; y < size; y++) {
                if (get(x, y) == color) count++;
            }
        }
        return count;
    }

}
