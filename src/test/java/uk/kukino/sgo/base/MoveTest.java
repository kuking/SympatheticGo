package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;


public class MoveTest
{

    @Test
    public void simple()
    {
        final short move = Move.parseToVal("Black A2");
        assertThat(Move.color(move)).isEqualTo(Color.BLACK);
        assertThat(Move.X(move)).isEqualTo((byte) 0);
        assertThat(Move.Y(move)).isEqualTo((byte) 1);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.isStone(move)).isTrue();
        assertThat(Move.isPass(move)).isFalse();
    }

    @Test
    public void pass()
    {
        final short move = Move.parseToVal("White Pass");
        assertThat(Move.color(move)).isEqualTo(Color.WHITE);
        assertThat(Move.X(move)).isEqualTo((byte) 127);
        assertThat(Move.Y(move)).isEqualTo((byte) 127);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.isStone(move)).isFalse();
        assertThat(Move.isPass(move)).isTrue();
    }

    @Test
    public void moreParsing()
    {
        short move = Move.parseToVal("B Z126");
        assertThat(Move.color(move)).isEqualTo(Color.BLACK);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.X(move)).isEqualTo((byte) 24);
        assertThat(Move.Y(move)).isEqualTo((byte) 125);
        assertThat(Move.isStone(move)).isTrue();

        move = Move.parseToVal("W AA100");
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.color(move)).isEqualTo(Color.WHITE);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.X(move)).isEqualTo((byte) 26);
        assertThat(Move.Y(move)).isEqualTo((byte) 99);
        assertThat(Move.isStone(move)).isTrue();

        move = Move.parseToVal("Black J8");
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.color(move)).isEqualTo(Color.BLACK);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.X(move)).isEqualTo((byte) 8);
        assertThat(Move.Y(move)).isEqualTo((byte) 7);
        assertThat(Move.isStone(move)).isTrue();
    }

    @Test
    public void itParsesUntrimmedStrings()
    {
        final short move = Move.parseToVal("   Black A2  ");
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.X(move)).isEqualTo((byte) 0);
        assertThat(Move.Y(move)).isEqualTo((byte) 1);
        assertThat(Move.isPass(move)).isFalse();
        assertThat(Move.isStone(move)).isTrue();
    }

    @Test
    void values()
    {
        short value = Move.parseToVal("W AB123");

        assertThat(Move.isValid(value)).isTrue();
        assertThat(Move.color(value)).isEqualTo(Color.WHITE);
        assertThat(Move.X(value)).isEqualTo((byte) 27);
        assertThat(Move.Y(value)).isEqualTo((byte) 122);

        value = Move.parseToVal("BLACK PASS");
        assertThat(Move.color(value)).isEqualTo(Color.BLACK);
        assertThat(Move.isPass(value)).isTrue();

        value = Move.parseToVal("invalid");
        assertThat(Move.isValid(value)).isFalse();
    }

    @Test
    public void invalids()
    {
        assertThat(Move.isValid(Move.parseToVal("rubish"))).isFalse();
        assertThat(Move.isValid(Move.parseToVal("Black"))).isFalse();
        assertThat(Move.isValid(Move.parseToVal("Blac"))).isFalse();
        assertThat(Move.isValid(Move.parseToVal("B2"))).isFalse();
        assertThat(Move.isValid(Move.parseToVal("BLACK A"))).isFalse();
        assertThat(Move.isValid(Move.parseToVal("   "))).isFalse();
        assertThat(Move.isValid(Move.parseToVal(""))).isFalse();
        assertThat(Move.isValid(Move.parseToVal(null))).isFalse();
    }

    @Test
    public void doesToString()
    {
        assertThat(Move.shortToString(Move.parseToVal("Black B10"))).isEqualTo("Black B10");
        assertThat(Move.shortToString(Move.parseToVal("w Q12"))).isEqualTo("White Q12");
        assertThat(Move.shortToString(Move.parseToVal("w pass"))).isEqualTo("White Pass");
        assertThat(Move.shortToString(Move.INVALID)).isEqualTo("Invalid");
    }

    @Test
    public void writeNotColor()
    {
        final StringBuilder sb = new StringBuilder();
        Move.write(sb, Move.parseToVal("Black B10"), false, false);
        assertThat(sb.toString()).isEqualTo("B10");
    }

    @Test
    public void toStringIsParsed_itRoundTrips()
    {
        Color[] colors = new Color[] {Color.BLACK, Color.WHITE};
        for (byte x = 0; x < 25; x++)
        {
            for (byte y = 0; y < 25; y++)
            {
                for (Color color : colors)
                {
                    final short original = Move.move(x, y, color);
                    final short original2 = Move.move(Coord.XY(x, y), color);
                    assertThat(original).isEqualTo(original2);
                    final String toString = Move.shortToString(original);
                    final short parsed = Move.parseToVal(toString);
                    assertThat(Move.X(parsed)).isEqualTo(x);
                    assertThat(Move.Y(parsed)).isEqualTo(y);
                    assertThat(Move.color(parsed)).isEqualTo(color);
                    assertThat(original).isEqualTo(parsed);
                }
            }
        }
    }
}
