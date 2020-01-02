package uk.kukino.sgo.base;

import uk.kukino.sgo.util.Parsing;

import static uk.kukino.sgo.util.Parsing.*;

public class Score
{
    // Represented as an int, bit 0 & 1 = color, 16 high value bits, a short representing the score * 10, there are some special values
    //  Normal Won: Score*10 << 16          + color
    // Won  b/time: Short.MAX_VALUE-1 << 16 + color
    //      Resign: Short.MAX_VALUE << 16   + color
    //        Draw: 0                       + Color.EMPTY
    //     Unknown: 0xffff0000              + 0xffff   (hence 0xffffffff)

    // Color == Empty: Draw, value == Short.MAX_VALUE resign, i.e. Color White, Short.MAX_VALUE, implies White Won by Black resignation.

    public static final int UNKNOWN = 0xffffffff;


    public static int wonWithScore(final Color color, final float score)
    {
        final short s = (short) (score * 10f);
        return (s << 16) + color.b;
    }

    public static int wonByResign(final Color color)
    {
        return (Short.MAX_VALUE << 16) + color.b;
    }

    public static int draw()
    {
        return Color.EMPTY.b;
    }

    public static int wonByTime(final Color color)
    {
        return (Short.MAX_VALUE - 1 << 16) + color.b;
    }

    public static boolean isResign(final int score)
    {
        return !isDraw(score) && rawPoints(score) == Short.MAX_VALUE;
    }

    public static boolean isTimeout(final int score)
    {
        return !isDraw(score) && rawPoints(score) == Short.MAX_VALUE - 1;
    }

    public static boolean isDraw(final int score)
    {
        return (score & 3) == Color.EMPTY.b;
    }

    public static boolean isUnknown(final int score)
    {
        return score == UNKNOWN;
    }

    public static Color winner(final int score)
    {
        if (score == UNKNOWN)
        {
            return Color.EMPTY;
        }
        return Color.fromByte((byte) (score & 3));
    }

    private static short rawPoints(final int score)
    {
        return (short) (score >> 16);
    }

    public static float points(final int score)
    {
        final short s = (short) (score >> 16);
        if (s == Short.MAX_VALUE)
        {
            return Float.POSITIVE_INFINITY;
        }
        else if (s == Short.MAX_VALUE - 1)
        {
            return Float.POSITIVE_INFINITY;
        }
        else if (s == 0 || score == UNKNOWN)
        {
            return Float.NaN;
        }
        return s / 10f;
    }

    public static void write(final StringBuilder sb, final int score)
    {
        if (score == UNKNOWN)
        {
            sb.append("Unknown");
        }
        else if (isDraw(score))
        {
            sb.append("Draw");
        }
        else
        {
            sb.append(winner(score) == Color.WHITE ? 'W' : 'B');
            sb.append('+');
            if (isResign(score))
            {
                sb.append("Resign");
            }
            else if (isTimeout(score))
            {
                sb.append("Time");
            }
            else
            {
                final float points = points(score);
                if (Math.rint(points) == points)
                {
                    sb.append((int) points);
                }
                else
                {
                    sb.append(points);
                }
            }
        }
    }

    public static int parseToVal(final CharSequence cs)
    {
        int s = scanSpaces(cs, 0);
        if (cs.length() >= s + 4 && Parsing.sameIgnoreCase(cs.subSequence(s, s + 4), "DRAW"))
        {
            if (scanSpaces(cs, s + 4) == cs.length())
            {
                return draw();
            }
            else
            {
                return UNKNOWN;
            }
        }

        int e = Math.min(scanToChar(cs, '+', s), scanToChar(cs, ' ', s));
        final Color color = Color.parse(cs.subSequence(s, e));
        if (color == null || color == Color.EMPTY || color == Color.MARK)
        {
            return UNKNOWN;
        }

        s = e + 1; // plus 'the plus'
        e = Parsing.scanNonSpaces(cs, s);
        if (e != cs.length() && scanSpaces(cs, e) != cs.length())
        {
            return UNKNOWN;
        }
        final float points = parseFloat(cs, s, e);
        if (!Float.isNaN(points))
        {
            return wonWithScore(color, points);
        }
        else if (Parsing.sameIgnoreCase(cs.subSequence(s, e), "TIME") || Parsing.sameIgnoreCase(cs.subSequence(s, e), "T"))
        {
            return wonByTime(color);
        }
        else if (Parsing.sameIgnoreCase(cs.subSequence(s, e), "RESIGN") || Parsing.sameIgnoreCase(cs.subSequence(s, e), "R"))
        {
            return wonByResign(color);
        }
        return UNKNOWN;
    }


}
