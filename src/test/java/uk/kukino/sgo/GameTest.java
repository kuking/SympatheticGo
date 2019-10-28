package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTest {

    Game game;

    @Test
    public void initialValues() {
        game = given19x19Game();
        assertThat(game.getHandicap(), equalTo((byte) 0));
        assertThat(game.getKomiX10(), equalTo((byte) 55));
        assertThat(game.getBoard().size(), equalTo((byte) 19));
        assertThat(game.lastMove(), equalTo(0));
        assertFalse(game.finished());
        assertThat(game.playerToPlay(), equalTo(Color.BLACK));
        assertThat(game.deadStones(Color.BLACK), equalTo(0));
        assertThat(game.deadStones(Color.WHITE), equalTo(0));
    }

    @Test
    public void handicap() {
        game = new Game((byte) 19, (byte) 2, (byte) 55);

        assertThat(game.getHandicap(), equalTo((byte) 2));
        assertThat(game.playerToPlay(), equalTo(Color.WHITE));
        assertThat(game.getBoard().get((byte) 4, (byte) 4), equalTo(Color.BLACK));
        assertThat(game.getBoard().get((byte) 16, (byte) 16), equalTo(Color.BLACK));
    }

    @Test
    public void simpleMove() {
        game = given19x19Game();

        assertTrue(game.play("B D4"));

        assertThat(game.lastMove(), equalTo(1));
        assertThat(game.playerToPlay(), equalTo(Color.WHITE));
        // over-the-top, but still
        assertFalse(game.finished());
        assertThat(game.deadStones(Color.BLACK), equalTo(0));
        assertThat(game.deadStones(Color.WHITE), equalTo(0));
    }


    @Test
    public void someInvalidMoves() {
        game = given19x19Game();

        assertFalse(game.play("W A1")); // white can't play first

        assertTrue(game.play("B A1"));
        assertFalse(game.play("W A1"));

        assertFalse(game.play("W Z25")); // outside the board
        assertFalse(game.play("W A0")); // 0  is not valid
    }

    @Test
    public void simpleSuicide() {
        // +--------
        // |   X  O
        // |  X X O
        // |   X  O
        // TODO: use nice toString, when implemented

        game = given19x19Game();
        game.play("B C18");
        game.play("W F18");
        game.play("B D17");
        game.play("W F17");
        game.play("B C16");
        game.play("W F16");
        game.play("B B17");

        assertFalse(game.play("W C17")); // suicide
        game.play("W F15");
        assertTrue(game.play("B C17")); // just a bunch of space
    }

    @Test
    public void simplestKill() {
        game = given19x19Game();
        game.play("B B1");
        game.play("W A1");
        game.play("B A2");
        assertThat(game.getBoard().get("A1"), equalTo(Color.EMPTY));
        assertThat(game.deadStones(Color.WHITE), equalTo(1));
    }


    private Game given19x19Game() {
        return new Game((byte) 19, (byte) 0, (byte) 55);
    }

}
