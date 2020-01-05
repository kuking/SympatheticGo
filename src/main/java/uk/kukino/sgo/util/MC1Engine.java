package uk.kukino.sgo.util;

import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.mc.Buffers;
import uk.kukino.sgo.mc.TTable;

public class MC1Engine extends BaseEngine
{

    private TTable ttable;
    private Buffers<Game> gameBuffers;
    private byte levels = 2;
    private byte wide = 9 * 9; // terrible!
    private int bottomRandom = 150;

    private long plys = 0;

    public MC1Engine()
    {
        super("MC1", "v0.1b");
    }

    @Override
    protected void resetGame()
    {
        super.resetGame();
        ttable = new TTable(boardSize, levels, wide);
        gameBuffers = new Buffers<>(100, () -> new Game(boardSize, handicap, komiX10));
    }

    @Override
    public short genMove(final Color color)
    {
        playouts();
        final short move = findBestFor(game.playerToPlay()); // this feels wrong....
//        final short best = ttable.topsFor(game.getBoard().hashCode(), 1, game.playerToPlay())[0];
//        if (Coord.isValid(best))
//        {
//            move = Move.move(best, game.playerToPlay());
//        }
//        else
//        {
//            move = Move.pass(game.playerToPlay());
//        }

        System.err.println("Looks like the move is going to be " + Move.shortToString(move));
        game.play(move);
        System.err.println(game);
        return move;
    }

    private short findBestFor(final Color playerToPlay)
    {
        final Game copy = gameBuffers.lease();
        try
        {
            final short[] tops = ttable.topsFor(game.getBoard().hashCode(), boardSize * boardSize, playerToPlay);
            for (int i = 0; i < tops.length; i++)
            {
                final short coord = tops[i];
                if (!Coord.isValid(coord))
                {
                    break; // nothing more to check
                }
                if (game.getBoard().get(coord) == Color.EMPTY)
                {
                    final short move = Move.move(coord, playerToPlay);
                    game.copyTo(copy);
                    if (copy.play(move))
                    {
                        return move; // a valid one then
                    }
                }
            }
            // if none is valid, goodbye
            return Move.pass(playerToPlay);
        }
        finally
        {
            gameBuffers.ret(copy);
        }
    }

    private void playouts()
    {
        playout(game, levels, bottomRandom);

    }

    private long playout(final Game game, final byte level, final int bottomRandoms)
    {
        final Game copy = gameBuffers.lease();
        int blackWins = 0;
        int whiteWins = 0;
        try
        {
            if (level == 0)
            {
                for (int i = 0; i < bottomRandoms; i++)
                {
                    plys++;
                    game.copyTo(copy);
                    copy.finishRandomly();
                    if (copy.simpletonWinnerUsingChineseRules() == Color.BLACK)
                    {
                        blackWins++;
                    }
                    else
                    {
                        whiteWins++;
                    }
                    if (plys % 100000 == 0)
                    {
                        System.err.println(plys + " plys ...");
                    }
                }
            }
            else
            {
                for (byte x = 0; x < boardSize; x++)
                {
                    for (byte y = 0; y < boardSize; y++)
                    {
                        if (game.getBoard().get(x, y) == Color.EMPTY)
                        {
                            game.copyTo(copy);
                            final int hash = copy.getBoard().hashCode();
                            final short coord = Coord.XY(x, y);
                            final short move = Move.move(coord, copy.playerToPlay());
                            if (copy.play(move))
                            {
                                final long wins = playout(copy, (byte) (level - 1), bottomRandoms);
                                ttable.account(hash, coord, Color.BLACK, IntIntAsLong.left(wins));
                                ttable.account(hash, coord, Color.WHITE, IntIntAsLong.right(wins));
                                blackWins += IntIntAsLong.left(wins);
                                whiteWins += IntIntAsLong.right(wins);
                            }
                        }
                    }
                }
            }
        }
        finally
        {
            gameBuffers.ret(copy);
        }
        return IntIntAsLong.enc(blackWins, whiteWins);
    }
}
