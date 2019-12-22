package uk.kukino.sgo.base;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoveTest
{

    @Test
    public void simple()
    {
        final short move = Move.parseToVal("Black A2");
        assertThat(Move.color(move), equalTo(Color.BLACK));
        assertThat(Move.X(move), equalTo((byte) 0));
        assertThat(Move.Y(move), equalTo((byte) 1));
        assertTrue(Move.isValid(move));
        assertTrue(Move.isStone(move));
        assertFalse(Move.isPass(move));
    }

    @Test
    public void pass()
    {
        final short move = Move.parseToVal("White Pass");
        assertThat(Move.color(move), equalTo(Color.WHITE));
        assertThat(Move.X(move), equalTo((byte) 127));
        assertThat(Move.Y(move), equalTo((byte) 127));
        assertTrue(Move.isValid(move));
        assertFalse(Move.isStone(move));
        assertTrue(Move.isPass(move));
    }

    @Test
    public void moreParsing()
    {
        short move = Move.parseToVal("B Z126");
        assertThat(Move.color(move), equalTo(Color.BLACK));
        assertTrue(Move.isValid(move));
        assertThat(Move.X(move), equalTo((byte) 24));
        assertThat(Move.Y(move), equalTo((byte) 125));
        assertTrue(Move.isStone(move));

        move = Move.parseToVal("W AA100");
        assertTrue(Move.isValid(move));
        assertThat(Move.color(move), equalTo(Color.WHITE));
        assertTrue(Move.isValid(move));
        assertThat(Move.X(move), equalTo((byte) 26));
        assertThat(Move.Y(move), equalTo((byte) 99));
        assertTrue(Move.isStone(move));

        move = Move.parseToVal("Black J8");
        assertTrue(Move.isValid(move));
        assertThat(Move.color(move), equalTo(Color.BLACK));
        assertTrue(Move.isValid(move));
        assertThat(Move.X(move), equalTo((byte) 8));
        assertThat(Move.Y(move), equalTo((byte) 7));
        assertTrue(Move.isStone(move));
    }

    @Test
    public void itParsesUntrimmedStrings()
    {
        final short move = Move.parseToVal("   Black A2  ");
        assertTrue(Move.isValid(move));
        assertThat(Move.X(move), is((byte) 0));
        assertThat(Move.Y(move), is((byte) 1));
        assertFalse(Move.isPass(move));
        assertTrue(Move.isStone(move));
    }

    @Test
    void values()
    {
        short value = Move.parseToVal("W AB123");

        assertTrue(Move.isValid(value));
        assertThat(Move.color(value), equalTo(Color.WHITE));
        assertThat(Move.X(value), equalTo((byte) 27));
        assertThat(Move.Y(value), equalTo((byte) 122));

        value = Move.parseToVal("BLACK PASS");
        assertThat(Move.color(value), equalTo(Color.BLACK));
        assertTrue(Move.isPass(value));

        value = Move.parseToVal("invalid");
        assertFalse(Move.isValid(value));
    }

    @Test
    public void invalids()
    {
        assertFalse(Move.isValid(Move.parseToVal("rubish")));
        assertFalse(Move.isValid(Move.parseToVal("Black")));
        assertFalse(Move.isValid(Move.parseToVal("Blac")));
        assertFalse(Move.isValid(Move.parseToVal("B2")));
        assertFalse(Move.isValid(Move.parseToVal("BLACK A")));
        assertFalse(Move.isValid(Move.parseToVal("   ")));
        assertFalse(Move.isValid(Move.parseToVal("")));
        assertFalse(Move.isValid(Move.parseToVal(null)));
    }
}
