package uk.kukino.sgo.mc;

import net.openhft.affinity.AffinityLock;
import uk.co.real_logic.agrona.collections.IntLruCache;
import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class TranspositionTable
{
    private static final int BOARD_SIZE = 9;
    private static final byte KOMI = 45;
    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    private final ForkJoinPool POOL = new ForkJoinPool(THREADS);
    private Buffers<Game> buffers = new Buffers<>(THREADS * 8, () -> new Game((byte) BOARD_SIZE, (byte) 0, KOMI));

    public static class Stats
    {
        int blackWins;
        int whiteWins;

        public Stats(int hash)
        {
            blackWins = 0;
            whiteWins = 0;
        }
    }


    IntLruCache<Stats> hashes = new IntLruCache(10_000, Stats::new, i -> System.out.println("Removing " + i + " from cache"));

    private int[] randomPlay(final Game startGame, final Game tempGame, final int gamesToPlay)
    {
        final AffinityLock al = AffinityLock.acquireLock();
        try
        {
            int black = 0;
            int white = 0;
            for (int i = 0; i < gamesToPlay; i++)
            {
                startGame.copyTo(tempGame);
                tempGame.finishRandomly();
                if (tempGame.simpletonWinnerUsingChineseRules() == Color.BLACK)
                {
                    black++;
                }
                else
                {
                    white++;
                }
            }
            return new int[] {
                startGame.getBoard().hashCode(), black, white
            };
        }
        finally
        {
            buffers.ret(tempGame);
            buffers.ret(startGame);
            al.release();
        }
    }

    public void play(final Game game, final int games)
    {
        final List<ForkJoinTask<int[]>> results = new ArrayList<>();
        final int playedPerPosition = games / game.getBoard().count(Color.EMPTY) / 4;
        int played = 0;
        while (played < games)
        {
            System.out.println(played + " < " + games);
            for (byte x = 0; x < BOARD_SIZE; x++)
            {
                for (byte y = 0; y < BOARD_SIZE; y++)
                {
                    if (game.getBoard().get(x, y) == Color.EMPTY && played < games)
                    {
                        final Game startGame = buffers.lease();
                        game.copyTo(startGame);
                        if (startGame.play(Move.move(x, y, game.playerToPlay())))
                        {
                            results.add(POOL.submit(() -> randomPlay(startGame, buffers.lease(), playedPerPosition)));
                            played += playedPerPosition;
                        }
                        else
                        {
                            buffers.ret(startGame);
                        }
                    }
                }
            }
        }

        System.out.println("Accounting ...");
        results.forEach(fjt ->
        {
            final int[] res;
            try
            {
                res = fjt.get();
            }
            catch (final InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
                return;
            }
            final Stats stats = hashes.lookup(res[0]);
            stats.blackWins += res[1];
            stats.whiteWins += res[2];
        });


        float bestRatio = -1;
        Stats bestStats = null;
        short bestMove = Move.INVALID;
        Game bestGame = buffers.lease();

        for (byte x = 0; x < BOARD_SIZE; x++)
        {
            for (byte y = 0; y < BOARD_SIZE; y++)
            {
                if (game.getBoard().get(x, y) == Color.EMPTY) //XXX: do better than just empty
                {
                    final Game startGame = buffers.lease();
                    game.copyTo(startGame);
                    final short move = Move.move(x, y, game.playerToPlay());
                    if (startGame.play(move))
                    {
                        final Stats stats = hashes.lookup(startGame.getBoard().hashCode());

                        float thisRatio = ((float) stats.blackWins / (float) (stats.whiteWins + stats.blackWins));
                        if (startGame.playerToPlay() == Color.BLACK)
                        {
                            thisRatio = 1f / thisRatio;
                        }

                        if (bestRatio < thisRatio)
                        {
                            bestRatio = thisRatio;
                            bestMove = move;
                            bestStats = stats;
                            startGame.copyTo(bestGame);
                        }

                        final Color winner = (stats.blackWins > stats.whiteWins) ? Color.BLACK : Color.WHITE;
                        System.out.println("Game Playing in " + Coord.shortToString(move) + " " + game.playerToPlay());
                        System.out.println(winner + " Wins (" + stats.blackWins + " black vs " + stats.whiteWins + " whites) - " + thisRatio);
                        System.out.println(startGame);
                    }
                    buffers.ret(startGame);
                }
            }
        }

        final Color winner = (bestStats.blackWins > bestStats.whiteWins) ? Color.BLACK : Color.WHITE;
        System.out.println("Game Playing in " + Coord.shortToString(bestMove) + " " + game.playerToPlay());
        System.out.println(winner + " Wins (" + bestStats.blackWins + " black vs " + bestStats.whiteWins + " whites) - " + bestRatio);
        System.out.println(bestGame);

        buffers.ret(bestGame);
    }

    public static void main(final String[] args)
    {
        TranspositionTable table = new TranspositionTable();
        final Game game = new Game((byte) BOARD_SIZE, (byte) 0, KOMI);

//        game.play(Move.parseToVal("Black E5"));
//        game.play(Move.parseToVal("White A1"));
//        game.play(Move.parseToVal("Black G5"));
//        game.play(Move.parseToVal("White A2"));
//        game.play(Move.parseToVal("Black C5"));

        table.play(game, 10_000_000);
    }


}
