package uk.kukino.sgo.mc;

import uk.co.real_logic.agrona.collections.IntLruCache;
import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.util.Buffers;

import java.util.Arrays;

public class TTable
{
    private final int boardSize;
    private final Buffers<int[]> buffers; // int[((y*size+x)*2){+1}] first int, black count, second, white count.
    private final IntLruCache<int[]> cache;

    private short[] results;
    private float[] ratios;


    public TTable(final byte boardSize, final int levels, final int wide)
    {
        this.boardSize = boardSize;
        final int capacity = (int) Math.pow(wide, levels);
        buffers = new Buffers<>(capacity + 1, () -> new int[boardSize * boardSize * 2 + 2]); // last two are pass moves
        cache = new IntLruCache<>(capacity, key -> buffers.lease(), buf ->
        {
            Arrays.fill(buf, 0);
            buffers.ret(buf);
        });
        this.results = new short[boardSize * boardSize + 2];
        this.ratios = new float[boardSize * boardSize + 2];
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

    public int playoutsFor(final Game game)
    {
        return playoutsFor(game.getBoard().hashCode());
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
        buildResultsFromRatios(Math.min(qty, nonEmpty), color);
        return results;
    }

    public short[] topsFor(final Game game, final int qty, final Color color)
    {
        return topsFor(game.getBoard().hashCode(), qty, color);
    }

    public short[] uct(final int boardHash, final int qty, final Color color, final float factor)
    {
        Arrays.fill(ratios, Float.NaN);
        final int[] table = cache.lookup(boardHash);
        final int n = playoutsFor(boardHash);
        final double c2logn = factor * Math.log(n);
        int nonEmpty = 0;
        for (int i = 0; i < table.length; i += 2)
        {
            final float blackWin = table[i];
            final float whiteWin = table[i + 1];
            // https://thegreendestiny.wordpress.com/2009/10/22/computer-go-2/
            if (blackWin != 0 || whiteWin != 0)
            {
                final float tj = blackWin + whiteWin;
                final float xj = ((color == Color.WHITE) ? whiteWin : blackWin) / tj;
                final float uct = xj + (float) Math.sqrt(c2logn / tj);
                ratios[i / 2] = uct;
                nonEmpty++;
            }
        }
        buildResultsFromRatios(Math.min(qty, nonEmpty), color);
        return results;
    }

    public short[] uct(final Game game, final int qty, final Color color, final float factor)
    {
        return uct(game.getBoard().hashCode(), qty, color, factor);
    }

    private void buildResultsFromRatios(final int finalQty, final Color color)
    {
        if (finalQty == 0)
        {
            results[0] = Coord.INVALID;
            return;
        }
        Arrays.fill(results, 0, Math.min(results.length, finalQty + 1), Coord.INVALID);

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

        final int passBoundary = boardSize * boardSize;
        for (int i = 0; i < finalQty; i++)
        {
            final short orig = results[i];
            if (orig < passBoundary)
            {
                results[i] = Move.move(Coord.XY((byte) (orig % boardSize), (byte) (orig / boardSize)), color);
            }
            else if (orig >= passBoundary)
            {
                results[i] = Move.pass(color);
            }
        }
    }

    /**
     * Returns the last results' ratios, sorted. Obviously no thread safe.
     *
     * @return
     */
    public float[] lastRatiosSorted()
    {
        Arrays.sort(ratios);
        int tail = ratios.length - 1;
        while (tail > 0 && Float.isNaN(ratios[tail]))
        {
            tail--;
        }
        for (int i = 0; i < tail / 2; i++)
        {
            final float tmp = ratios[i];
            ratios[i] = ratios[tail - i];
            ratios[tail - i] = tmp;
        }
        return ratios;
    }

    /***
     * Account a winner for this particular board configuration, if Color==Empty, implies 'DRAW'
     * @param boardHash board' hash
     * @param move move
     * @param wins how many wins to account
     */
    public void account(final int boardHash, final short move, final int wins)
    {
        final Color c = Move.color(move);
        final int ofs;
        if (Move.isStone(move))
        {
            ofs = (((Move.Y(move) * boardSize) + Move.X(move)) << 1) + (c == Color.WHITE ? 1 : 0);
        }
        else if (Move.isPass(move))
        {
            ofs = ((boardSize * boardSize) << 1) + (c == Color.WHITE ? 1 : 0);
        }
        else
        {
            throw new IllegalArgumentException("I don't know how to handle move");
        }
        cache.lookup(boardHash)[ofs] += wins;

    }

    public void account(final int boardHash, final short move)
    {
        account(boardHash, move, 1);
    }

    public void account(final Game game, final short move, final int wins, final int losses)
    {
        account(game.getBoard().hashCode(), move, wins);
        account(game.getBoard().hashCode(), Move.oppositePlayer(move), losses);
    }

}
