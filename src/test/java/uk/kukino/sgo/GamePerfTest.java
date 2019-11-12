package uk.kukino.sgo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GamePerfTest
{

    private void randomPlays(final byte size, final int games)
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
        System.out.println("Total: " + totalMs + "ms  " + (games * 1000 / totalMs) + "pls");
    }

    @Test
    @Disabled
    public void game19perf()
    {
        randomPlays((byte) 19, 10_000);
    }

    @Test
    @Disabled
    public void game9perf()
    {
        randomPlays((byte) 9, 100_000);
    }

    public static void main(String[] args)
    {
       new GamePerfTest().randomPlays((byte)9, 100_000);
    }


}
