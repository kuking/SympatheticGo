package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class AdjacentTest
{

    private byte boardSize = 9;
    private short c_E5 = Coord.parseToVal("E5");

    @Test
    public void size1Board()
    {
        short coord = Coord.parseToVal("A1");
        long adjs = Adjacent.asVal(coord, (byte) 1);

        assertFalse(Adjacent.iterHasNext(adjs));
        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.INVALID));
    }

    @Test
    public void happyPath()
    {
        long adjs = Adjacent.asVal(c_E5, boardSize);

        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.parseToVal("E6")));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.parseToVal("F5")));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.parseToVal("E4")));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.parseToVal("D5")));

        adjs = Adjacent.iterMoveNext(adjs);
        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.INVALID));
    }

    @Test
    public void iteratesRightAmountAndStaysThere()
    {
        long adjs = Adjacent.asVal(c_E5, boardSize);

        int count = 0;
        while (Adjacent.iterHasNext(adjs))
        {
            count++;
            adjs = Adjacent.iterMoveNext(adjs);
        }

        assertThat(count, equalTo(4));
        assertThat(Adjacent.iterMoveNext(adjs), equalTo(adjs)); // and does not changes anymore
    }

    @Test
    public void itCanBeReset()
    {
        long adjs = Adjacent.asVal(c_E5, boardSize);

        adjs = assertPositions(adjs, "E6", "F5", "E4", "D5");
        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.INVALID));

        adjs = Adjacent.iterReset(adjs);
        adjs = assertPositions(adjs, "E6", "F5", "E4", "D5");
        assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.INVALID));
    }

    @Test
    public void itCanBeInteractivelyUnset()
    {
        long adjs = Adjacent.asVal(c_E5, boardSize);
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
        long adjs = Adjacent.asVal(c_E5, boardSize);
        assertThat(Adjacent.valToStr(adjs), equalTo("{E5 NESW #0->E6}"));

        adjs = Adjacent.iterUnset(adjs);
        assertThat(Adjacent.valToStr(adjs), equalTo("{E5 .ESW #0->E6}"));
    }

    @Test
    public void topLeft()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("A9"), boardSize);
        assertPositions(adjs, "B9", "A8");
    }

    @Test
    public void top()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("E9"), boardSize);
        assertPositions(adjs, "F9", "E8", "D9");
    }

    @Test
    public void topRight()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("J9"), boardSize);
        assertPositions(adjs, "J8", "H9");
    }

    @Test
    public void right()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("J5"), boardSize);
        assertPositions(adjs, "J6", "J4", "H5");
    }

    @Test
    public void bottomRight()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("J1"), boardSize);
        assertPositions(adjs, "J2", "H1");
    }

    @Test
    public void bottom()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("E1"), boardSize);
        assertPositions(adjs, "E2", "F1", "D1");
    }

    @Test
    public void bottomLeft()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("A1"), boardSize);
        assertPositions(adjs, "A2", "B1");
    }

    @Test
    public void left()
    {
        long adjs = Adjacent.asVal(Coord.parseToVal("A5"), boardSize);
        assertPositions(adjs, "A6", "B5", "A4");
    }

    @Test
    public void canonicalEmptyIterator()
    {
        assertFalse(Adjacent.iterHasNext(Adjacent.EMPTY_ITERATOR));
        assertThat(Adjacent.iterPosition(Adjacent.EMPTY_ITERATOR), equalTo(Coord.INVALID));
    }
    // util

    public static long assertPositions(long adjs, String... positions)
    {
        int posIdx = 0;
        while (Adjacent.iterHasNext(adjs))
        {
//            System.out.println(Adjacent.valToStr(adjs) + " -> " + Coord.shortToString(Adjacent.iterPosition(adjs)));
            assertThat("Iterator as more values than expected.", positions.length, greaterThan(posIdx));
            assertThat(Adjacent.iterPosition(adjs), equalTo(Coord.parseToVal(positions[posIdx++])));
            adjs = Adjacent.iterMoveNext(adjs);
        }
        assertThat(positions.length, equalTo(posIdx));
        return adjs;
    }


}
