package uk.kukino.sgo;

public class Move {

    // short = 0123456789abcdef
    //         CCXXXXXXXYYYYYYY
    //
    private short value;
    private byte x, y;
    private Color color;
    private boolean valid;


    private Parsing.UpperCaseCharSequence upperSeq = new Parsing.UpperCaseCharSequence();

    public Color color() {
        return color;
    }

    public boolean parse(final CharSequence sequence) {
        x = y = 0;
        color = Color.EMPTY;

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
            pass(color);
        } else {
            byte a = (byte) (upperSeq.charAt(i) - 64);
            if (a > 'I' - 65) a--;
            if (i + 1 == j) {
                x = a;
            } else {
                byte b = (byte) (upperSeq.charAt(j-1) - 64);
                if (b > 'I' - 65) b--;
                x = (byte) ((a * (byte)25) + b);
            }
            y = 0;
            while (j < upperSeq.length() && upperSeq.charAt(j) >= '0' && upperSeq.charAt(j) <= '9') {
                y = (byte) ((y * 10) + (upperSeq.charAt(j) - '0'));
                j++;
            }
            move(x, y, color);
        }
        valid = true;
        return true;
    }

    private boolean setInvalid() {
        value = 0;
        valid = false;
        return false;
    }

    public void assign(short value) {
        this.value = value;
    }

    public void move(final byte x, final byte y, final Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        value = (short) ((color.b) << 14 + (x & 0x7f) << 7 + y & 0x7f);
    }

    public void pass(final Color color) {
        this.x = 0;
        this.y = 0;
        this.color = color;
        value = (short) ((color.b) << 14 + (x & 0x7f) << 7 + y & 0x7f);
    }

    public boolean valid() {
        return valid;
    }

    public byte x() {
        return x;
//        return (byte) ((value >>> 7) & 0x7f);
    }

    public byte y() {
        return y;
//        return (byte) (value & 0x7f);
    }

    public boolean pass() {
        return x == 0 && y  == 0;
    }
}
