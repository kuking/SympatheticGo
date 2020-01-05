package uk.kukino.sgo.mc;

import uk.co.real_logic.agrona.collections.IntLruCache;
import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;

import java.util.Arrays;

public class TTable
{
    final private int size;
    final private Buffers<int[]> buffers; // int[((y*size+x)*2){+1}] first int, black count, second, white count.
    final private IntLruCache<int[]> cache;

    private short[] results;
    private float[] ratios;


    public TTable(final byte size, final byte levels, final byte wide)
    {
        this.size = size;
        final int capacity = (int) Math.pow(wide, levels);
        buffers = new Buffers<>(capacity + 1, () -> new int[size * size * 2]);
        cache = new IntLruCache<>(capacity, key -> buffers.lease(), buf ->
        {
            Arrays.fill(buf, 0);
            buffers.ret(buf);
        });
        this.results = new short[size * size];
        this.ratios = new float[size * size];
    }

    public int playoutsFor(final int boardHash)
    {
        final int table[] = cache.lookup(boardHash);
        int playouts = 0;
        for (int i = 0; i < table.length; i++)
        {
            playouts += table[i];
        }
        return playouts;
    }

    public short[] topsFor(final int boardHash, final int qty, final Color color)
    {
        Arrays.fill(ratios, Float.NaN);
        final int[] table = cache.lookup(boardHash);
        int nonEmpty = 0;
        for (int i = 0; i < table.length; i += 2)
        {
            final int blackWin = table[i];
            final int whiteWin = table[i + 1];
            if (blackWin != 0 || whiteWin != 0)
            {
                if (color == Color.WHITE)
                {
                    ratios[i / 2] = (float) whiteWin / (float) (blackWin + whiteWin);
                }
                else if (color == Color.BLACK)
                {
                    ratios[i / 2] = (float) blackWin / (float) (blackWin + whiteWin);
                }
                else
                {
                    throw new IllegalArgumentException("I can't handle color " + color);
                }
                nonEmpty++;
            }
        }

        final int finalQty = Math.min(qty, nonEmpty);
        Arrays.fill(results, 0, Math.min(results.length, finalQty + 1), Coord.INVALID);
        if (finalQty == 0)
        {
            return results;
        }

        for (int i = 0; i < ratios.length; i++)
        {
            if (!Float.isNaN(ratios[i]))
            {
                for (int j = 0; j < finalQty; j++)
                {
                    if (results[j] == Coord.INVALID)
                    {
                        results[j] = (short) i;
                        break;
                    }
                    else if (ratios[results[j]] < ratios[i])
                    {
                        System.arraycopy(results, j, results, j + 1, finalQty - j - 1);
                        results[j] = (short) i;
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < finalQty; i++)
        {
            final short orig = results[i];
            results[i] = Coord.XY((byte) (orig % size), (byte) (orig / size));
        }

        return results;
    }

    public short[] uct(final int boardHash, final int qty)
    {
        return new short[0];

    }

    /***
     * Account a winner for this particular board configuration, if Color==Empty, implies 'DRAW'
     * @param boardHash board' hash
     * @param winner who won in this hash
     */
    public void account(final int boardHash, final short coord, final Color winner, final int wins)
    {
        final byte x = Coord.X(coord);
        final byte y = Coord.Y(coord);
        int ofs = (((y * size) + x) << 1) + (winner == Color.WHITE ? 1 : 0);
        cache.lookup(boardHash)[ofs] += wins;
    }

    public void account(final int boardHash, final short coord, final Color winner)
    {
        account(boardHash, coord, winner, 1);
    }

}
