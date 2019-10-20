package uk.kukino.sgo;

public enum Color {
    EMPTY((byte) 0), BLACK((byte) 1), WHITE((byte) 2);

    final byte b;

    Color(byte value) {
        b = value;
    }

    public static Color fromByte(byte b) {
        if (b == EMPTY.b) {
            return EMPTY;
        } else if (b == BLACK.b) {
            return BLACK;
        } else if (b == WHITE.b) {
            return WHITE;
        } else {
            throw new IllegalStateException("Unexpected Color byte value: " + b);
        }
    }
}
