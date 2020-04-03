package uk.kukino.sgo.sgf;

import uk.kukino.sgo.base.Color;

import static uk.kukino.sgo.util.Parsing.*;

public final class Result
{

    public enum Outcome
    {
        Standard, Resign, Forfeit, Void, Timeout;
    }


    private final Color winner;
    private final Outcome outcome;
    private final float points;

    private Result(final Color winner, final Outcome outcome, final float points)
    {
        this.outcome = outcome;
        this.winner = winner;
        this.points = points;
    }

    public static final Result fromCs(final CharSequence cs)
    {
        if (cs == null)
        {
            return null;
        }
        int i = scanSpaces(cs, 0);
        int j = scanAlphas(cs, i);

        if (j - i == 4)
        {
            if (sameIgnoreCase(cs.subSequence(i, j), "VOID"))
            {
                i = scanSpaces(cs, i);
                if (i == cs.length() - 1)
                {
                    return new Result(null, Outcome.Void, Float.NaN);
                }
                return null;
            }
        }

        final Color color = parseColor(cs, i, j);
        if (color == null || cs.length() <= j || cs.charAt(j) != '+')
        {
            return null;
        }

        i = j + 1;
        j = scanNonSpaces(cs, j);
        final CharSequence lastToken = cs.subSequence(i, j);
        if (sameIgnoreCase(lastToken, "R") || sameIgnoreCase(lastToken, "RESIGN"))
        {
            return new Result(color, Outcome.Resign, Float.NaN);
        }
        if (sameIgnoreCase(lastToken, "T") || sameIgnoreCase(lastToken, "TIME"))
        {
            return new Result(color, Outcome.Timeout, Float.NaN);
        }
        if (sameIgnoreCase(lastToken, "F") || sameIgnoreCase(lastToken, "FORFEIT"))
        {
            return new Result(color, Outcome.Forfeit, Float.NaN);
        }
        final float value = parseFloat(lastToken);
        if (!Float.isNaN(value))
        {
            return new Result(color, Outcome.Standard, value);
        }
        return null;
    }

    @Override
    protected Result clone()
    {
        return this;
    }

    public Outcome outcome()
    {
        return this.outcome;
    }

    public boolean hasWinner()
    {
        return winner != null;
    }

    public Color winner()
    {
        return winner;
    }

    public boolean hasPoints()
    {
        return !Float.isNaN(points);
    }

    public float points()
    {
        return points;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        if (hasWinner())
        {
            if (winner == Color.WHITE)
            {
                sb.append('W');
            }
            else if (winner == Color.BLACK)
            {
                sb.append('B');
            }
            else
            {
                sb.append('?');
            }
        }
        sb.append('+');

        switch (outcome)
        {
            case Standard:
                sb.append(String.format("%.1f", points));
                break;
            case Forfeit:
                sb.append('F');
                break;
            case Timeout:
                sb.append('T');
                break;
            case Resign:
                sb.append('R');
                break;
            case Void:
                return "Void";
        }
        return sb.toString();
    }
}
