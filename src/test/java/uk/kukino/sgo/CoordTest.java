package uk.kukino.sgo;

import net.openhft.chronicle.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoordTest
{

    byte size = (byte) 19;
    short[] res = new short[4];

    Bytes<ByteBuffer> adjs = Bytes.elasticByteBuffer(2 * 10);

    @Test
    public void simplest()
    {
        short val = Coord.XY((byte) 2, (byte) 3);
        assertThat(Coord.X(val), equalTo((byte) 2));
        assertThat(Coord.Y(val), equalTo((byte) 3));
        assertTrue(Coord.isValid(val));
    }

    @Test
    public void adjacentCentre()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 5, (byte) 5), size), equalTo((byte) 4));
        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 5, (byte) 4),
            Coord.XY((byte) 6, (byte) 5),
            Coord.XY((byte) 5, (byte) 6),
            Coord.XY((byte) 4, (byte) 5)
        }));
    }

    @Test
    public void adjacentOneCellBoard()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 0, (byte) 0), (byte) 1), equalTo((byte) 0));
        assertThat(res, equalTo(new short[] {0, 0, 0, 0}));
    }

    @Test
    public void adjacentToTopLeftCorner()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 0, (byte) 0), size), equalTo((byte) 2));
        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 1, (byte) 0),
            Coord.XY((byte) 0, (byte) 1),
            0,
            0
        }));
    }

    @Test
    public void adjacentToTopRightCorner()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 18, (byte) 0), size), equalTo((byte) 2));
        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 18, (byte) 1),
            Coord.XY((byte) 17, (byte) 0),
            0,
            0
        }));
    }

    @Test
    public void adjacentToTopRightCorner_Bytes()
    {
        Coord.adjacents(adjs, Coord.XY((byte) 18, (byte) 0), size);
        assertFalse(adjs.isEmpty());
        assertThat(adjs.readShort(), equalTo(Coord.XY((byte) 18, (byte) 1)));
        assertThat(adjs.readShort(), equalTo(Coord.XY((byte) 17, (byte) 0)));
        assertTrue(adjs.isEmpty());
    }

    @Test
    public void adjacentToBottomRightCorner()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 19, (byte) 19), size), equalTo((byte) 2));
        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 19, (byte) 18),
            Coord.XY((byte) 18, (byte) 19),
            0,
            0
        }));
    }

    @Test
    public void adjacentToBottomRightCorner_Bytes()
    {
        Coord.adjacents(adjs, Coord.XY((byte) 19, (byte) 19), size);
        assertFalse(adjs.isEmpty());
        assertThat(adjs.readShort(), equalTo(Coord.XY((byte) 19, (byte) 18)));
        assertThat(adjs.readShort(), equalTo(Coord.XY((byte) 18, (byte) 19)));
        assertTrue(adjs.isEmpty());
    }

    @Test
    public void adjacentToBottomLeftCorner()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 0, (byte) 18), size), equalTo((byte) 2));

        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 0, (byte) 17),
            Coord.XY((byte) 1, (byte) 18),
            0,
            0
        }));
    }

    @Test
    public void adjacentTopSide()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 8, (byte) 0), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 9, (byte) 0),
            Coord.XY((byte) 8, (byte) 1),
            Coord.XY((byte) 7, (byte) 0),
            0
        }));
    }

    @Test
    public void adjacentRightSide()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 19, (byte) 9), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 19, (byte) 8),
            Coord.XY((byte) 19, (byte) 10),
            Coord.XY((byte) 18, (byte) 9),
            0
        }));
    }

    @Test
    public void adjacentBottomSide()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 9, (byte) 19), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 9, (byte) 18),
            Coord.XY((byte) 10, (byte) 19),
            Coord.XY((byte) 8, (byte) 19),
            0
        }));
    }

    @Test
    public void adjacentLeftSide()
    {
        assertThat(Coord.adjacents(res, Coord.XY((byte) 0, (byte) 8), size), equalTo((byte) 3));

        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 0, (byte) 7),
            Coord.XY((byte) 1, (byte) 8),
            Coord.XY((byte) 0, (byte) 9),
            0
        }));
    }

    @Test
    public void adjacentCentreWithObject()
    {
        final short coord = Coord.XY((byte) 5, (byte) 5);
        assertThat(Coord.adjacents(res, coord, size), equalTo((byte) 4));
        assertThat(res, equalTo(new short[] {
            Coord.XY((byte) 5, (byte) 4),
            Coord.XY((byte) 6, (byte) 5),
            Coord.XY((byte) 5, (byte) 6),
            Coord.XY((byte) 4, (byte) 5)
        }));
    }

    @Test
    public void parse()
    {
        short coord = Coord.parseToVal("Z126");
        assertThat(Coord.X(coord), equalTo((byte) 24));
        assertThat(Coord.Y(coord), equalTo((byte) 125));

        coord = Coord.parseToVal("Aa100");
        assertThat(Coord.X(coord), equalTo((byte) 26));
        assertThat(Coord.Y(coord), equalTo((byte) 99));

        coord = Coord.parseToVal("j8");
        assertThat(Coord.X(coord), equalTo((byte) 8));
        assertThat(Coord.Y(coord), equalTo((byte) 7));
    }

    @Test
    public void parseStatic()
    {
        assertThat(Coord.parseToVal("bz126"), equalTo(Coord.XY((byte) (26 * 2 + 24), (byte) 125)));
        assertThat(Coord.parseToVal("A1"), equalTo(Coord.XY((byte) 0, (byte) 0)));
        assertThat(Coord.parseToVal("b2"), equalTo(Coord.XY((byte) 1, (byte) 1)));
    }

    @Test
    public void shortToString()
    {
        assertThat(Coord.shortToString(Coord.parseToVal("A1")), equalTo("A1"));
        assertThat(Coord.shortToString(Coord.parseToVal("B2")), equalTo("B2"));
        assertThat(Coord.shortToString(Coord.parseToVal("C10")), equalTo("C10"));
        assertThat(Coord.shortToString(Coord.parseToVal("Z20")), equalTo("Z20"));
//        assertThat(Coord.shortToString(Coord.parseToVal("AA100")), equalTo("AA100")); //FIXME
    }

    @Test
    public void parserCanTrim()
    {
        final short parsed = Coord.parseToVal("   D10   ");
        assertTrue(Coord.isValid(parsed));
        assertThat(Coord.X(parsed), equalTo((byte) 3));
        assertThat(Coord.Y(parsed), equalTo((byte) 9));
    }

    @Test
    public void parserHandleInvalids()
    {
        assertFalse(Coord.isValid(Coord.parseToVal("aasdasd")));
        assertFalse(Coord.isValid(Coord.parseToVal("aas dasd")));
        assertFalse(Coord.isValid(Coord.parseToVal("123A")));
        assertFalse(Coord.isValid(Coord.parseToVal("AA1234")));
        assertFalse(Coord.isValid(Coord.parseToVal("AAA1")));
        assertFalse(Coord.isValid(Coord.parseToVal("123")));
        assertFalse(Coord.isValid(Coord.parseToVal(" A 1 ")));
        assertFalse(Coord.isValid(Coord.parseToVal("")));
        assertFalse(Coord.isValid(Coord.parseToVal("   ")));
        assertFalse(Coord.isValid(Coord.parseToVal(null)));
    }

    @Test
    public void toStringIsParsed()
    {
        for (byte x = 0; x < 25; x++)
        {
            for (byte y = 0; y < 25; y++)
            {
                final short original = Coord.XY(x, y);
                final String toString = Coord.shortToString(original);
                final short parsed = Coord.parseToVal(toString);
                assertThat(Coord.X(parsed), equalTo(x));
                assertThat(Coord.Y(parsed), equalTo(y));
            }
        }
    }

}
