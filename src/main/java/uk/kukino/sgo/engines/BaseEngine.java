package uk.kukino.sgo.engines;

import uk.kukino.sgo.base.*;
import uk.kukino.sgo.gtp.Engine;

public abstract class BaseEngine implements Engine
{

    protected CharSequence name;
    protected CharSequence version;

    protected byte boardSize = 19;
    protected byte handicap = 0;
    protected byte komiX10 = 45;

    protected Game game;

    protected BaseEngine(final CharSequence name, final CharSequence version)
    {
        this.name = name;
        this.version = version;
        resetGame();
    }

    @Override
    public CharSequence name()
    {
        return name;
    }

    @Override
    public CharSequence version()
    {
        return version;
    }


    protected void resetGame()
    {
        game = new Game(boardSize, handicap, komiX10);
    }

    @Override
    public boolean setBoardSize(final byte size)
    {
        this.boardSize = size;
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
        resetGame();
    }

    @Override
    public boolean play(final short move)
    {
        return game.play(move);
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
    public short[] fixedHandicap(final int stones)
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
                if (board.get(x, y) != Color.EMPTY)
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

    }

}
