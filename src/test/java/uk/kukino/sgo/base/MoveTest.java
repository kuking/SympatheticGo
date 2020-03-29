package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;


public class MoveTest
{

    private short p(final String st)
    {
        return Move.parseToVal(st);
    }

    @Test
    public void simple()
    {
        final short move = p("Black A2");
        assertThat(Move.color(move)).isEqualTo(Color.BLACK);
        assertThat(Move.X(move)).isEqualTo((byte) 0);
        assertThat(Move.Y(move)).isEqualTo((byte) 1);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.isStone(move)).isTrue();
        assertThat(Move.isPass(move)).isFalse();
        assertThat(Move.isResign(move)).isFalse();
    }

    @Test
    public void pass()
    {
        final short move = p("White Pass");
        assertThat(Move.color(move)).isEqualTo(Color.WHITE);
        assertThat(Move.X(move)).isEqualTo((byte) 127);
        assertThat(Move.Y(move)).isEqualTo((byte) 127);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.isStone(move)).isFalse();
        assertThat(Move.isResign(move)).isFalse();
        assertThat(Move.isPass(move)).isTrue();
    }

    @Test
    public void resign()
    {
        final short move = p("White Resign");
        assertThat(Move.color(move)).isEqualTo(Color.WHITE);
        assertThat(Move.X(move)).isEqualTo((byte) 126);
        assertThat(Move.Y(move)).isEqualTo((byte) 126);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.isStone(move)).isFalse();
        assertThat(Move.isResign(move)).isTrue();
        assertThat(Move.isPass(move)).isFalse();
    }

    @Test
    public void moreParsing()
    {
        short move = p("B Z126");
        assertThat(Move.color(move)).isEqualTo(Color.BLACK);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.X(move)).isEqualTo((byte) 24);
        assertThat(Move.Y(move)).isEqualTo((byte) 125);
        assertThat(Move.isStone(move)).isTrue();

        move = p("W AA100");
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.color(move)).isEqualTo(Color.WHITE);
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.X(move)).isEqualTo((byte) 26);
        assertThat(Move.Y(move)).isEqualTo((byte) 99);
        assertThat(Move.isStone(move)).isTrue();

        move = p("Black J8");
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
        final short move = p("   Black A2  ");
        assertThat(Move.isValid(move)).isTrue();
        assertThat(Move.X(move)).isEqualTo((byte) 0);
        assertThat(Move.Y(move)).isEqualTo((byte) 1);
        assertThat(Move.isPass(move)).isFalse();
        assertThat(Move.isStone(move)).isTrue();
        assertThat(Move.isResign(move)).isFalse();
    }

    @Test
    void values()
    {
        short value = p("W AB123");
        assertThat(Move.isValid(value)).isTrue();
        assertThat(Move.color(value)).isEqualTo(Color.WHITE);
        assertThat(Move.X(value)).isEqualTo((byte) 27);
        assertThat(Move.Y(value)).isEqualTo((byte) 122);

        value = p("BLACK PASS");
        assertThat(Move.color(value)).isEqualTo(Color.BLACK);
        assertThat(Move.isPass(value)).isTrue();

        value = p("BLACK RESIGN");
        assertThat(Move.color(value)).isEqualTo(Color.BLACK);
        assertThat(Move.isResign(value)).isTrue();

        value = p("invalid");
        assertThat(Move.isValid(value)).isFalse();
    }

    @Test
    public void invalids()
    {
        assertThat(Move.isValid(p("rubish"))).isFalse();
        assertThat(Move.isValid(p("Black"))).isFalse();
        assertThat(Move.isValid(p("Blac"))).isFalse();
        assertThat(Move.isValid(p("B2"))).isFalse();
        assertThat(Move.isValid(p("BLACK A"))).isFalse();
        assertThat(Move.isValid(p("   "))).isFalse();
        assertThat(Move.isValid(p(""))).isFalse();
        assertThat(Move.isValid(p(null))).isFalse();
    }

    @Test
    public void doesToString()
    {
        assertThat(Move.shortToString(p("Black B10"))).isEqualTo("Black B10");
        assertThat(Move.shortToString(p("w Q12"))).isEqualTo("White Q12");
        assertThat(Move.shortToString(p("w pass"))).isEqualTo("White Pass");
        assertThat(Move.shortToString(Move.INVALID)).isEqualTo("Invalid");
        assertThat(Move.shortToString(p("Black Resign"))).isEqualTo("Black Resign");
    }

    @Test
    public void writeNotColor()
    {
        final StringBuilder sb = new StringBuilder();
        Move.write(sb, p("Black B10"), false, false);
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
                    final short parsed = p(toString);
                    assertThat(Move.X(parsed)).isEqualTo(x);
                    assertThat(Move.Y(parsed)).isEqualTo(y);
                    assertThat(Move.color(parsed)).isEqualTo(color);
                    assertThat(original).isEqualTo(parsed);
                }
            }
        }
    }

    @Test
    public void invertPlayer()
    {
        assertThat(Move.oppositePlayer(Move.WHITE_PASS)).isEqualTo(Move.BLACK_PASS);
        assertThat(Move.oppositePlayer(Move.BLACK_PASS)).isEqualTo(Move.WHITE_PASS);
        assertThat(Move.oppositePlayer(Move.WHITE_RESIGN)).isEqualTo(Move.BLACK_RESIGN);
        assertThat(Move.oppositePlayer(Move.BLACK_RESIGN)).isEqualTo(Move.WHITE_RESIGN);
        assertThat(Move.oppositePlayer(Move.BLACK_A1)).isEqualTo(Move.WHITE_A1);
        assertThat(Move.oppositePlayer(Move.WHITE_B1)).isEqualTo(Move.BLACK_B1);
    }
}
