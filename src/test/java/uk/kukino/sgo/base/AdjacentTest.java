package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;


public class AdjacentTest
{

    private byte boardSize = 9;
    private short c_E5 = Coord.parseToVal("E5");

    @Test
    public void size1Board()
    {
        short coord = Coord.parseToVal("A1");
        int adjs = Adjacent.asVal(coord, (byte) 1);

        assertThat(Adjacent.iterHasNext(adjs)).isFalse();
        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.INVALID);
    }

    @Test
    public void happyPath()
    {
        int adjs = Adjacent.asVal(c_E5, boardSize);

        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.parseToVal("E6"));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.parseToVal("F5"));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.parseToVal("E4"));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.parseToVal("D5"));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.INVALID);
    }

    @Test
    public void iteratesRightAmountAndStaysThere()
    {
        int adjs = Adjacent.asVal(c_E5, boardSize);

        int count = 0;
        while (Adjacent.iterHasNext(adjs))
        {
            count++;
            adjs = Adjacent.iterMoveNext(adjs);
        }

        assertThat(count).isEqualTo(4);
        assertThat(Adjacent.iterMoveNext(adjs)).isEqualTo(adjs); // and does not changes anymore
    }

    @Test
    public void itCanBeReset()
    {
        int adjs = Adjacent.asVal(c_E5, boardSize);

        adjs = assertPositions(adjs, "E6", "F5", "E4", "D5");
        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.INVALID);

        adjs = Adjacent.iterReset(adjs);
        adjs = assertPositions(adjs, "E6", "F5", "E4", "D5");
        assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.INVALID);
    }

    @Test
    public void itCanBeInteractivelyUnset()
    {
        int adjs = Adjacent.asVal(c_E5, boardSize);
        assertPositions(adjs, "E6", "F5", "E4", "D5");

        adjs = Adjacent.iterReset(adjs);
        adjs = Adjacent.iterUnset(adjs);
        adjs = Adjacent.iterMoveNext(adjs);
        adjs = Adjacent.iterMoveNext(adjs);
        adjs = Adjacent.iterMoveNext(adjs);
        adjs = Adjacent.iterUnset(adjs);

        adjs = Adjacent.iterReset(adjs);
        assertPositions(adjs, "F5", "E4");
    }

    @Test
    public void unsettingDoNotMoveIteratorPosition()
    {
        // we don't want to do two operations in one
        int adjs = Adjacent.asVal(c_E5, boardSize);
        assertThat(Adjacent.valToStr(adjs)).isEqualTo("{E5 NESW #0->E6}");

        adjs = Adjacent.iterUnset(adjs);
        assertThat(Adjacent.valToStr(adjs)).isEqualTo("{E5 .ESW #0->E6}");
    }

    @Test
    public void topLeft()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("A9"), boardSize);
        assertPositions(adjs, "B9", "A8");
    }

    @Test
    public void top()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("E9"), boardSize);
        assertPositions(adjs, "F9", "E8", "D9");
    }

    @Test
    public void topRight()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("J9"), boardSize);
        assertPositions(adjs, "J8", "H9");
    }

    @Test
    public void right()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("J5"), boardSize);
        assertPositions(adjs, "J6", "J4", "H5");
    }

    @Test
    public void bottomRight()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("J1"), boardSize);
        assertPositions(adjs, "J2", "H1");
    }

    @Test
    public void bottom()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("E1"), boardSize);
        assertPositions(adjs, "E2", "F1", "D1");
    }

    @Test
    public void bottomLeft()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("A1"), boardSize);
        assertPositions(adjs, "A2", "B1");
    }

    @Test
    public void left()
    {
        int adjs = Adjacent.asVal(Coord.parseToVal("A5"), boardSize);
        assertPositions(adjs, "A6", "B5", "A4");
    }

    @Test
    public void canonicalEmptyIterator()
    {
        assertThat(Adjacent.iterHasNext(Adjacent.EMPTY_ITERATOR)).isFalse();
        assertThat(Adjacent.iterPosition(Adjacent.EMPTY_ITERATOR)).isEqualTo(Coord.INVALID);
    }
    // util

    public static int assertPositions(int adjs, String... positions)
    {
        int posIdx = 0;
        while (Adjacent.iterHasNext(adjs))
        {
//            System.out.println(Adjacent.valToStr(adjs) + " -> " + Coord.shortToString(Adjacent.iterPosition(adjs)));
            assertThat(positions.length).isGreaterThan(posIdx);
            assertThat(Adjacent.iterPosition(adjs)).isEqualTo(Coord.parseToVal(positions[posIdx++]));
            adjs = Adjacent.iterMoveNext(adjs);
        }
        assertThat(positions.length).isEqualTo(posIdx);
        return adjs;
    }


}
