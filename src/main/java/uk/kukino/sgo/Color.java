package uk.kukino.sgo;

public enum Color {
    EMPTY((byte) 0), BLACK((byte) 1), WHITE((byte) 2), MARK((byte) 3);

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
        } else if (b == MARK.b) {
            return MARK;
        } else {
            throw new IllegalStateException("Unexpected Color byte value: " + b);
        }
    }

    public Color opposite() {
        switch (this) {
            case WHITE:
                return BLACK;
            case BLACK:
                return WHITE;
            case EMPTY:
                return MARK;
            case MARK:
                return EMPTY;
        }
        throw new IllegalArgumentException("This needs further implementation, missing opposite Color configuration");
    }
}
