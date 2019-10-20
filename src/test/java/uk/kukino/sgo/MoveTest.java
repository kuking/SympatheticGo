package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoveTest {

    Move move = new Move();

    @Test
    public void simple() {
        assertTrue(move.parse("Black A2"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertThat(move.x(), equalTo((byte)1));
        assertThat(move.y(), equalTo((byte)2));
        assertFalse(move.pass());
    }

    @Test
    public void pass() {
        assertTrue(move.parse("White Pass"));
        assertThat(move.color(), equalTo(Color.WHITE));
        assertTrue(move.valid());
        assertThat(move.x(), is((byte)0));
        assertThat(move.y(), is((byte)0));
        assertTrue(move.pass());
    }

    @Test
    public void moreParsing() {
        assertTrue(move.parse("B Z126"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertTrue(move.valid());
        assertThat(move.x(), equalTo((byte)25));
        assertThat(move.y(), equalTo((byte)126));

        assertTrue(move.parse("W AA100"));
        assertThat(move.color(), equalTo(Color.WHITE));
        assertTrue(move.valid());
        assertThat(move.x(), equalTo((byte)26));
        assertThat(move.y(), equalTo((byte)100));

        assertTrue(move.parse("Black J8"));
        assertThat(move.color(), equalTo(Color.BLACK));
        assertTrue(move.valid());
        assertThat(move.x(), equalTo((byte)9));
        assertThat(move.y(), equalTo((byte)8));
    }

    public void itParsesUntrimmedStrings() {
        assertTrue(move.parse("   Black A2  "));
        assertTrue(move.valid());
        assertThat(move.x(), is(1));
        assertThat(move.y(), is(2));
        assertFalse(move.pass());
    }

    @Test
    public void invalids() {
        assertFalse(move.parse("rubish"));
        assertFalse(move.valid());

        assertFalse(move.parse("Black"));
        assertFalse(move.valid());

        assertFalse(move.parse("Blac"));
        assertFalse(move.valid());

        assertFalse(move.parse("B2"));
        assertFalse(move.valid());

        assertFalse(move.parse(null));
        assertFalse(move.valid());
    }
}
