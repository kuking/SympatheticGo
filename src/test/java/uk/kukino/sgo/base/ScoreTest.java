package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

public class ScoreTest
{

    @Test
    public void typicalScores()
    {
        final int W_plus_12_5 = Score.wonWithScore(Color.WHITE, 12.5f);
        final int B_plus_0_5 = Score.wonWithScore(Color.BLACK, 0.5f);

        assertThat(Score.winner(W_plus_12_5)).isEqualTo(Color.WHITE);
        assertThat(Score.points(W_plus_12_5)).isEqualTo(12.5f);
        assertThat(Score.isResign(W_plus_12_5)).isFalse();
        assertThat(Score.isTimeout(W_plus_12_5)).isFalse();
        assertThat(Score.isDraw(W_plus_12_5)).isFalse();
        assertThat(Score.isUnknown(W_plus_12_5)).isFalse();

        assertThat(Score.winner(B_plus_0_5)).isEqualTo(Color.BLACK);
        assertThat(Score.points(B_plus_0_5)).isEqualTo(0.5f);
        assertThat(Score.isResign(B_plus_0_5)).isFalse();
        assertThat(Score.isTimeout(B_plus_0_5)).isFalse();
        assertThat(Score.isDraw(B_plus_0_5)).isFalse();
        assertThat(Score.isUnknown(B_plus_0_5)).isFalse();
    }

    @Test
    public void draw()
    {
        final int draw = Score.draw();

        assertThat(Score.winner(draw)).isEqualTo(Color.EMPTY);
        assertThat(Score.points(draw)).isNaN();
        assertThat(Score.isResign(draw)).isFalse();
        assertThat(Score.isDraw(draw)).isTrue();
        assertThat(Score.isUnknown(draw)).isFalse();
    }

    @Test
    public void resign()
    {
        final int W_R = Score.wonByResign(Color.WHITE);
        final int B_R = Score.wonByResign(Color.BLACK);

        assertThat(Score.winner(W_R)).isEqualTo(Color.WHITE);
        assertThat(Score.isDraw(W_R)).isFalse();
        assertThat(Score.isResign(W_R)).isTrue();
        assertThat(Score.isTimeout(W_R)).isFalse();
        assertThat(Score.points(W_R)).isPositiveInfinity();
        assertThat(Score.isUnknown(W_R)).isFalse();

        assertThat(Score.winner(B_R)).isEqualTo(Color.BLACK);
        assertThat(Score.isDraw(B_R)).isFalse();
        assertThat(Score.isResign(B_R)).isTrue();
        assertThat(Score.isTimeout(B_R)).isFalse();
        assertThat(Score.points(B_R)).isPositiveInfinity();
        assertThat(Score.isUnknown(B_R)).isFalse();
    }

    @Test
    public void unknown()
    {
        assertThat(Score.winner(Score.UNKNOWN)).isEqualTo(Color.EMPTY);
        assertThat(Score.isDraw(Score.UNKNOWN)).isFalse();
        assertThat(Score.isResign(Score.UNKNOWN)).isFalse();
        assertThat(Score.isTimeout(Score.UNKNOWN)).isFalse();
        assertThat(Score.points(Score.UNKNOWN)).isNaN();

        assertThat(Score.isUnknown(Score.UNKNOWN)).isTrue();
    }

    @Test
    public void timeout()
    {
        final int W_R = Score.wonByTime(Color.WHITE);
        final int B_R = Score.wonByTime(Color.BLACK);

        assertThat(Score.winner(W_R)).isEqualTo(Color.WHITE);
        assertThat(Score.isDraw(W_R)).isFalse();
        assertThat(Score.isResign(W_R)).isFalse();
        assertThat(Score.isTimeout(W_R)).isTrue();
        assertThat(Score.points(W_R)).isPositiveInfinity();

        assertThat(Score.winner(B_R)).isEqualTo(Color.BLACK);
        assertThat(Score.isDraw(B_R)).isFalse();
        assertThat(Score.isResign(B_R)).isFalse();
        assertThat(Score.isTimeout(B_R)).isTrue();
        assertThat(Score.points(B_R)).isPositiveInfinity();
    }

    @Test
    public void parsing()
    {
        assertThat(Score.parseToVal("DRAW")).isEqualTo(Score.draw());
        assertThat(Score.parseToVal("  DRAW  ")).isEqualTo(Score.draw());

        assertThat(Score.parseToVal("W+4.5")).isEqualTo(Score.wonWithScore(Color.WHITE, 4.5f));
        assertThat(Score.parseToVal("WHITE+4.5")).isEqualTo(Score.wonWithScore(Color.WHITE, 4.5f));
        assertThat(Score.parseToVal("WHITE+4.5")).isEqualTo(Score.wonWithScore(Color.WHITE, 4.5f));
        assertThat(Score.parseToVal(" WHITE+4.5 ")).isEqualTo(Score.wonWithScore(Color.WHITE, 4.5f));
        assertThat(Score.parseToVal("  W+4.5  ")).isEqualTo(Score.wonWithScore(Color.WHITE, 4.5f));
        assertThat(Score.parseToVal("B+5")).isEqualTo(Score.wonWithScore(Color.BLACK, 5f));
        assertThat(Score.parseToVal("Black+5")).isEqualTo(Score.wonWithScore(Color.BLACK, 5f));

        assertThat(Score.parseToVal("W+R")).isEqualTo(Score.wonByResign(Color.WHITE));
        assertThat(Score.parseToVal("W+Resign")).isEqualTo(Score.wonByResign(Color.WHITE));
        assertThat(Score.parseToVal("W+RESIgN")).isEqualTo(Score.wonByResign(Color.WHITE));
        assertThat(Score.parseToVal("B+R")).isEqualTo(Score.wonByResign(Color.BLACK));
        assertThat(Score.parseToVal("B+RESIGN")).isEqualTo(Score.wonByResign(Color.BLACK));

        assertThat(Score.parseToVal("W+Time")).isEqualTo(Score.wonByTime(Color.WHITE));
        assertThat(Score.parseToVal("W+TIME")).isEqualTo(Score.wonByTime(Color.WHITE));
        assertThat(Score.parseToVal("B+t")).isEqualTo(Score.wonByTime(Color.BLACK));
        assertThat(Score.parseToVal("b+TiMe")).isEqualTo(Score.wonByTime(Color.BLACK));

        assertThat(Score.parseToVal("W+4.5 WAT")).isEqualTo(Score.UNKNOWN);
        assertThat(Score.parseToVal("B+4.5 WAT")).isEqualTo(Score.UNKNOWN);
        assertThat(Score.parseToVal("B+Resign WAT")).isEqualTo(Score.UNKNOWN);
        assertThat(Score.parseToVal("B+Time WAT")).isEqualTo(Score.UNKNOWN);
        assertThat(Score.parseToVal("B+WAT")).isEqualTo(Score.UNKNOWN);
        assertThat(Score.parseToVal("  DRAW  WAT? ")).isEqualTo(Score.UNKNOWN);
        assertThat(Score.parseToVal("E+1")).isEqualTo(Score.UNKNOWN);
        assertThat(Score.parseToVal("Unknown")).isEqualTo(Score.UNKNOWN);
    }

    @Test
    public void outputsWithRoundTrips()
    {
        final StringBuilder sb = new StringBuilder();
        for (final String score : new String[] {"W+4.5", "B+5", "W+Resign", "B+Resign", "W+Time", "B+Time", "Draw"})
        {
            sb.delete(0, sb.length());
            Score.write(sb, Score.parseToVal(score));
            assertThat(sb.toString()).isEqualTo(score);
        }
    }

}
