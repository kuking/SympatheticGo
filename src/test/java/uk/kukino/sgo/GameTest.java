package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameTest {

    Game game;
    Move move = new Move();


    @Test
    public void initialValues() {
        game = new Game((byte) 19, (byte) 0, (byte) 55);
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
        game = new Game((byte) 19, (byte) 0, (byte) 55);

        assertTrue(game.play(Move.parseToValue("B D4")));

        assertThat(game.lastMove(), equalTo(1));
        assertThat(game.playerToPlay(), equalTo(Color.WHITE));
        // over-the-top, but still
        assertFalse(game.finished());
        assertThat(game.deadStones(Color.BLACK), equalTo(0));
        assertThat(game.deadStones(Color.WHITE), equalTo(0));
    }

}
