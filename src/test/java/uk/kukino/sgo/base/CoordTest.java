package uk.kukino.sgo.base;

import net.openhft.chronicle.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.kukino.sgo.util.TUtils.cutOnFirstInvalid;


public class CoordTest
{

    byte size = (byte) 19;
    short[] res = new short[4];

    Bytes<ByteBuffer> adjs = Bytes.elasticByteBuffer(2 * 10);

    @Test
    public void simplest()
    {
        short val = Coord.XY((byte) 2, (byte) 3);
        assertThat(Coord.X(val)).isEqualTo((byte) 2);
        assertThat(Coord.Y(val)).isEqualTo((byte) 3);
        assertThat(Coord.isValid(val)).isTrue();
    }

    @Test
    public void parse()
    {
        short coord = Coord.parseToVal("Z126");
        assertThat(Coord.X(coord)).isEqualTo((byte) 24);
        assertThat(Coord.Y(coord)).isEqualTo((byte) 125);

        coord = Coord.parseToVal("Aa100");
        assertThat(Coord.X(coord)).isEqualTo((byte) 26);
        assertThat(Coord.Y(coord)).isEqualTo((byte) 99);

        coord = Coord.parseToVal("j8");
        assertThat(Coord.X(coord)).isEqualTo((byte) 8);
        assertThat(Coord.Y(coord)).isEqualTo((byte) 7);
    }

    @Test
    public void parseStatic()
    {
        assertThat(Coord.parseToVal("bz126")).isEqualTo(Coord.XY((byte) (26 * 2 + 24), (byte) 125));
        assertThat(Coord.parseToVal("A1")).isEqualTo(Coord.XY((byte) 0, (byte) 0));
        assertThat(Coord.parseToVal("b2")).isEqualTo(Coord.XY((byte) 1, (byte) 1));
    }

    @Test
    public void shortToString()
    {
        assertThat(Coord.shortToString(Coord.parseToVal("A1"))).isEqualTo("A1");
        assertThat(Coord.shortToString(Coord.parseToVal("B2"))).isEqualTo("B2");
        assertThat(Coord.shortToString(Coord.parseToVal("C10"))).isEqualTo("C10");
        assertThat(Coord.shortToString(Coord.parseToVal("Z20"))).isEqualTo("Z20");
//        assertThat(Coord.shortToString(Coord.parseToVal("AA100"))).isEqualTo("AA100")); //FIXME
    }

    @Test
    public void parserCanTrim()
    {
        final short parsed = Coord.parseToVal("   D10   ");
        assertThat(Coord.isValid(parsed)).isTrue();
        assertThat(Coord.X(parsed)).isEqualTo((byte) 3);
        assertThat(Coord.Y(parsed)).isEqualTo((byte) 9);
    }

    @Test
    public void parserHandleInvalids()
    {
        assertThat(Coord.isValid(Coord.parseToVal("aasdasd"))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal("aas dasd"))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal("123A"))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal("AA1234"))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal("AAA1"))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal("123"))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal(" A 1 "))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal(""))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal("   "))).isFalse();
        assertThat(Coord.isValid(Coord.parseToVal(null))).isFalse();
    }

    @Test
    public void toStringIsParsed_itRountrips()
    {
        for (byte x = 0; x < 25; x++)
        {
            for (byte y = 0; y < 25; y++)
            {
                final short original = Coord.XY(x, y);
                final String toString = Coord.shortToString(original);
                final short parsed = Coord.parseToVal(toString);
                assertThat(Coord.X(parsed)).isEqualTo(x);
                assertThat(Coord.Y(parsed)).isEqualTo(y);
                assertThat(original).isEqualTo(parsed);
            }
        }
    }

    private short c(final String coordSt)
    {
        return Coord.parseToVal(coordSt);
    }

    @Test
    public void rotationsAndReflections()
    {
        final short[] buffer = new short[9];

        // centre, 1 result
        assertThat(Coord.allRotationsAndReflections(buffer, Coord.parseToVal("E5"), (byte) 9)).isEqualTo(1);
        assertThat(cutOnFirstInvalid(buffer)[0]).isEqualTo(c("E5"));

        // diagonal, 4 results
        assertThat(Coord.allRotationsAndReflections(buffer, Coord.A1, (byte) 9)).isEqualTo(4);
        assertThat(cutOnFirstInvalid(buffer)).asList().containsExactly(c("A1"), c("A9"), c("J9"), c("J1"));

        assertThat(Coord.allRotationsAndReflections(buffer, Coord.C3, (byte) 19)).isEqualTo(4);
        assertThat(cutOnFirstInvalid(buffer)).asList().containsExactly(c("C3"), c("C17"), c("R17"), c("R3"));

        // anything else, 8 results
        assertThat(Coord.allRotationsAndReflections(buffer, Coord.A2, (byte) 9)).isEqualTo(8);
        assertThat(cutOnFirstInvalid(buffer)).asList()
            .containsExactly(c("A2"), c("J2"), c("J8"), c("A8"), c("B1"), c("H1"), c("H9"), c("B9"));

        // 8 results in 19x19
        assertThat(Coord.allRotationsAndReflections(buffer, Coord.parseToVal("E3"), (byte) 19)).isEqualTo(8);
        assertThat(cutOnFirstInvalid(buffer)).asList()
            .containsExactly(c("E3"), c("P3"), c("P17"), c("E17"), c("C5"), c("R5"), c("R15"), c("C15"));

        Exception e = assertThrows(IllegalArgumentException.class,
            () -> Coord.allRotationsAndReflections(new short[1], Coord.A1, (byte) 9));
        assertThat(e).hasMessageThat().isEqualTo("Please provide an array of at least size 8.");
    }

    @Test
    public void linealOffset()
    {
        assertThat(Coord.linealOffset(Coord.A1, (byte) 9)).isEqualTo(0);
        assertThat(Coord.linealOffset(Coord.A1, (byte) 19)).isEqualTo(0);
        assertThat(Coord.linealOffset(Coord.B2, (byte) 4)).isEqualTo(5);
        assertThat(Coord.linealOffset(Coord.B4, (byte) 4)).isEqualTo(13);
        assertThat(Coord.linealOffset(Coord.D1, (byte) 4)).isEqualTo(3);
        assertThat(Coord.linealOffset(Coord.XY((byte) 9, (byte) 9), (byte) 9)).isEqualTo(90);
    }

}
