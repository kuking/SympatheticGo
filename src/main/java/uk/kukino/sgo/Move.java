package uk.kukino.sgo;

public class Move {

    // short = 0123456789abcdef
    //         CCXXXXXXXYYYYYYY
    //
    private short value;

    private Parsing.UpperCaseCharSequence upperSeq = new Parsing.UpperCaseCharSequence();

    public Color color() {
        return Move.color(value);
    }

    public boolean parse(final CharSequence sequence) {
        byte x;
        byte y;
        Color color = Color.EMPTY;

        if (sequence == null) {
            return setInvalid();
        }

        int i = 0, j;
        upperSeq.assign(sequence);

        while (i < upperSeq.length() && upperSeq.charAt(i) == ' ') i++;
        if (i == upperSeq.length()) return setInvalid();
        j = i;
        while (j < upperSeq.length() && upperSeq.charAt(j) != ' ') j++;
        if (i == j) return setInvalid();

        final CharSequence colorCharSeq = upperSeq.subSequence(i, j);
        if ("BLACK".contentEquals(colorCharSeq) || "B".contentEquals(colorCharSeq)) {
            color = Color.BLACK;
        }
        if ("WHITE".contentEquals(colorCharSeq) || "W".contentEquals(colorCharSeq)) {
            color = Color.WHITE;
        }

        // A/B/AA
        i = j;
        while (i < upperSeq.length() && upperSeq.charAt(i) == ' ') i++;
        if (i == upperSeq.length()) return setInvalid();
        j = i;
        while (j < upperSeq.length() && upperSeq.charAt(j) >= 'A' && upperSeq.charAt(j) <= 'Z') j++;
        if (i == j) return setInvalid();

        if (j - i == 4 && "PASS".contentEquals(upperSeq.subSequence(i, j))) {
            isPass(color);
        } else {
            byte a = (byte) (upperSeq.charAt(i) - 64);
            if (a > 'I' - 65) a--;
            if (i + 1 == j) {
                x = a;
            } else {
                byte b = (byte) (upperSeq.charAt(j - 1) - 64);
                if (b > 'I' - 65) b--;
                x = (byte) ((a * (byte) 25) + b);
            }
            y = 0;
            while (j < upperSeq.length() && upperSeq.charAt(j) >= '0' && upperSeq.charAt(j) <= '9') {
                y = (byte) ((y * 10) + (upperSeq.charAt(j) - '0'));
                j++;
            }
            move(x, y, color);
        }
        return true;
    }

    private static Move moveForStaticParsing = new Move();

    public static short parseToValue(final CharSequence sequence) {
        moveForStaticParsing.parse(sequence);
        return moveForStaticParsing.value;
    }

    private boolean setInvalid() {
        value = 0;
        return false;
    }

    public void assign(short value) {
        this.value = value;
    }

    public void move(final byte x, final byte y, final Color color) {
        value = (short) ((short) ((color.b) << 14) | (short) ((x & 0x7f) << 7) | (short) (y & 0x7f));
    }

    public void isPass(final Color color) {
        value = (short) ((color.b) << 14);
    }

    public static byte x(final short value) {
        return (byte) ((value >> 7) & 0x7f);
    }

    public byte x() {
        return Move.x(value);
    }

    public static byte y(final short value) {
        return (byte) (value & 0x7f);
    }

    public byte y() {
        return Move.y(value);
    }

    static public Color color(final short value) {
        return Color.fromByte((byte) (value >> 14 & 3));
    }

    static public boolean isPass(short value) {
        return Move.x(value) == (short) 0 &&
                Move.y(value) == (short) 0 &&
                color(value) != Color.EMPTY;
    }

    public boolean isPass() {
        return isPass(value);
    }

    public static boolean isValid(final short value) {
        return value != (short) 0;
    }

    public boolean isValid() {
        return Move.isValid(value);
    }

    public static boolean isStone(final short value) {
        return isValid(value) && !isPass(value);

    }

    public boolean isStone() {
        return isStone(value);
    }


}
