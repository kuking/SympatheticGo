package uk.kukino.sgo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GamePerfTest
{


    @Test
    @Disabled
    public void game19perf()
    {
        new PerfStats().randomPlays(0, (byte) 19, 10_000);
    }

    @Test
    @Disabled
    public void game9perf()
    {
        new PerfStats().randomPlays(0, (byte) 9, 100_000);
    }


}
