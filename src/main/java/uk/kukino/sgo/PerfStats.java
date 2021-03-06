package uk.kukino.sgo;

import net.openhft.affinity.AffinityLock;
import uk.kukino.sgo.base.Game;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.IntStream;

public class PerfStats
{

    public long randomPlays(final int threadNo, final byte size, final int games)
    {
        final Game gameOrig = new Game(size, (byte) 0, (byte) 55);
        final Game game = new Game(size, (byte) 0, (byte) 55);

        final long startMs = System.currentTimeMillis();
        for (int i = 0; i < games; i++)
        {
            gameOrig.copyTo(game);
            game.finishRandomly();
        }
        final long totalMs = System.currentTimeMillis() - startMs;
        final double playoutsSec = (games * 1000.0 / totalMs);
        System.out.println(String.format("   thrd. %2d: %6dms, %7d plys, %9.2f ply/s, %3.3fμs/ply",
            threadNo, totalMs, games, playoutsSec, (double) 1_000_000 * totalMs / 1000d / games));

        return totalMs;
    }

    private static long consume(final ForkJoinTask<Long> job)
    {
        try
        {
            return job.get();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return Long.MAX_VALUE;
    }

    public static void main(final String[] args)
    {
        final byte size = 9;
        final int totalPlayouts = 2_000_000 / size;
        final int minThreads = 1;
        final int maxThreads = Runtime.getRuntime().availableProcessors();
        for (int threads = minThreads; threads <= maxThreads; threads++)
        {
            System.out.println("===================================================================");
            System.out.println(String.format("Running %d concurrent threads with %dx%d games ...", threads, size, size));
            final ForkJoinPool pool = new ForkJoinPool(threads);

            final long startMs = System.currentTimeMillis();
            final long totalMs = IntStream.range(0, threads).boxed().parallel()
                .map((t) -> pool.submit(() ->
                {
                    final AffinityLock al = AffinityLock.acquireLock();
                    try
                    {
                        return new PerfStats().randomPlays(t, size, totalPlayouts);
                    }
                    finally
                    {
                        al.release();
                    }
                }))
                .mapToLong(PerfStats::consume)
                .sum();
            final long wallClockMs = System.currentTimeMillis() - startMs;

            System.out.println("------------- -------- ------------ ----------------- -------------");

            System.out.println(String.format("Agr. & avg.: %6dms, %7d plys, %9.2f ply/s, %3.3fμs/ply",
                totalMs, totalPlayouts,
                (totalPlayouts * threads * 1000.0d) / totalMs,
                (double) 1_000_000 * totalMs / 1000d / (totalPlayouts * threads)));

            System.out.println(String.format("Effectively: %6dms, %7d plys, %9.2f ply/s, %3.3fμs/ply",
                wallClockMs, totalPlayouts * threads,
                (totalPlayouts * threads * 1000.0d) / wallClockMs,
                (double) 1_000_000 * wallClockMs / 1000d / (totalPlayouts * threads)
            ));
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

Agr. & avg.: 510096ms,  222222 plys,   6970.36 ply/s, 143.465μs/ply
Effectively:  32740ms, 3555552 plys, 108599.63 ply/s, 9.208μs/ply

i7 after SplittableRandom & Move pass bugfix
Agr. & avg.: 154625ms,  222222 plys,  11497.34 ply/s, 86.977μs/ply
Effectively:  19497ms, 1777776 plys,  91182.03 ply/s, 10.967μs/ply

Ryzer 3800
Agr. & avg.: 397514ms,  222222 plys,   8944.47 ply/s, 111.801μs/ply
Effectively:  25055ms, 3555552 plys, 141909.88 ply/s, 7.047μs/ply

     */
    }

}
