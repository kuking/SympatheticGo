package uk.kukino.sgo.base;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameTest
{

    private Game game;

    @Test
    public void initialValues()
    {
        game = given19x19Game();
        assertThat(game.getHandicap()).isEqualTo((byte) 0);
        assertThat(game.getKomiX10()).isEqualTo((byte) 55);
        assertThat(game.getBoard().size()).isEqualTo((byte) 19);
        assertThat(game.lastMove()).isEqualTo(0);
        assertThat(game.finished()).isFalse();
        assertThat(game.playerToPlay()).isEqualTo(Color.BLACK);
        assertThat(game.deadStones(Color.BLACK)).isEqualTo(0);
        assertThat(game.deadStones(Color.WHITE)).isEqualTo(0);
    }

    @Test
    public void handicap()
    {
        game = new Game((byte) 19, (byte) 2, (byte) 55);

        assertThat(game.getHandicap()).isEqualTo((byte) 2);
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);
        assertThat(game.getBoard().get((byte) 3, (byte) 3)).isEqualTo(Color.BLACK);
        assertThat(game.getBoard().get((byte) 15, (byte) 15)).isEqualTo(Color.BLACK);
    }

    @Test
    public void simpleMove()
    {
        game = given19x19Game();

        assertThat(game.play("B D4")).isTrue();

        assertThat(game.lastMove()).isEqualTo(1);
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);
        // over-the-top, but still
        assertThat(game.finished()).isFalse();
        assertThat(game.deadStones(Color.BLACK)).isEqualTo(0);
        assertThat(game.deadStones(Color.WHITE)).isEqualTo(0);
    }


    @Test
    public void someInvalidMoves()
    {
        game = given19x19Game();

        assertThat(game.play("W A1")).isFalse(); // white can't play first

        assertThat(game.play("B A1")).isTrue();
        assertThat(game.play("W A1")).isFalse();

        assertThat(game.play("W Z25")).isFalse(); // outside the board
        assertThat(game.play("W A0")).isFalse(); // 0  is not valid
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
        assertThat(game.play("w a1")).isFalse();
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

        assertThat(game.play("W C17")).isFalse(); // suicide
        game.play("W F15");
        assertThat(game.play("B C17")).isTrue(); // just a bunch of space
    }

    @Test
    public void simplestKill()
    {
        game = given19x19Game();
        game.play("B B1");
        game.play("W A1");
        game.play("B A2");
        assertThat(game.getBoard().get("A1")).isEqualTo(Color.EMPTY);
        assertThat(game.deadStones(Color.WHITE)).isEqualTo(1);
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

        assertThat(game.getBoard().get("k10") == Color.EMPTY).isTrue();
        assertThat(game.getBoard().get("j9") == Color.EMPTY).isTrue();
        assertThat(game.getBoard().get("j10") == Color.EMPTY).isTrue();
        assertThat(game.getBoard().get("k9") == Color.EMPTY).isTrue();
        assertThat(game.deadStones(Color.WHITE)).isEqualTo(0);
        assertThat(game.deadStones(Color.BLACK)).isEqualTo(4);
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
        assertThat(game.deadStones(Color.BLACK)).isEqualTo(14);
        assertThat(game.getBoard().get(Coord.parseToVal("D7"))).isEqualTo(Color.EMPTY);
        assertThat(game.play("B D7")).isTrue();
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

        assertThat(game.getBoard().get("k10")).isEqualTo(Color.EMPTY);
        assertThat(game.getBoard().get("j9")).isEqualTo(Color.EMPTY);
        assertThat(game.getBoard().get("j10")).isEqualTo(Color.WHITE);
        assertThat(game.getBoard().get("k9")).isEqualTo(Color.WHITE);
        assertThat(game.deadStones(Color.BLACK)).isEqualTo(6);
        assertThat(game.deadStones(Color.WHITE)).isEqualTo(0);
    }

    @Test
    public void simplestKillThenDoubleKillInverted()
    {
        //    A B C D E F G H J K L M N O P Q R S T
        //  1 X . . . . . . . . . . . . . . . . . . 1
        // ....
        //  7 . . . . . . . . . . . . . . . . . . . 7
        //  8 . . . O . . . . X X . . . . . . . . . 8
        //  9 . . . O . . . X . X X . . . . . . . . 9
        // 10 . . . O . . . X X . X . . . . . . . . 0
        // 11 . . . O . . . . X X . . . . . . . . . 1
        // 12 . . . . . . . . . . . . . . . . . . . 2

        game = given19x19Game();
        assertAllValid(game, Lists.newArrayList("b a1", "w k10", "b l10", "w k9", "b l9", "w j10", "b k11", "w j9",
            "b j11", "w d10", "b h10", "w d9", "b h9", "w d8", "b j8", "w d11", "b k8"/* eat 4 */, "w k10",
            "b j10", "w j9", "b k9" /* eat 2 different groups */));
        assertThat(game.getBoard().get("k10")).isEqualTo(Color.EMPTY);
        assertThat(game.getBoard().get("j9")).isEqualTo(Color.EMPTY);
        assertThat(game.getBoard().get("j10")).isEqualTo(Color.BLACK);
        assertThat(game.getBoard().get("k9")).isEqualTo(Color.BLACK);
        assertThat(game.deadStones(Color.BLACK)).isEqualTo(0);
        assertThat(game.deadStones(Color.WHITE)).isEqualTo(6);
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
        assertThat(game.play("b a1")).isTrue();
        assertThat(game.play("w b1")).isTrue();
        assertThat(game.play("b a3")).isTrue();
        assertThat(game.play("w c1")).isTrue();
        assertThat(game.play("b b2")).isTrue();
        assertThat(game.play("w a2")).isTrue();  // eat, valid.
        assertThat(game.play("b a1")).isFalse(); // invalid.
        assertThat(game.play("b b3")).isTrue();
        assertThat(game.play("w a1")).isTrue();  // white can finish ko

        game = given9x9Game();
        assertThat(game.play("b a1")).isTrue();
        assertThat(game.play("w b1")).isTrue();
        assertThat(game.play("b a3")).isTrue();
        assertThat(game.play("w c1")).isTrue();
        assertThat(game.play("b b2")).isTrue();
        assertThat(game.play("w a2")).isTrue();  // eat, valid.
        assertThat(game.play("b a1")).isFalse(); // invalid.
        assertThat(game.play("b b3")).isTrue();
        assertThat(game.play("w d1")).isTrue();
        assertThat(game.play("b a1")).isTrue(); // now black can eat again
        assertThat(game.play("w a2")).isFalse(); // and white can´t eat at A2 because its a KO
    }

    //TODO: https://senseis.xmp.net/?PinwheelKo

    @Test
    public void twoConsecutiveBlackMovesAreNotValid()
    {
        game = given9x9Game();
        game.play("black A3");
        assertThat(game.play("black G5")).isFalse();
    }

    @Test
    public void twoConsecutiveWhiteMovesAreNotValid()
    {
        game = given9x9Game();
        game.play("black A3");
        game.play("white A6");
        assertThat(game.play("white F2")).isFalse();
    }

    @Test
    public void simpleFinishGame()
    {
        game = given9x9Game();
        game.play("black a1");
        game.play("white pass");
        game.play("black pass");
        assertThat(game.finished()).isTrue();
    }

    @Test
    public void shortestGame()
    {
        game = given9x9Game();
        game.play("black pass");
        game.play("white pass");
        assertThat(game.finished()).isTrue();
    }

    @Test
    public void notSoSimpleFinishGame()
    {
        game = given19x19Game();
        assertAllValid(game, Lists.newArrayList("black a1", "white pass", "black g10", "white f10", "black pass", "white pass"));
        assertThat(game.finished()).isTrue();
    }


    @Test
    public void invalidHandicaps()
    {
        for (byte size = 3; size < (byte) 30; size++)
        {
            final byte sizeToUse = size;
            assertThrows(IllegalArgumentException.class, () -> new Game(sizeToUse, (byte) 1, (byte) 55));
            for (byte handicap = 10; handicap < 20; handicap++)
            {
                final byte handicapToUse = handicap;
                assertThrows(IllegalArgumentException.class, () -> new Game(sizeToUse, handicapToUse, (byte) 55));
            }
        }
    }

    @Test
    public void validHandicapsFor9x9()
    {
        game = new Game((byte) 9, (byte) 2, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 9, (byte) 3, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7", "B G3");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 9, (byte) 4, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7", "B G3", "B C7");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 9, (byte) 5, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7", "B G3", "B C7", "B E5");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 9, (byte) 6, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7", "B G3", "B C7", "B C5", "B G5");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 9, (byte) 7, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7", "B G3", "B C7", "B C5", "B G5", "B E5");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 9, (byte) 8, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7", "B G3", "B C7", "B C5", "B G5", "B E7", "B E3");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 9, (byte) 9, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B C3", "B G7", "B G3", "B C7", "B C5", "B G5", "B E7", "B E3", "B E5");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);
    }

    @Test
    public void validHandicapsFor13x13()
    {
        game = new Game((byte) 13, (byte) 2, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 13, (byte) 3, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10", "B D4");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 13, (byte) 4, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10", "B D4", "B D10");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 13, (byte) 5, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10", "B D4", "B D10", "B G7");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 13, (byte) 6, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10", "B D4", "B D10", "B D7", "B K7");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 13, (byte) 7, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10", "B D4", "B D10", "B D7", "B K7", "B G7");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 13, (byte) 8, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10", "B D4", "B D10", "B D7", "B K7", "B G4", "B G10");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 13, (byte) 9, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B K10", "B D4", "B D10", "B D7", "B K7", "B G4", "B G10", "B G7");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);
    }

    @Test
    public void validHandicapsFor19x19()
    {
        game = new Game((byte) 19, (byte) 2, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 19, (byte) 3, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16", "B Q4");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 19, (byte) 4, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16", "B Q4", "B D16");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 19, (byte) 5, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16", "B Q4", "B D16", "B K10");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 19, (byte) 6, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16", "B Q4", "B D16", "B D10", "B Q10");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 19, (byte) 7, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16", "B Q4", "B D16", "B D10", "B Q10", "B K10");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 19, (byte) 8, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16", "B Q4", "B D16", "B D10", "B Q10", "B K4", "B K16");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);

        game = new Game((byte) 19, (byte) 9, (byte) 55);
        assertBoardOnlyContains(game.getBoard(), "B D4", "B Q16", "B Q4", "B D16", "B D10", "B Q10", "B K4", "B K16", "B K10");
        assertThat(game.playerToPlay()).isEqualTo(Color.WHITE);
    }


    /* --------------------------------------------------------------------------------------------------------------------------------- */

    private void assertBoardOnlyContains(final Board board, String... coords)
    {
        for (int i = 0; i < coords.length; i++)
        {
            final short move = Move.parseToVal(coords[i]);
            assertThat(board.get(move)).isEqualTo(Move.color(move));
        }
        assertThat(board.count(Color.WHITE) + board.count(Color.BLACK)).isEqualTo(coords.length);
    }

    private void assertAllValid(final Game game, Collection<String> moves)
    {
        moves.forEach(move -> assertThat(game.play(move)).isTrue());
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
