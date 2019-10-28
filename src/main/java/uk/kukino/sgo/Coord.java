package uk.kukino.sgo;

public class Coord {

    // short = 0123456789abcdef
    //         CCXXXXXXXYYYYYYY = General format
    //         1111111111111111 = Invalid
    //         CC00000000000000 = Pass

    static final short INVALID = 0xffffffff;

    private short value;

    boolean parse(final CharSequence seq) {
        value = Coord.parseToVal(seq);
        return isValid();
    }


    public static short parseToVal(final CharSequence seq) {
        if (seq == null) return INVALID;
        int i = Parsing.scanSpaces(seq, 0);
        if (i == seq.length()) return INVALID;
        int j = Parsing.scanAlphas(seq, i);
        if (i == j || i + 2 < j) return INVALID;

        byte x;
        byte y;
        byte a = (byte) (Character.toUpperCase(seq.charAt(i)) - 64);
        if (a > 'I' - 65) a--;
        if (i + 1 == j) {
            x = a;
        } else {
            byte b = (byte) (Character.toUpperCase(seq.charAt(j - 1)) - 64);
            if (b > 'I' - 65) b--;
            x = (byte) ((a * (byte) 26) + b);
        }

        y = 0;
        i = j;
        while (i < seq.length() && seq.charAt(i) >= '0' && seq.charAt(i) <= '9') {
            y = (byte) ((y * 10) + (seq.charAt(i) - '0'));
            i++;
            if (i - j > 3) return INVALID;
        }
        if (i == j) return INVALID;

        return XY(x, y);
    }

    public static short XY(final byte x, final byte y) {
        return (short) ((short) ((x & 0x7f) << 7) | (short) (y & 0x7f));
    }

    public void assignXY(final byte x, final byte y) {
        value = Coord.XY(x, y);
    }

    public static byte x(final short val) {
        return (byte) ((val >> 7) & 0x7f);
    }

    public byte x() {
        return Coord.x(value);
    }

    public static byte y(final short val) {
        return (byte) (val & 0x7f);
    }

    public byte y() {
        return Coord.y(value);
    }

    public static boolean isValid(final short val) {
        return val != INVALID;
    }

    public boolean isValid() {
        return Coord.isValid(value);
    }

    /***
     * Adjacent cells are returned in wall clock order, if the coordinate does not have four adjacent, it will return
     * the number of adjacent interceptions, and the array will be filled up to four elements of invalid coordinates.
     * @param result
     * @param coord
     * @param boardSize
     * @return
     */
    public static byte adjacents(short[] result, final short coord, final byte boardSize) {
        int c = 0;
        byte x = Coord.x(coord);
        byte y = Coord.y(coord);
        if (y - 1 > 0) {
            result[c++] = Coord.XY(x, (byte) (y - 1));
        }
        if (x + 1 < boardSize) {
            result[c++] = Coord.XY((byte) (x + 1), y);
        }
        if (y + 1 < boardSize) {
            result[c++] = Coord.XY(x, (byte) (y + 1));
        }
        if (x - 1 > 0) {
            result[c++] = Coord.XY((byte) (x - 1), y);
        }
        for (int i = c; i < 4; i++) result[i] = Coord.INVALID;
        return (byte) c;
    }

    public byte adjacents(short[] values, final byte size) {
        return Coord.adjacents(values, value, size);
    }

}
