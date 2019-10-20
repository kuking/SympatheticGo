package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoveTest {

    private Move move = new Move();

    @Test
    public void simple() {
        assertTrue(move.parse("Black A2"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertThat(move.x(), equalTo((byte) 1));
        assertThat(move.y(), equalTo((byte) 2));
        assertTrue(move.isStone());
        assertFalse(move.isPass());
    }

    @Test
    public void pass() {
        assertTrue(move.parse("White Pass"));
        assertThat(move.color(), equalTo(Color.WHITE));
        assertTrue(move.isValid());
        assertThat(move.x(), is((byte) 0));
        assertThat(move.y(), is((byte) 0));
        assertFalse(move.isStone());
        assertTrue(move.isPass());
    }

    @Test
    public void moreParsing() {
        assertTrue(move.parse("B Z126"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertTrue(move.isValid());
        assertThat(move.x(), equalTo((byte) 25));
        assertThat(move.y(), equalTo((byte) 126));
        assertTrue(move.isStone());

        assertTrue(move.parse("W AA100"));
        assertThat(move.color(), equalTo(Color.WHITE));
        assertTrue(move.isValid());
        assertThat(move.x(), equalTo((byte) 26));
        assertThat(move.y(), equalTo((byte) 100));
        assertTrue(move.isStone());

        assertTrue(move.parse("Black J8"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertTrue(move.isValid());
        assertThat(move.x(), equalTo((byte) 9));
        assertThat(move.y(), equalTo((byte) 8));
        assertTrue(move.isStone());
    }

    @Test
    public void itParsesUntrimmedStrings() {
        assertTrue(move.parse("   Black A2  "));
        assertTrue(move.isValid());
        assertThat(move.x(), is((byte) 1));
        assertThat(move.y(), is((byte) 2));
        assertFalse(move.isPass());
        assertTrue(move.isStone());
    }

    @Test
    void values() {
        short value = Move.parseToValue("W AB123");

        assertTrue(Move.isValid(value));
        assertThat(Move.color(value), equalTo(Color.WHITE));
        assertThat(Move.x(value), equalTo((byte) 27));
        assertThat(Move.y(value), equalTo((byte) 123));

        value = Move.parseToValue("BLACK PASS");
        assertThat(Move.color(value), equalTo(Color.BLACK));
        assertTrue(Move.isPass(value));

        value = Move.parseToValue("invalid");
        assertFalse(Move.isValid(value));
    }

    @Test
    public void invalids() {
        assertFalse(move.parse("rubish"));
        assertFalse(move.isValid());

        assertFalse(move.parse("Black"));
        assertFalse(move.isValid());

        assertFalse(move.parse("Blac"));
        assertFalse(move.isValid());

        assertFalse(move.parse("B2"));
        assertFalse(move.isValid());

        assertFalse(move.parse(null));
        assertFalse(move.isValid());
    }
}
