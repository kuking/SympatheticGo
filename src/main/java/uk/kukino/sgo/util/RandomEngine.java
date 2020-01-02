package uk.kukino.sgo.util;

import uk.kukino.sgo.base.*;
import uk.kukino.sgo.gtp.Engine;

public class RandomEngine implements Engine
{

    private byte boardSize = 19;
    private byte handicap = 0;
    private byte komiX10 = 45;

    private Game game;

    public RandomEngine()
    {
        resetGame();
    }

    private void resetGame()
    {
        game = new Game(boardSize, handicap, komiX10);
    }

    @Override
    public CharSequence name()
    {
        return "RandomEngine";
    }

    @Override
    public CharSequence version()
    {
        return "1.0";
    }

    @Override
    public boolean setBoardSize(final byte size)
    {
        boardSize = size;
        resetGame();
        return true;
    }

    @Override
    public void clearBoard()
    {
        resetGame();
    }

    @Override
    public void setKomi(final float komi)
    {
        this.komiX10 = (byte) (komi * 10f);
        //FIXME: game won't change ...
    }

    @Override
    public boolean play(final short move)
    {
        return game.play(move);
    }

    @Override
    public short genMove(final Color color)
    {
        final Game tmp = new Game(boardSize, handicap, komiX10);
        for (int i = 0; i < 1000; i++)
        {
            game.copyTo(tmp);
            final short candidate = Move.random(boardSize, tmp.playerToPlay());
            if (tmp.play(candidate))
            {
                game.play(candidate);
                return candidate;
            }
        }
        return Move.pass(game.playerToPlay());
    }

    @Override
    public boolean undo()
    {
        return false;
    }

    @Override
    public CharSequence displayableBoard()
    {
        return game.toString();
    }

    @Override
    public boolean setTimeSettings(final int mainTimeSecs, final int byoYomiSecs, final int byoYomiStones)
    {
        return false;
    }

    @Override
    public short[] fixedHandicap(int stones)
    {
        this.handicap = (byte) stones;
        final Board board = game.getBoard();
        final short[] res = new short[stones];
        int resI = 0;
        resetGame();
        for (byte x = 0; x < boardSize; x++)
        {
            for (byte y = 0; y < boardSize; y++)
            {
                if (game.getBoard().get(x, y) != Color.EMPTY)
                {
                    res[resI++] = Coord.XY(x, y);
                }
            }
        }
        return res;
    }

    @Override
    public int calculateFinalScore()
    {
        return Score.UNKNOWN;
    }

    @Override
    public void shutdown()
    {
        // easy
    }
}
