package uk.kukino.sgo.engines;

import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.mc.CanadianTimeRules;
import uk.kukino.sgo.mc.TTable;
import uk.kukino.sgo.mc.TimeManager;
import uk.kukino.sgo.util.Buffers;

import java.util.concurrent.atomic.AtomicInteger;

import static uk.kukino.sgo.util.TUtils.cutOnFirstInvalid;
import static uk.kukino.sgo.util.TUtils.logMoves;

public class MC2Engine extends BaseEngine
{
    private TimeManager timeManager;

    private TTable ttable;
    private Buffers<Game> gameBuffers;
    private byte levels = 2;
    private byte wide = 9 * 9; // terrible!
    private short[] validMoves;


    private final AtomicInteger genMovePlys = new AtomicInteger(0);

    private int uctSeedingLotSize = 1000;
    private int uctTopCandidates = 10;
    private int uctCandidateLotSize = 50;
    private float uctFactor = 2f;


    public MC2Engine()
    {
        super("MC2", "v0.1b");
        timeManager = canadianTimeRulesBuilder().build();
    }

    @Override
    protected void resetGame()
    {
        super.resetGame();
        ttable = new TTable(boardSize, levels, wide);
        validMoves = new short[boardSize * boardSize + 2];
        gameBuffers = new Buffers<>(100, () -> new Game(boardSize, handicap, komiX10));
        timeManager = canadianTimeRulesBuilder()
            .boardSize(boardSize)
            .build();
    }

    private CanadianTimeRules canadianTimeRulesBuilder()
    {
        return CanadianTimeRules.builder()
            .maxMoveSecs(30)
            .enoughConfidence(0.99f);
    }

    @Override
    public short genMove(final Color color)
    {
        genMovePlys.set(0);
        playouts();
        final short[] moves = ttable.topsFor(game, 1, game.playerToPlay());
        final short move = moves[0];
        debugln("genMove " + Move.shortToString(move));
        game.play(move);
        debugln(game.toString());
        return move;
    }

    private void playouts()
    {
        timeManager.newGenerateMove();
        seedUct(game);
        exploreUtc(game);
    }

    private void seedUct(final Game game)
    {
        debugln("Seeding UCT ...");
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
            genMovePlys.addAndGet(validMovesQ * uctSeedingLotSize);
        }
        finally
        {
            gameBuffers.ret(copy);
        }
    }

    private void exploreUtc(final Game game)
    {
        debugln("Exploring UTC ...");
        final Game copy = gameBuffers.lease();
        try
        {
            int tickNo = 0;
            while (!timeManager.tick(genMovePlys.get(), 0.1f))
            {
                debug("UCT " + tickNo + "; " + timeManager + "; Candidates: ");
                logMoves(this::debug, cutOnFirstInvalid(ttable.uct(game, uctTopCandidates, game.playerToPlay(), uctFactor)));
                debug(" Top 3: ");
                logMoves(this::debug, cutOnFirstInvalid(ttable.topsFor(game, 3, game.playerToPlay())));
                debugln("");


                final short[] uctCandidates = ttable.uct(game, uctTopCandidates, game.playerToPlay(), uctFactor);

                for (int uctIdx = 0; uctIdx < uctCandidates.length && uctCandidates[uctIdx] != Move.INVALID; uctIdx++)
                {
                    final short uctMove = uctCandidates[uctIdx];
                    for (int c = 0; c < uctTopCandidates; c++)
                    {
                        for (int s = 0; s < uctCandidateLotSize; s++)
                        {
                            game.copyTo(copy);
                            if (!copy.play(uctMove))
                            {
                                throw new IllegalStateException("uct is retuning invalid moves! Are we seeding it wrong?");
                            }
                            copy.finishRandomly();
                            final Color winner = copy.simpletonWinnerUsingChineseRules();
                            ttable.account(game, uctMove, winner == Move.color(uctMove) ? 1 : 0, winner == Move.color(uctMove) ? 0 : 1);
                        }
                    }
                }
                tickNo++;
            }
        }
        finally
        {
            gameBuffers.ret(copy);
        }
    }


}
