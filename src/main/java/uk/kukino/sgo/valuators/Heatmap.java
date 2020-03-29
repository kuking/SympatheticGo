package uk.kukino.sgo.valuators;

import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Move;

import java.io.*;
import java.lang.ref.SoftReference;
import java.util.Arrays;

public class Heatmap implements Serializable
{
    private byte boardSize;
    private float[] values;
    private float min;
    private float max;
    private SoftReference<float[]> sortedSR;

    public Heatmap(final byte boardSize, final float[] values)
    {
        intialize(boardSize, values);
    }

    private void intialize(final byte boardSize, final float[] values)
    {
        this.boardSize = boardSize;
        this.values = Arrays.copyOf(values, values.length);
        this.min = Float.NaN;
        this.max = Float.NaN;
        this.sortedSR = new SoftReference<>(null);
    }

    private void resetCaches()
    {
        min = Float.NaN;
        max = Float.NaN;
        sortedSR.clear();
    }

    public float min()
    {
        if (!Float.isNaN(min))
        {
            return min;
        }
        min = Float.MAX_VALUE;
        for (int i = 0; i < values.length; i++)
        {
            if (min > values[i])
            {
                min = values[i];
            }
        }
        return min;
    }

    public float max()
    {
        if (!Float.isNaN(max))
        {
            return max;
        }
        max = Float.MIN_VALUE;
        for (int i = 0; i < values.length; i++)
        {
            if (max < values[i])
            {
                max = values[i];
            }
        }
        return max;
    }


    public void normalize()
    {
        final float[] newValues = new float[values.length];
        final float delta = max() - min();
        for (int i = 0; i < values.length; i++)
        {
            newValues[i] = (values[i] - min()) / delta;
        }
        values = newValues;
        resetCaches();
    }


    public float percentile(final int q)
    {
        if (q < 0 || q > 100)
        {
            throw new IllegalArgumentException("Percentile should be between 0 and 100, inclusive.");
        }
        float sorted[] = sortedSR.get();
        if (sorted == null)
        {
            sorted = Arrays.copyOf(values, values.length);
            Arrays.sort(sorted);
            sortedSR = new SoftReference<>(sorted);
        }
        if (q == 0)
        {
            return sorted[0];
        }
        else if (q == 100)
        {
            return sorted[sorted.length - 1];
        }
        return sorted[q * values.length / 100];
    }

    public float heat(final short coord)
    {
        if (Move.isPass(coord))
        {
            return values[boardSize * boardSize];
        }
        return values[Coord.linealOffset(coord, boardSize)];
    }

    public float heatLineal(final int ofs)
    {
        return values[ofs];
    }

    private int ofs(final byte x, final byte y)
    {
        return y * boardSize + x;
    }

    public float[] getCopy()
    {
        return Arrays.copyOf(values, values.length);
    }

    public byte size()
    {
        return boardSize;
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private static final String P_UNDER_20_SLOT = ANSI_RESET + '∙' + ANSI_RESET;
    private static final String P20_50_SLOT = ANSI_CYAN + '⬥' + ANSI_RESET;
    private static final String P50_75_SLOT = ANSI_PURPLE + 'o' + ANSI_RESET;
    private static final String P75_90_SLOT = ANSI_YELLOW + 'O' + ANSI_RESET;
    private static final String P90_100_SLOT = ANSI_RED + '@' + ANSI_RESET;

    private String heatChar(final float value, final float p20, final float p50, final float p75, final float p90)
    {
        if (value < p20)
        {
            return P_UNDER_20_SLOT;
        }
        else if (value < p50)
        {
            return P20_50_SLOT;
        }
        else if (value < p75)
        {
            return P50_75_SLOT;
        }
        else if (value < p90)
        {
            return P75_90_SLOT;
        }
        return P90_100_SLOT;
    }

    public String toString()
    {
        final float p20 = percentile(20);
        final float p50 = percentile(50);
        final float p75 = percentile(75);
        final float p90 = percentile(90);
        // nice to have: + in special places in the board
        final StringBuffer sb = new StringBuffer();
        sb.append(String.format("Range: [%.4f, %.4f]\n", min(), max()))
            .append(String.format("p20(%s): %.4f p50(%s): %.4f\n", P20_50_SLOT, p20, P50_75_SLOT, p50))
            .append(String.format("p75(%s): %.4f p90(%s): %.4f\n", P75_90_SLOT, p75, P90_100_SLOT, p90));

        sb.append("   ");
        for (int x = 0; x < boardSize; x++)
        {
            final char symb = (x <= 'I' - 'A' - 1) ? (char) (65 + x) : (char) (65 + 1 + x);
            sb.append(symb).append(' ');
        }
        sb.append("\n");

        for (byte y = 0; y < boardSize; y++)
        {
            final int yToUse = boardSize - y;
            if (yToUse < 10)
            {
                sb.append(' ');
            }
            sb.append(yToUse);
            sb.append(' ');
            for (byte x = 0; x < boardSize; x++)
            {
                sb.append(heatChar(values[ofs(x, (byte) (yToUse - 1))], p20, p50, p75, p90));
                sb.append(' ');
            }
            sb.append(yToUse).append("\n");
        }

        sb.append("   ");
        for (byte x = 0; x < boardSize; x++)
        {
            final char symb = (x <= 'I' - 'A' - 1) ? (char) (65 + x) : (char) (65 + 1 + x);
            sb.append(symb).append(' ');
        }
        sb.append("\n");
        sb.append(String.format("    Pass(%s): %.4f \n",
            heatChar(values[boardSize * boardSize], p20, p50, p75, p90), values[boardSize * boardSize]));

        return sb.toString();
    }


    private void writeObject(final ObjectOutputStream oos) throws IOException
    {
        oos.writeByte(boardSize);
        oos.writeInt(values.length);
        for (int i = 0; i < values.length; i++)
        {
            oos.writeFloat(values[i]);
        }
    }

    private void readObjectNoData() throws ObjectStreamException
    {
        intialize((byte) 0, new float[] {});
    }

    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        final byte size = ois.readByte();
        final int valuesSize = ois.readInt();
        final float[] values = new float[valuesSize];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = ois.readFloat();
        }
        this.intialize(size, values);
    }


}
