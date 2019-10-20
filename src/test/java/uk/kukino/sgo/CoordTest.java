package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoordTest {

    byte size = (byte) 19;
    short[] res = new short[4];
    Coord coord = new Coord();

    @Test
    public void simplest() {
        short val = Coord.XY((byte) 2, (byte) 3);
        assertThat(Coord.x(val), equalTo((byte) 2));
        assertThat(Coord.y(val), equalTo((byte) 3));
        assertTrue(Coord.isValid(val));

        coord = new Coord();
        coord.assignXY((byte) 5, (byte) 6);
        assertThat(coord.x(), equalTo((byte) 5));
        assertThat(coord.y(), equalTo((byte) 6));
        assertTrue(coord.isValid());
    }

    @Test
    public void adjacentCentre() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 5, (byte) 5), size), equalTo((byte) 4));
        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 5, (byte) 4),
                Coord.XY((byte) 6, (byte) 5),
                Coord.XY((byte) 5, (byte) 6),
                Coord.XY((byte) 4, (byte) 5)
        }));
    }

    @Test
    public void adjacentOneCellBoard() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 1, (byte) 1), (byte) 1), equalTo((byte) 0));
        assertThat(res, equalTo(new short[]{
                Coord.INVALID, Coord.INVALID, Coord.INVALID, Coord.INVALID}));
    }

    @Test
    public void adjacentToTopLeftCorner() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 1, (byte) 1), size), equalTo((byte) 2));
        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 2, (byte) 1),
                Coord.XY((byte) 1, (byte) 2),
                Coord.INVALID,
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentToTopRightCorner() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 19, (byte) 1), size), equalTo((byte) 2));
        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 19, (byte) 2),
                Coord.XY((byte) 18, (byte) 1),
                Coord.INVALID,
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentToBottomRightCorner() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 19, (byte) 19), size), equalTo((byte) 2));
        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 19, (byte) 18),
                Coord.XY((byte) 18, (byte) 19),
                Coord.INVALID,
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentToBottomLeftCorner() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 1, (byte) 19), size), equalTo((byte) 2));

        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 1, (byte) 18),
                Coord.XY((byte) 2, (byte) 19),
                Coord.INVALID,
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentTopSide() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 9, (byte) 1), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 10, (byte) 1),
                Coord.XY((byte) 9, (byte) 2),
                Coord.XY((byte) 8, (byte) 1),
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentRightSide() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 19, (byte) 9), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 19, (byte) 8),
                Coord.XY((byte) 19, (byte) 10),
                Coord.XY((byte) 18, (byte) 9),
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentBottomSide() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 9, (byte) 19), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 9, (byte) 18),
                Coord.XY((byte) 10, (byte) 19),
                Coord.XY((byte) 8, (byte) 19),
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentLeftSide() {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 1, (byte) 9), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 1, (byte) 8),
                Coord.XY((byte) 2, (byte) 9),
                Coord.XY((byte) 1, (byte) 10),
                Coord.INVALID
        }));
    }

    @Test
    public void adjacentCentreWithObject() {
        coord.assignXY((byte) 5, (byte) 5);
        assertThat(coord.adjacents(res, size), equalTo((byte) 4));
        assertThat(res, equalTo(new short[]{
                Coord.XY((byte) 5, (byte) 4),
                Coord.XY((byte) 6, (byte) 5),
                Coord.XY((byte) 5, (byte) 6),
                Coord.XY((byte) 4, (byte) 5)
        }));
    }

    @Test
    public void parse() {
        assertTrue(coord.parse("Z126"));
        assertThat(coord.x(), equalTo((byte) 25));
        assertThat(coord.y(), equalTo((byte) 126));

        assertTrue(coord.parse("Aa100"));
        assertThat(coord.x(), equalTo((byte) 27));
        assertThat(coord.y(), equalTo((byte) 100));

        assertTrue(coord.parse("j8"));
        assertThat(coord.x(), equalTo((byte) 9));
        assertThat(coord.y(), equalTo((byte) 8));
    }

    @Test
    public void parseStatic() {
        assertThat(Coord.parseToVal("bz126"), equalTo(Coord.XY((byte) (26*2 + 25), (byte) 126)));
        assertThat(Coord.parseToVal("A1"), equalTo(Coord.XY((byte) 1, (byte) 1)));
        assertThat(Coord.parseToVal("b2"), equalTo(Coord.XY((byte) 2, (byte) 2)));
    }

    @Test
    public void parserCanTrim() {
        assertTrue(coord.parse("   D10   "));
        assertThat(coord.x(), equalTo((byte) 4));
        assertThat(coord.y(), equalTo((byte) 10));
    }

    @Test
    public void parserHandleInvalids() {
        assertFalse(coord.parse("aasdasd"));
        assertFalse(coord.isValid());

        assertFalse(coord.parse("aas dasd"));
        assertFalse(coord.isValid());

        assertFalse(coord.parse("123A"));
        assertFalse(coord.isValid());

        assertFalse(coord.parse("AA1234"));
        assertFalse(coord.isValid());

        assertFalse(coord.parse("AAA1"));
        assertFalse(coord.isValid());

        assertFalse(coord.parse("123"));
        assertFalse(coord.isValid());

        assertFalse(coord.parse(" A 1 "));
        assertFalse(coord.isValid());

        assertFalse(coord.parse(""));
        assertFalse(coord.isValid());

        assertFalse(coord.parse("   "));
        assertFalse(coord.isValid());

        assertFalse(coord.parse(null));
        assertFalse(coord.isValid());
    }
}
