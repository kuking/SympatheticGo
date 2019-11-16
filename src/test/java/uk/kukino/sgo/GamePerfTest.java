package uk.kukino.sgo;

import net.openhft.affinity.AffinityLock;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.IntStream;

public class GamePerfTest
{

    private long randomPlays(final int threadNo, final byte size, final int games)
    {
        Game gameOrig = new Game(size, (byte) 0, (byte) 55);
        Game game = new Game(size, (byte) 0, (byte) 55);

        final long startMs = System.currentTimeMillis();
        for (int i = 0; i < games; i++)
        {
            gameOrig.copyTo(game);
            game.randomPlay();
        }
        final long totalMs = System.currentTimeMillis() - startMs;
        final double playoutsSec = (games * 1000.0 / totalMs);
        System.out.println(String.format("   thrd. %2d: %6dms, %7d plys, %8.2f ply/s", threadNo, totalMs, games, playoutsSec));
        return totalMs;
    }

    @Test
    @Disabled
    public void game19perf()
    {
        randomPlays(0, (byte) 19, 10_000);
    }

    @Test
    @Disabled
    public void game9perf()
    {
        randomPlays(0, (byte) 9, 100_000);
    }

    private static long consume(ForkJoinTask<Long> job)
    {
        try
        {
            return job.get();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Long.MAX_VALUE;
    }

    public static void main(String[] args)
    {
        byte size = 9;
        int totalPlayouts = 2_000_000 / size;
        final int minThreads = 16;
        final int maxThreads = 16;
        for (int threads = minThreads; threads <= maxThreads; threads++)
        {
            System.out.println("===================================================");
            System.out.println(String.format("Running %d concurrent threads ...", threads));
            ForkJoinPool pool = new ForkJoinPool(threads);

            final long startMs = System.currentTimeMillis();
            final long totalMs = IntStream.range(0, threads).boxed().parallel()
                .map((t) -> pool.submit(() -> {
                    AffinityLock al = AffinityLock.acquireLock();
                    try
                    {
                        return new GamePerfTest().randomPlays(t, size, totalPlayouts);
                    }
                    finally
                    {
                        al.release();
                    }
                }))
                .mapToLong(GamePerfTest::consume)
                .sum();
            final long wallClockMs = System.currentTimeMillis() - startMs;

            System.out.println("------------- -------- ------------ ---------------");
            System.out.println(String.format("Agr. & avg.: %6dms, %7d plys, %8.2f ply/s",
                totalMs, totalPlayouts, (totalPlayouts * threads * 1000.0d) / totalMs));
            System.out.println(String.format("Effectively: %6dms, %7d plys, %8.2f ply/s",
                wallClockMs, totalPlayouts * threads, (totalPlayouts * threads * 1000.0d) / wallClockMs));
        }
        // 9x9 = 3500pls
        // 9x9 = 8539pls   19x19=1068pls
        // 9x9 = 10116pls
        // 9x9 = 11271pls  19x19=1908pls / 9x9x4th=29724.27  19x19x4th=5412.22  (after countIsZero / count split)

    /*
   thrd.  1:  28533ms,  222222 plys,  7788.25 ply/s
   thrd.  0:  28583ms,  222222 plys,  7774.62 ply/s
   thrd.  3:  28596ms,  222222 plys,  7771.09 ply/s
   thrd.  2:  28623ms,  222222 plys,  7763.76 ply/s
------------- -------- ------------ --------------
Agr. & avg.: 114335ms,  222222 plys,  7774.42 ply/s
Effectively:  28638ms,  888888 plys, 31038.76 ply/s

Agr. & avg.: 547855ms,  222222 plys,  6489.95 ply/s
Effectively:  34560ms, 3555552 plys, 102880.56 ply/s

     */
    }

}
