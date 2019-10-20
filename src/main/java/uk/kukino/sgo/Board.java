package uk.kukino.sgo;

public class Board {

    final private byte size;
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

    private int ofs(final byte x, final byte y) {
        return y * size + x;
    }

    public byte size() {
        return this.size;
    }

}
