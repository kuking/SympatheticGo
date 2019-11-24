package uk.kukino.sgo;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Collection;

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
    public void cornerSuicide()
    {
        //  A B C D E F G H J
        // 1 . X . . . . . . . 1
        // 2 X . . . . . . . . 2
        // 3 . . . O . . . . . 3
        // 4 . . . . . . . . . 4
        // => White A1 is not a valid move

        game = given9x9Game();
        game.play("b a2");
        game.play("w d3");
        game.play("b b1");
        assertFalse(game.play("w a1"));
    }

    @Test
    public void oneSuicide()
    {
        //  14 . . . . . . . .
        //  15 . . . . . . . .
        //  16 . . X . . O . .
        //  17 . X . X . O . .
        //  18 . . X . . O . .
        //  19 . . . . . . . .
        //     A B C D E F G H

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
        //    A B C D E F G H J K L M N O P Q R S T
        //  1 . . . . . . . . . . . . . . . . . . . 1
        // ....
        //  7 . . . . . . . . . . . . . . . . . . . 7
        //  8 . . . X . . . . O O . . . . . . . . . 8
        //  9 . . . X . . . O . . O . . . . . . . . 9
        // 10 . . . X . . . O . . O . . . . . . . . 0
        // 11 . . . X . . . . O O . . . . . . . . . 1
        // 12 . . . . . . . . . . . . . . . . . . . 2

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
    public void bigKill()
    {
        //   A B C D E F G H J
        // 9 . . . . . . . . . 1
        // 8 O . . . . . . . . 2
        // 7 X O O O O O . . . 3
        // 6 X X X X X X O . . 4
        // 5 X X X X X X O . . 5
        // 4 X O O O O O . . . 6
        // 3 . . . . . . . . . 7
        // 2 . . . . . . . . . 8
        // 1 . . . . . . . . . 9
        //   A B C D E F G H J
        game = given9x9Game();
        assertAllValid(game, Lists.newArrayList(
            "b c7", "w c8", "b d7", "w d8", "b e7", "w e8", "b f7", "w f8", "b f6", "w g7", "b e6", "w g6", "b d6", "w f5", "b c6",
            "w e5", "b b7", "w d5", "b b6", "w c5", "b a6", "w b5", "b a7", "w b8", "b a8", "w a9", "b a5"));
        game.play("w a4");
        assertThat(game.deadStones(Color.BLACK), equalTo(14));
        assertThat(game.getBoard().get(Coord.parseToVal("D7")), equalTo(Color.EMPTY));
        assertTrue(game.play("B D7"));
        //   A B C D E F G H J
        // 9 . . . . . . . . . 1
        // 8 O . . . . . . . . 2
        // 7 . O O O O O . . . 3
        // 6 . . . X . . O . . 4
        // 5 . . . . . . O . . 5
        // 4 . O O O O O . . . 6
        // 3 O . . . . . . . . 7
        // 2 . . . . . . . . . 8
        // 1 . . . . . . . . . 9
        // A B C D E F G H J
    }

    @Test
    public void simplestKillThenDobleKill()
    {
        //    A B C D E F G H J K L M N O P Q R S T
        //  1 . . . . . . . . . . . . . . . . . . . 1
        // ....
        //  7 . . . . . . . . . . . . . . . . . . . 7
        //  8 . . . X . . . . O O . . . . . . . . . 8
        //  9 . . . X . . . O . O O . . . . . . . . 9
        // 10 . . . X . . . O O . O . . . . . . . . 0
        // 11 . . . X . . . . O O . . . . . . . . . 1
        // 12 . . . . . . . . . . . . . . . . . . . 2

        game = given19x19Game();
        assertAllValid(game, Lists.newArrayList("b k10", "w l10", "b k9", "w l9", "b j10", "w k11", "b j9",
            "w j11", "b d10", "w h10", "b d9", "w h9", "b d8", "w j8", "b d11", "w k8"/* eat 4 */, "b k10",
            "w j10", "b j9", "w k9" /* eat 2 different groups */));
        assertSame(game.getBoard().get("k10"), Color.EMPTY);
        assertSame(game.getBoard().get("j9"), Color.EMPTY);
        assertSame(game.getBoard().get("j10"), Color.WHITE);
        assertSame(game.getBoard().get("k9"), Color.WHITE);
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
        game = given9x9Game();
        assertTrue(game.play("b a1"));
        assertTrue(game.play("w b1"));
        assertTrue(game.play("b a3"));
        assertTrue(game.play("w c1"));
        assertTrue(game.play("b b2"));
        assertTrue(game.play("w a2"));  // eat, valid.
        assertFalse(game.play("b a1")); // invalid.
        assertTrue(game.play("b b3"));
        assertTrue(game.play("w a1"));  // white can finish ko

        game = given9x9Game();
        assertTrue(game.play("b a1"));
        assertTrue(game.play("w b1"));
        assertTrue(game.play("b a3"));
        assertTrue(game.play("w c1"));
        assertTrue(game.play("b b2"));
        assertTrue(game.play("w a2"));  // eat, valid.
        assertFalse(game.play("b a1")); // invalid.
        assertTrue(game.play("b b3"));
        assertTrue(game.play("w d1"));
        assertTrue(game.play("b a1")); // now black can eat again
        assertFalse(game.play("w a2")); // and white can´t eat at A2 because its a KO
    }

    //TODO: https://senseis.xmp.net/?PinwheelKo

    @Test
    public void twoConsecutiveBlackMovesAreNotValid()
    {
        game = given9x9Game();
        game.play("black A3");
        assertFalse(game.play("black G5"));
    }

    @Test
    public void twoConsecutiveWhiteMovesAreNotValid()
    {
        game = given9x9Game();
        game.play("black A3");
        game.play("white A6");
        assertFalse(game.play("white F2"));
    }

    @Test
    public void simpleFinishGame()
    {
        game = given9x9Game();
        game.play("black a1");
        game.play("white pass");
        game.play("black pass");
        assertTrue(game.finished());
    }

    @Test
    public void shortestGame()
    {
        game = given9x9Game();
        game.play("black pass");
        game.play("white pass");
        assertTrue(game.finished());
    }

    @Test
    public void notSoSimpleFinishGame()
    {
        game = given19x19Game();
        assertAllValid(game, Lists.newArrayList("black a1", "white pass", "black g10", "white f10", "black pass", "white pass"));
        assertTrue(game.finished());
    }

    /* --------------------------------------------------------------------------------------------------------------------------------- */

    private void assertAllValid(final Game game, Collection<String> moves)
    {
        moves.forEach(move -> assertTrue(game.play(move), () -> "Move " + move + " Failed!"));
    }

    private Game given19x19Game()
    {
        return new Game((byte) 19, (byte) 0, (byte) 55);
    }

    private Game given9x9Game()
    {
        return new Game((byte) 9, (byte) 0, (byte) 45);
    }

}
