package uk.kukino.sgo.base;

import uk.kukino.sgo.util.Parsing;

import java.util.SplittableRandom;

public final class Move
{

    // short = 0123456789abcdef
    //         CCXXXXXXXYYYYYYY = General format
    //         1111111111111111 = Invalid
    //         CC00000000000000 = Pass

    public static final short INVALID = 0xffffffff;
    public static final SplittableRandom RND = new SplittableRandom(System.currentTimeMillis());

    public static final short BLACK_PASS = move((byte) 127, (byte) 127, Color.BLACK);
    public static final short WHITE_PASS = move((byte) 127, (byte) 127, Color.WHITE);

    public static final short BLACK_A1 = parseToVal("BLACK A1");
    public static final short BLACK_A2 = parseToVal("BLACK A2");
    public static final short BLACK_A4 = parseToVal("BLACK A4");
    public static final short BLACK_B1 = parseToVal("BLACK B1");
    public static final short BLACK_B2 = parseToVal("BLACK B2");
    public static final short BLACK_C1 = parseToVal("BLACK C1");
    public static final short BLACK_D1 = parseToVal("BLACK D1");
    public static final short BLACK_D4 = parseToVal("BLACK D4");
    public static final short WHITE_A1 = parseToVal("WHITE A1");
    public static final short WHITE_A2 = parseToVal("WHITE A2");
    public static final short WHITE_A4 = parseToVal("WHITE A4");
    public static final short WHITE_B1 = parseToVal("WHITE B1");
    public static final short WHITE_B2 = parseToVal("WHITE B2");
    public static final short WHITE_C1 = parseToVal("WHITE C1");
    public static final short WHITE_D1 = parseToVal("WHITE D1");
    public static final short WHITE_D4 = parseToVal("WHITE D4");

    private Move()
    {
    }

    public static short parseToVal(final CharSequence seq)
    {
        short coord = Coord.INVALID;
        Color color = Color.EMPTY;

        if (seq == null)
        {
            return INVALID;
        }
        int i = Parsing.scanSpaces(seq, 0);
        if (i == seq.length())
        {
            return INVALID;
        }

        int j = Parsing.scanAlphas(seq, i);
        if (i == j)
        {
            return INVALID;
        }

        if (j - i == 1)
        {
            final char ch = Character.toUpperCase(seq.charAt(i));
            if (ch == 'B')
            {
                color = Color.BLACK;
            }
            else if (ch == 'W')
            {
                color = Color.WHITE;
            }
            else
            {
                return INVALID;
            }
        }
        else if (j - i == 5)
        {
            final char a = Character.toUpperCase(seq.charAt(i)); // not weird, on purpose to to avoid memory allocation i.e. char[]
            final char b = Character.toUpperCase(seq.charAt(i + 1));
            final char c = Character.toUpperCase(seq.charAt(i + 2));
            final char d = Character.toUpperCase(seq.charAt(i + 3));
            final char e = Character.toUpperCase(seq.charAt(i + 4));
            if (a == 'B' && b == 'L' && c == 'A' && d == 'C' && e == 'K')
            {
                color = Color.BLACK;
            }
            else if (a == 'W' && b == 'H' && c == 'I' && d == 'T' && e == 'E')
            {
                color = Color.WHITE;
            }
        }
        else
        {
            return INVALID;
        }

        i = Parsing.scanSpaces(seq, j + 1);
        if (i < seq.length())
        {
            coord = Coord.parseToVal(seq.subSequence(i, seq.length()));
        }

        if (!Coord.isValid(coord))
        { // PASS?
            j = Parsing.scanAlphas(seq, i);
            if (j - 4 == i)
            {
                final char a = Character.toUpperCase(seq.charAt(i)); // not weird, on purpose to to avoid memory allocation i.e. char[]
                final char b = Character.toUpperCase(seq.charAt(i + 1));
                final char c = Character.toUpperCase(seq.charAt(i + 2));
                final char d = Character.toUpperCase(seq.charAt(i + 3));
                if (a == 'P' && b == 'A' && c == 'S' && d == 'S')
                {
                    return Move.pass(color);
                }
            }
        }

        if (color == Color.EMPTY || !Coord.isValid(coord))
        {
            return INVALID;
        }

        return Move.move(coord, color);
    }

    public static short move(final short coord, final Color color)
    {
        return (short) ((short) ((color.b) << 14) | (short) (coord & (short) 0b11111111111111));
    }

    public static short move(final byte x, final byte y, final Color color)
    {
        return (short) ((short) ((color.b) << 14) | (short) ((x & 0x7f) << 7) | (short) (y & 0x7f));
    }

    public static short pass(final Color color)
    {
        if (color == Color.WHITE)
        {
            return WHITE_PASS;
        }
        else if (color == Color.BLACK)
        {
            return BLACK_PASS;
        }
        throw new IllegalArgumentException("Can't build a pass move with color" + color);
    }

    public static byte X(final short value)
    {
        return (byte) ((value >> 7) & 0x7f);
    }

    public static byte Y(final short value)
    {
        return (byte) (value & 0x7f);
    }

    public static Color color(final short value)
    {
        return Color.fromByte((byte) ((value & (short) 0b1100000000000000) >> 14 & 3));
    }

    public static boolean isPass(final short value)
    {
        return value == WHITE_PASS || value == BLACK_PASS;
    }

    public static short random(final byte size, final Color color)
    {
        return Move.move((byte) RND.nextInt(size), (byte) RND.nextInt(size), color);
    }

    public static boolean isValid(final short value)
    {
        return value != INVALID;
    }

    public static boolean isStone(final short value)
    {
        return isValid(value) && !isPass(value);
    }

    public static String shortToString(final short value)
    {
        final StringBuilder sb = new StringBuilder();
        write(sb, value);
        return sb.toString();
    }

    public static void write(final StringBuilder sb, final short value, final boolean includeColor, final boolean camelCase)
    {
        if (!isValid(value))
        {
            sb.append("Invalid");
            return;
        }

        if (includeColor && color(value) != null)
        {
            color(value).write(sb);
            sb.append(' ');
        }
        if (isPass(value))
        {
            if (camelCase)
            {
                sb.append("Pass");
            }
            else
            {
                sb.append("PASS");
            }
        }
        if (isStone(value))
        {
            Coord.write(sb, value);
        }
    }

    public static void write(final StringBuilder sb, final short value)
    {
        write(sb, value, true, true);
    }

    public static short oppositePlayer(final short move)
    {
        if (isPass(move))
        {
            return pass(color(move).opposite());
        }
        else if (isStone(move))
        {
            return move(X(move), Y(move), color(move).opposite());
        }
        return move;
    }

}
