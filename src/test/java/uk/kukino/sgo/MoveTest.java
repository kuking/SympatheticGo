package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoveTest
{

    private Move move = new Move();

    @Test
    public void simple()
    {
        assertTrue(move.parse("Black A2"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertThat(move.x(), equalTo((byte) 0));
        assertThat(move.y(), equalTo((byte) 1));
        assertTrue(move.isStone());
        assertFalse(move.isPass());
    }

    @Test
    public void pass()
    {
        assertTrue(move.parse("White Pass"));
        assertThat(move.color(), equalTo(Color.WHITE));
        assertTrue(move.isValid());
        assertThat(move.x(), is((byte) 127));
        assertThat(move.y(), is((byte) 127));
        assertFalse(move.isStone());
        assertTrue(move.isPass());
    }

    @Test
    public void moreParsing()
    {
        assertTrue(move.parse("B Z126"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertTrue(move.isValid());
        assertThat(move.x(), equalTo((byte) 24));
        assertThat(move.y(), equalTo((byte) 125));
        assertTrue(move.isStone());

        assertTrue(move.parse("W AA100"));
        assertThat(move.color(), equalTo(Color.WHITE));
        assertTrue(move.isValid());
        assertThat(move.x(), equalTo((byte) 26));
        assertThat(move.y(), equalTo((byte) 99));
        assertTrue(move.isStone());

        assertTrue(move.parse("Black J8"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertTrue(move.isValid());
        assertThat(move.x(), equalTo((byte) 8));
        assertThat(move.y(), equalTo((byte) 7));
        assertTrue(move.isStone());
    }

    @Test
    public void itParsesUntrimmedStrings()
    {
        assertTrue(move.parse("   Black A2  "));
        assertTrue(move.isValid());
        assertThat(move.x(), is((byte) 0));
        assertThat(move.y(), is((byte) 1));
        assertFalse(move.isPass());
        assertTrue(move.isStone());
    }

    @Test
    void values()
    {
        short value = Move.parseToVal("W AB123");

        assertTrue(Move.isValid(value));
        assertThat(Move.color(value), equalTo(Color.WHITE));
        assertThat(Move.x(value), equalTo((byte) 27));
        assertThat(Move.y(value), equalTo((byte) 122));

        value = Move.parseToVal("BLACK PASS");
        assertThat(Move.color(value), equalTo(Color.BLACK));
        assertTrue(Move.isPass(value));

        value = Move.parseToVal("invalid");
        assertFalse(Move.isValid(value));
    }

    @Test
    public void invalids()
    {
        assertFalse(move.parse("rubish"));
        assertFalse(move.isValid());

        assertFalse(move.parse("Black"));
        assertFalse(move.isValid());

        assertFalse(move.parse("Blac"));
        assertFalse(move.isValid());

        assertFalse(move.parse("B2"));
        assertFalse(move.isValid());

        assertFalse(move.parse("BLACK A"));
        assertFalse(move.isValid());

        assertFalse(move.parse("   "));
        assertFalse(move.isValid());

        assertFalse(move.parse(""));
        assertFalse(move.isValid());

        assertFalse(move.parse(null));
        assertFalse(move.isValid());
    }
}
