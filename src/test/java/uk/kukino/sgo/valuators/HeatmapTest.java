package uk.kukino.sgo.valuators;

import org.junit.jupiter.api.Test;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Move;

import static com.google.common.truth.Truth.assertThat;

public class HeatmapTest
{
    public static final float DELTA = 0.001f;
    Heatmap hm = new Heatmap((byte) 2, new float[] {1.2f, 0.1f, 4f, 3.14f, 50f});

    @Test
    public void min()
    {
        assertThat(hm.min()).isWithin(DELTA).of(0.1f);
    }

    @Test
    public void max()
    {
        assertThat(hm.max()).isWithin(DELTA).of(50f);
    }

    @Test
    public void percentile()
    {
        assertThat(hm.percentile(0)).isWithin(DELTA).of(0.1f);
        assertThat(hm.percentile(5)).isWithin(DELTA).of(0.1f);
        assertThat(hm.percentile(25)).isWithin(DELTA).of(1.2f);
        assertThat(hm.percentile(50)).isWithin(DELTA).of(3.14f);
        assertThat(hm.percentile(75)).isWithin(DELTA).of(4f);
        assertThat(hm.percentile(95)).isWithin(DELTA).of(50f);
        assertThat(hm.percentile(100)).isWithin(DELTA).of(50f);
    }

    @Test
    public void heat()
    {
        assertThat(hm.heat(Coord.A1)).isWithin(DELTA).of(1.2f);
        assertThat(hm.heat(Coord.B1)).isWithin(DELTA).of(0.1f);
        assertThat(hm.heat(Coord.A2)).isWithin(DELTA).of(4f);
        assertThat(hm.heat(Coord.B2)).isWithin(DELTA).of(3.14f);
        assertThat(hm.heat(Move.BLACK_PASS)).isWithin(DELTA).of(50f);
        assertThat(hm.heat(Move.WHITE_PASS)).isWithin(DELTA).of(50f);
    }

}
