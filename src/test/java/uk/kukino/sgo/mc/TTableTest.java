package uk.kukino.sgo.mc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;

import static com.google.common.truth.Truth.assertThat;

public class TTableTest
{

    public static final int ONE_HASH = 123;
    public static final int ANOTHER_HASH = 234;

    private TTable underTest;

    @BeforeEach
    public void beforeEach()
    {
        underTest = new TTable((byte) 19, (byte) 5, (byte) 5);
    }

    @Test
    public void uninitializedHash()
    {
        assertThat(underTest.playoutsFor(ONE_HASH)).isEqualTo(0);
        assertThat(cutOnFirstInvalid(underTest.topsFor(ONE_HASH, 10, Color.WHITE))).isEmpty();
//        assertThat(underTest.uct(ONE_HASH, 4)).asList().containsExactly(Coord.A1, Coord.A2, Coord.A3, Coord.A4);
    }

    @Test
    public void simplest()
    {
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);

        assertThat(underTest.playoutsFor(ONE_HASH)).isEqualTo(1);
        assertThat(cutOnFirstInvalid(underTest.topsFor(ONE_HASH, 10, Color.WHITE))).asList().containsExactly(Coord.D4);
        //FIXME: uct?
    }

    @Test
    public void almostSimplest()
    {
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.D4, Color.BLACK);
        underTest.account(ONE_HASH, Coord.D1, Color.WHITE);
        underTest.account(ONE_HASH, Coord.A1, Color.WHITE);

        assertThat(underTest.playoutsFor(ONE_HASH)).isEqualTo(4);
        assertThat(cutOnFirstInvalid(underTest.topsFor(ONE_HASH, 10, Color.WHITE)))
            .asList().containsExactly(Coord.A1, Coord.D1, Coord.D4);
    }

    @Test
    public void bigOne()
    {
        for (byte x = 0; x < 19; x++)
        {
            for (byte y = 0; y < 19; y++)
            {
                underTest.account(ONE_HASH, Coord.XY(x, y), Color.WHITE);
                underTest.account(ONE_HASH, Coord.XY(x, y), Color.WHITE);
                underTest.account(ONE_HASH, Coord.XY(x, y), Color.BLACK);
                underTest.account(ANOTHER_HASH, Coord.XY(x, y), Color.BLACK);
            }
        }
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.B2, Color.BLACK);
        underTest.account(ANOTHER_HASH, Coord.D4, Color.WHITE);

        assertThat(underTest.playoutsFor(ONE_HASH)).isEqualTo(19 * 19 * 3 + 5);
        assertThat(cutOnFirstInvalid(underTest.topsFor(ONE_HASH, 5, Color.WHITE))).asList()
            .containsExactly(Coord.D4, Coord.A1, Coord.B1, Coord.C1, Coord.D1);

        assertThat(cutOnFirstInvalid(underTest.topsFor(ONE_HASH, 5, Color.BLACK))).asList()
            .containsExactly(Coord.B2, Coord.A1, Coord.B1, Coord.C1, Coord.D1);


    }

    @Test
    public void bigOne2()
    {
        for (byte x = 0; x < 19; x++)
        {
            for (byte y = 0; y < 19; y++)
            {
                underTest.account(ONE_HASH, Coord.XY(x, y), Color.WHITE);
                underTest.account(ONE_HASH, Coord.XY(x, y), Color.WHITE);
                underTest.account(ONE_HASH, Coord.XY(x, y), Color.BLACK);
            }
        }
        for (int i = 0; i < 100; i++)
        {
            underTest.account(ONE_HASH, Coord.B2, Color.WHITE);
        }
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);

        assertThat(underTest.playoutsFor(ONE_HASH)).isEqualTo(19 * 19 * 3 + 100 + 3);
        assertThat(cutOnFirstInvalid(underTest.topsFor(ONE_HASH, 6, Color.WHITE))).asList()
            .containsExactly(Coord.B2, Coord.D4, Coord.A1, Coord.B1, Coord.C1, Coord.D1);
    }


    @Test
    public void differentTablesDoesNotOverlap()
    {
        underTest.account(ONE_HASH, Coord.D4, Color.WHITE);
        underTest.account(ONE_HASH, Coord.D4, Color.BLACK);
        underTest.account(ONE_HASH, Coord.D1, Color.WHITE);
        underTest.account(ONE_HASH, Coord.A1, Color.WHITE);
        underTest.account(ANOTHER_HASH, Coord.D4, Color.BLACK);
        underTest.account(ANOTHER_HASH, Coord.D4, Color.WHITE);
        underTest.account(ANOTHER_HASH, Coord.D1, Color.BLACK);

        assertThat(underTest.playoutsFor(ONE_HASH)).isEqualTo(4);
        assertThat(cutOnFirstInvalid(underTest.topsFor(ONE_HASH, 10, Color.WHITE)))
            .asList().containsExactly(Coord.A1, Coord.D1, Coord.D4);
    }

    short[] cutOnFirstInvalid(final short[] bigList)
    {
        int size = 0;
        while (bigList[size] != Coord.INVALID)
        {
            size++;
        }
        final short[] result = new short[size];
        System.arraycopy(bigList, 0, result, 0, size);
        return result;
    }

    @Test
    public void newTablesAreAlwaysEmptied()
    {
        underTest = new TTable((byte) 19, (byte) 2, (byte) 2); // smaller so less buffers to cycle
        for (int i = 0; i < 2000; i++)
        {
            assertThat(underTest.topsFor(i, 1, Color.WHITE)[0]).isEqualTo(Coord.INVALID);
            underTest.account(i, Coord.A1, Color.WHITE);
        }
    }

}

