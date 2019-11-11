package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class GameTest
{

    private Game game;

    @Test
    public void initialValues()
    {
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
    public void handicap()
    {
        game = new Game((byte) 19, (byte) 2, (byte) 55);

        assertThat(game.getHandicap(), equalTo((byte) 2));
        assertThat(game.playerToPlay(), equalTo(Color.WHITE));
        assertThat(game.getBoard().get((byte) 3, (byte) 3), equalTo(Color.BLACK));
        assertThat(game.getBoard().get((byte) 15, (byte) 15), equalTo(Color.BLACK));
    }

    @Test
    public void simpleMove()
    {
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
    public void someInvalidMoves()
    {
        game = given19x19Game();

        assertFalse(game.play("W A1")); // white can't play first

        assertTrue(game.play("B A1"));
        assertFalse(game.play("W A1"));

        assertFalse(game.play("W Z25")); // outside the board
        assertFalse(game.play("W A0")); // 0  is not valid
    }

    @Test
    public void simpleSuicide()
    {
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
    public void simplestKill()
    {
        game = given19x19Game();
        game.play("B B1");
        game.play("W A1");
        game.play("B A2");
        assertThat(game.getBoard().get("A1"), equalTo(Color.EMPTY));
        assertThat(game.deadStones(Color.WHITE), equalTo(1));
    }

    @Test
    public void notSoSimpleKill()
    {
        /*
   A B C D E F G H J K L M N O P Q R S T
 1 . . . . . . . . . . . . . . . . . . . 1
 2 . . . . . . . . . . . . . . . . . . . 2
 3 . . . . . . . . . . . . . . . . . . . 3
 4 . . . . . . . . . . . . . . . . . . . 4
 5 . . . . . . . . . . . . . . . . . . . 5
 6 . . . . . . . . . . . . . . . . . . . 6
 7 . . . . . . . . . . . . . . . . . . . 7
 8 . . . X . . . . O O . . . . . . . . . 8
 9 . . . X . . . O . . O . . . . . . . . 9
10 . . . X . . . O . . O . . . . . . . . 0
11 . . . X . . . . O O . . . . . . . . . 1
12 . . . . . . . . . . . . . . . . . . . 2
13 . . . . . . . . . . . . . . . . . . . 3
14 . . . . . . . . . . . . . . . . . . . 4
15 . . . . . . . . . . . . . . . . . . . 5
16 . . . . . . . . . . . . . . . . . . . 6
17 . . . . . . . . . . . . . . . . . . . 7
18 . . . . . . . . . . . . . . . . . . . 8
19 . . . . . . . . . . . . . . . . . . . 9
   A B C D E F G H J K L M N O P Q R S T
         */
        game = given19x19Game();
        game.play("b k10");
        game.play("w l10");
        game.play("b k9");
        game.play("w l9");
        game.play("b j10");
        game.play("w k11");
        game.play("b j9");
        game.play("w j11");
        game.play("b d10");
        game.play("w h10");
        game.play("b d9");
        game.play("w h9");
        game.play("b d8");
        game.play("w j8");
        game.play("b d11");
        game.play("w k8");

        assertTrue(game.getBoard().get("k10") == Color.EMPTY);
        assertTrue(game.getBoard().get("j9") == Color.EMPTY);
        assertTrue(game.getBoard().get("j10") == Color.EMPTY);
        assertTrue(game.getBoard().get("k9") == Color.EMPTY);
        assertEquals(0, game.deadStones(Color.WHITE));
        assertEquals(4, game.deadStones(Color.BLACK));
    }

    @Test
    public void simplestKillThenDobleKill()
    {
        game = given19x19Game();
        game.play("b k10");
        game.play("w l10");
        game.play("b k9");
        game.play("w l9");
        game.play("b j10");
        game.play("w k11");
        game.play("b j9");
        game.play("w j11");
        game.play("b d10");
        game.play("w h10");
        game.play("b d9");
        game.play("w h9");
        game.play("b d8");
        game.play("w j8");
        game.play("b d11");
        game.play("w k8");  // eat 4
        game.play("b k10");
        game.play("w j10");
        game.play("b j9");
        game.play("w k9"); // eats 2 different groups
        assertTrue(game.getBoard().get("k10") == Color.EMPTY);
        assertTrue(game.getBoard().get("j9") == Color.EMPTY);
        assertTrue(game.getBoard().get("j10") == Color.WHITE);
        assertTrue(game.getBoard().get("k9") == Color.WHITE);
    }

    @Test
    public void basicKO()
    {
        /*
         *  5 . . . . . . . . .
         *  4 . . . + . . . . .
         *  3 X X . . . . . . .
         *  2 . X . . . . . . .
         *  1(X)O O O . . . . .
         *    A B C D E F G H J
         */
        game = given19x19Game();
        assertTrue(game.play("b a1"));
        assertTrue(game.play("w b1"));
        assertTrue(game.play("b a3"));
        assertTrue(game.play("w c1"));
        assertTrue(game.play("b b2"));
        assertTrue(game.play("w a2"));  // eat, valid.
        assertFalse(game.isValidMove("b a1")); // invalid.
        assertTrue(game.play("b b3"));
        assertTrue(game.isValidMove("w a1")); // white can finish ko
        assertTrue(game.play("w d1"));
        assertTrue(game.play("b a1")); // now black can eat again
        assertFalse(game.play("w a2")); // and white can´t eat at A2 because its a KO
    }


    private Game given19x19Game()
    {
        return new Game((byte) 19, (byte) 0, (byte) 55);
    }

}
