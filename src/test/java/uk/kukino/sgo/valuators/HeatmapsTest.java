package uk.kukino.sgo.valuators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static com.google.common.truth.Truth.assertThat;

public class HeatmapsTest
{
    final Heatmap hm1 = new Heatmap((byte) 1, new float[] {1f, 2f, 3f});
    final Heatmap hm2 = new Heatmap((byte) 2, new float[] {10f, 20f, 30f});
    Heatmaps hms;

    @BeforeEach
    public void before()
    {
        hms = new Heatmaps();
    }

    @Test
    public void ItHoldsItsValuesTrivial()
    {
        assertThat(hms.get(1)).isNull();
        assertThat(hms.get(2)).isNull();
        assertThat(hms.keys()).isEmpty();

        hms.put(1, hm1);
        assertThat(hms.get(1)).isEqualTo(hm1);
        assertThat(hms.get(2)).isNull();
        assertThat(hms.keys()).containsExactly(1l);
        assertThat(hms.values()).containsExactly(hm1);

        hms.put(2, hm2);
        assertThat(hms.get(1)).isEqualTo(hm1);
        assertThat(hms.get(2)).isEqualTo(hm2);
        assertThat(hms.keys()).containsExactly(1l, 2l);
        assertThat(hms.values()).containsExactly(hm1, hm2);
    }


    @Test
    public void itRoundTripSerialises() throws IOException, ClassNotFoundException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        hms.put(1, hm1);
        hms.put(2, hm2);

        oos.writeObject(hms);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);

        Heatmaps heatmaps = (Heatmaps) ois.readObject();
        assertThat(heatmaps).isNotSameInstanceAs(hms);
        assertThat(heatmaps.get(1)).isNotSameInstanceAs(hm1);
        assertThat(heatmaps.get(2)).isNotSameInstanceAs(hm2);

        assertThat(heatmaps.get(1).getCopy()).isEqualTo(new float[] {1f, 2f, 3f});
        assertThat(heatmaps.get(1).size()).isEqualTo(1);
        assertThat(heatmaps.get(2).getCopy()).isEqualTo(new float[] {10f, 20f, 30f});
        assertThat(heatmaps.get(2).size()).isEqualTo(2);
    }


}
