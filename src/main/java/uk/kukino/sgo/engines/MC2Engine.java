package uk.kukino.sgo.engines;

import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.util.Buffers;
import uk.kukino.sgo.mc.TTable;

import static uk.kukino.sgo.util.TUtils.cutOnFirstInvalid;
import static uk.kukino.sgo.util.TUtils.printMoves;

public class MC2Engine extends BaseEngine
{
    private TTable ttable;
    private Buffers<Game> gameBuffers;
    private byte levels = 2;
    private byte wide = 9 * 9; // terrible!
    private short[] validMoves;


    private int uctSeedingLotSize = 1000;
    private int uctTopCandidates = 15;
    private int uctCandidateLotSize = 200;
    private int uctIterationsPerGenMove = 50;
    private float uctFactor = 2f;


    public MC2Engine()
    {
        super("MC2", "v0.1b");
    }

    @Override
    protected void resetGame()
    {
        super.resetGame();
        ttable = new TTable(boardSize, levels, wide);
        validMoves = new short[boardSize * boardSize + 2];
        gameBuffers = new Buffers<>(100, () -> new Game(boardSize, handicap, komiX10));
    }

    @Override
    public short genMove(final Color color)
    {
        if (color != game.playerToPlay())
        {
            System.err.println("COLOR NOT EXPECTED");
            return Coord.INVALID;
        }
        playouts();

        final short[] moves = ttable.topsFor(game, 10, game.playerToPlay());
        printMoves(System.err, cutOnFirstInvalid(moves));

        final short move = moves[0];
        //        final short move = findBestFor(game.playerToPlay()); // this feels wrong....

        System.err.println("Looks like the move is going to be " + Move.shortToString(move));
        game.play(move);
        System.err.println(game);
        return move;
    }

    private void playouts()
    {

        if (ttable.playoutsFor(game) < 2)
        {
            System.err.println("Seeding UCT");
            seedUct(game);
        }
        for (int i = 0; i < uctIterationsPerGenMove; i++)
        {
            System.err.print("UCT " + i + "/" + uctIterationsPerGenMove);
            exploreUtc(game);
        }
    }

    private void seedUct(final Game game)
    {
        final Game copy = gameBuffers.lease();
        final int validMovesQ = game.validMoves(validMoves);
        try
        {
            for (int m = 0; m < validMovesQ; m++)
            {
                for (int s = 0; s < uctSeedingLotSize; s++)
                {
                    final short move = validMoves[m];
                    game.copyTo(copy);
                    copy.play(move);
                    copy.finishRandomly();
                    final Color winner = copy.simpletonWinnerUsingChineseRules();
                    ttable.account(game, move, winner == Move.color(move) ? 1 : 0, winner == Move.color(move) ? 0 : 1);
                }
            }
        }
        finally
        {
            gameBuffers.ret(copy);
        }
    }

    private void exploreUtc(final Game game)
    {
        final Game uctBaseCopy = gameBuffers.lease();
        final Game copy = gameBuffers.lease();
        try
        {
            final short[] uctCandidates = ttable.uct(game, uctTopCandidates, game.playerToPlay(), uctFactor);


            System.err.print("; Candidates: ");
            for (int uctIdx = 0; uctIdx < uctCandidates.length && uctCandidates[uctIdx] != Move.INVALID; uctIdx++)
            {
                final short uctMove = uctCandidates[uctIdx];
                System.err.print(Coord.shortToString(uctMove) + ", ");
            }
            System.err.println();

            for (int uctIdx = 0; uctIdx < uctCandidates.length && uctCandidates[uctIdx] != Move.INVALID; uctIdx++)
            {
                final short uctMove = uctCandidates[uctIdx];
                for (int c = 0; c < uctTopCandidates; c++)
                {
                    game.copyTo(uctBaseCopy);
                    if (!uctBaseCopy.play(uctMove))
                    {
                        throw new IllegalStateException("uct is retuning invalid moves! Are we seeding it wrong?");
                    }

                    for (int s = 0; s < uctCandidateLotSize; s++)
                    {
                        uctBaseCopy.copyTo(copy);
                        copy.finishRandomly();
                        final Color winner = copy.simpletonWinnerUsingChineseRules();
                        ttable.account(game, uctMove, winner == Move.color(uctMove) ? 1 : 0, winner == Move.color(uctMove) ? 0 : 1);
                    }
                }
            }
        }
        finally
        {
            gameBuffers.ret(uctBaseCopy);
            gameBuffers.ret(copy);
        }
    }


}
