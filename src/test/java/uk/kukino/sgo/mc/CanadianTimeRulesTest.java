package uk.kukino.sgo.mc;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class CanadianTimeRulesTest
{

    @Test
    public void builderCantBeReused()
    {
        CanadianTimeRules builder = CanadianTimeRules.builder();
        builder.build();
        try
        {
            builder.build();
        }
        catch (final IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("The builder has already created an TimeManager, can't create two.");
        }

    }

    @Test
    public void acceptsMoveAutomaticallyIfConfidenceIsEnough()
    {
        final TimeManager tm = CanadianTimeRules.builder()
            .enoughConfidence(0.9f)
            .build();

        tm.newGenerateMove();
        assertThat(tm.tick(1_000_000, 0.1f)).isFalse();
        assertThat(tm.tick(1_000_000, 0.2f)).isFalse();
        assertThat(tm.tick(1_000_000, 0.2f)).isFalse();
        assertThat(tm.tick(1_000_000, 0.2f)).isFalse();
        assertThat(tm.tick(1_000_000, 0.89f)).isFalse();
        assertThat(tm.tick(1_000_000, 0.91f)).isTrue();
    }

    @Test
    public void callingTickWithoutCallingNewGenerateMoveThrows()
    {
        final TimeManager tm = CanadianTimeRules.builder()
            .enoughConfidence(0.9f)
            .build();
        try
        {
            tm.tick(1, 0.1f);
            fail("This should have thrown.");
        }
        catch (final IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("newGenerateMove() should have been called first at the beginning of time.");
        }

        tm.newGenerateMove();
        assertThat(tm.tick(10, 0.1f)).isFalse();
        assertThat(tm.tick(10, 1f)).isTrue();
        assertThat(tm.tick(10, 1f)).isTrue();
        assertThat(tm.tick(0, 0f)).isTrue();
    }

    @Test
    public void onceTickSaysNewMoveItAlwaysSaysIt()
    {
        final TimeManager tm = CanadianTimeRules.builder()
            .enoughConfidence(0.9f)
            .build();

        tm.newGenerateMove();
        assertThat(tm.tick(0, 0.1f)).isFalse();
        assertThat(tm.tick(0, 1f)).isTrue();
        assertThat(tm.tick(0, 0f)).isTrue();
        assertThat(tm.tick(1, 0f)).isTrue();
        assertThat(tm.tick(0, 1f)).isTrue();
        tm.newGenerateMove();
        assertThat(tm.tick(0, 0f)).isFalse();
    }

    @Test
    public void maxPlys()
    {
        final TimeManager tm = CanadianTimeRules.builder()
            .maxMovePlys(100)
            .build();

        tm.newGenerateMove();
        assertThat(tm.tick(10, 0.1f)).isFalse();
        assertThat(tm.tick(90, 0.1f)).isFalse();
        assertThat(tm.tick(1, 0.1f)).isTrue();
    }

    @Test
    public void maxMoveSecs()
    {
        AtomicLong pseudoWatch = new AtomicLong(0);

        final TimeManager tm = CanadianTimeRules.builder()
            .maxMoveSecs(10)
            .timeSupplier(() -> pseudoWatch.addAndGet(1) * 1000)
            .build();

        tm.newGenerateMove();
        for (int i = 0; i < 9; i++)
        {
            assertThat(tm.tick(123, 0.1f)).isFalse();
        }
        assertThat(tm.tick(123, 0.2f)).isTrue();
    }


}