package uk.kukino.sgo;

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
        int threads = 1;
        ForkJoinPool pool = new ForkJoinPool(threads);

        final long startMs = System.currentTimeMillis();
        final long totalMs = IntStream.range(0, threads).boxed().parallel()
            .map((t) -> pool.submit(() -> new GamePerfTest().randomPlays(t, size, totalPlayouts)))
            .mapToLong(GamePerfTest::consume)
            .sum();
        final long wallClockMs = System.currentTimeMillis() - startMs;

        System.out.println("------------- -------- ------------ --------------");
        System.out.println(String.format("Grand Total: %6dms, %7d plys, %8.2f ply/s",
            totalMs, totalPlayouts * threads, (totalPlayouts * threads * 1000.0d) / totalMs));
        System.out.println(String.format(" Wall clock: %6dms", wallClockMs));
        // 9x9 = 3500pls
        // 9x9 = 8539pls   19x19=1068pls
        // 9x9 = 10116pls
        // 9x9 = 11271pls  19x19=1908pls / 9x9x4th=29724.27  19x19x4th=5412.22  (after countIsZero / count split)

    /*
   thrd.  2: 145873ms, 1111111 plys,  7616.98 ply/s
   thrd.  0: 145947ms, 1111111 plys,  7613.11 ply/s
   thrd.  1: 145961ms, 1111111 plys,  7612.38 ply/s
   thrd.  3: 146004ms, 1111111 plys,  7610.14 ply/s
------------- -------- ------------ --------------
Grand Total: 583785ms, 4444444 plys,  7613.15 ply/s
 Wall clock: 146019ms

   thrd.  0:   9805ms,  111111 plys, 11332.08 ply/s
------------- -------- ------------ --------------
Grand Total:   9805ms,  111111 plys, 11332.08 ply/s
 Wall clock:   9829ms


     */
    }

}
