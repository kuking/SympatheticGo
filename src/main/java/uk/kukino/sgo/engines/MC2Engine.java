package uk.kukino.sgo.engines;

import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.mc.CanadianTimeRules;
import uk.kukino.sgo.mc.TTable;
import uk.kukino.sgo.mc.TimeManager;
import uk.kukino.sgo.util.Buffers;
import uk.kukino.sgo.util.IntIntAsLong;
import uk.kukino.sgo.valuators.Heatmaps;

import java.util.concurrent.atomic.AtomicInteger;

import static uk.kukino.sgo.util.TUtils.*;

public class MC2Engine extends BaseEngine
{
    private TimeManager timeManager;

    private TTable ttable;
    private Buffers<Game> gameBuffers;
    private byte maxDepth = 5;
    private short[] validMoves;


    private final AtomicInteger genMovePlys = new AtomicInteger(0);
    private final AtomicInteger tickNo = new AtomicInteger(0);

    private int seedingSize = 100;
    private int uctTopCandidates = 10;
    private int expansionThreshold = 3;
    private float uctFactor = 2f;
    private float minWinRatioForResign = 0.20f;
    private float minWinRatioForPass = 0.95f;

    public MC2Engine()
    {
        super("MC2", "v0.1b");
        timeManager = canadianTimeRulesBuilder().build();
    }

    @Override
    protected void resetGame()
    {
        super.resetGame();
        ttable = new TTable(boardSize, maxDepth, uctTopCandidates);
        ttable.setCoordByMove9x9(Heatmaps.loadFromResources("/9x9-coord-by-move-heatmap.xz"));
        validMoves = new short[boardSize * boardSize + 2];
        gameBuffers = new Buffers<>(100, () -> new Game(boardSize, handicap, komiX10));
        timeManager = canadianTimeRulesBuilder()
            .boardSize(boardSize)
            .build();
    }

    private CanadianTimeRules canadianTimeRulesBuilder()
    {
        return CanadianTimeRules.builder()
            .maxMoveSecs(15)
//            .maxMovePlys(1000_000_000)
            .enoughConfidence(0.99f);
    }


    @Override
    public short genMove(final Color color)
    {
        genMovePlys.set(0);

        if (ttable.getCoordByMove9x9() != null && ttable.getCoordByMove9x9().get(game.lastMove()) != null)
        {
            System.err.println(ttable.getCoordByMove9x9().get(game.lastMove()));
        }
        final short move;

        // pass here when there is a huge difference in points
        if (game.deadStones(color) - game.deadStones(color.opposite()) > boardSize)
        {
            move = Move.pass(color);
            debugln("genMove " + Move.shortToString(move) + " -- too much death stones, giving up.");
        }
        else
        {
            mcts();

            final int idx = bestMove(this.game, color);

            if (idx == -1)
            {
                move = Move.pass(color);
                debugln("genMove " + Move.shortToString(move) + " -- Couldn't find any good move.");
            }
            else if (ttable.lastRatiosSorted()[idx] < minWinRatioForResign ||
                ttable.lastRatiosSorted()[idx] > minWinRatioForPass)
            {
                move = Move.pass(color);
                debugln("genMove " + Move.shortToString(move) + " with confidence " + ttable.lastRatiosSorted()[idx] + " this is too good/bad");
            }
            else
            {
                final short[] moves = ttable.topsFor(game, boardSize * boardSize + 1, color);
                move = moves[idx]; //do better instead of ttable.topsFor twice
                debugln("genMove " + Move.shortToString(move) + " with confidence " + ttable.lastRatiosSorted()[idx]);
            }
        }
        game.play(move);

        debugln(game.toString());
        return move;
    }

    // this could be the canary ... i'm not sure this is necessary
    public int bestMove(final Game game, final Color color)
    {
        // the 'best' ratio could be a 0.1 win-rate ... therefore we need to be sure it is a valid move for us
        final Game copy = gameBuffers.lease();
        try
        {
            final short[] moves = ttable.topsFor(game, boardSize * boardSize + 1, color);
            for (int i = 0; i < moves.length && Move.isValid(moves[i]); i++)
            {
                game.copyTo(copy);
                if (copy.play(moves[i]))
                {
                    return i;
                }
            }
            return -1;
        }
        finally
        {
            gameBuffers.ret(copy);
        }
    }


    @Override
    public boolean play(final short move)
    {
        final boolean res = super.play(move);
        debugln("play " + Move.shortToString(move));
        debugln(game.toString());
        return res;
    }

    private void mcts()
    {
        tickNo.set(0);
        ttable.clearInterest();
        timeManager.newGenerateMove();
        final short[] exploration = new short[boardSize * boardSize * boardSize];
        exploration[0] = Move.INVALID;
        while (!timeManager.tick(genMovePlys.get(), 0.1f))
        {
            mcts(game, exploration, 0);
        }
        debugln("");
    }

    private long mcts(final Game game, final short[] exploration, final int explorationIdx)
    {
        tickNo.incrementAndGet();
        final Game copy = gameBuffers.lease();
        try
        {
            int blackWins = 0;
            int whiteWins = 0;
            if (game.finished())
            {
                if (game.simpletonWinnerUsingChineseRules() == Color.BLACK)
                {
                    blackWins++;
                }
                else
                {
                    whiteWins++;
                }
                return IntIntAsLong.enc(blackWins, whiteWins);
            }

            if (!ttable.contains(game))
            {
                final long wins = seed(game, seedingSize, true);
                blackWins += IntIntAsLong.left(wins);
                whiteWins += IntIntAsLong.right(wins);
            }
            logUtc(game, exploration, explorationIdx);

            final short[] uctMoves = ttable.uct(game, uctTopCandidates, game.playerToPlay(), uctFactor);
            boolean finish = false;
            for (int uctIdx = 0; !finish && uctIdx < uctMoves.length && uctMoves[uctIdx] != Move.INVALID; uctIdx++)
            {
                final short move = uctMoves[uctIdx];
                game.copyTo(copy);
                if (copy.play(move))
                {
                    final long wins;
                    if (ttable.markInterest(copy) > expansionThreshold)
                    {
                        exploration[explorationIdx] = move;
                        exploration[explorationIdx + 1] = Move.INVALID;
                        wins = mcts(copy, exploration, explorationIdx + 1);
                        exploration[explorationIdx] = Move.INVALID;
                        finish = true; // it should really do all of them...
                    }
                    else
                    {
                        wins = seed(copy, 1, false);
                    }
                    // account
                    final int black = IntIntAsLong.left(wins);
                    final int white = IntIntAsLong.right(wins);
                    blackWins += black;
                    whiteWins += white;
                    ttable.account(game, move, //GAME!
                        Move.color(move) == Color.BLACK ? black : white,
                        Move.color(move) == Color.BLACK ? white : black);
                }
            }
            return IntIntAsLong.enc(blackWins, whiteWins);
        }
        finally
        {
            gameBuffers.ret(copy);
        }
    }


    private Long seed(final Game game, final int lotSize, final boolean account)
    {
        final Game copy = gameBuffers.lease();
        final Game validMovesGameBuffer = gameBuffers.lease();
        try
        {
            int blackWins = 0;
            int whiteWins = 0;
            final int validMovesQ = game.validMoves(validMoves, validMovesGameBuffer);
            for (int m = 0; m < validMovesQ; m++)
            {
                for (int s = 0; s < lotSize; s++)
                {
                    final short move = validMoves[m];
                    game.copyTo(copy);
                    copy.play(move);
                    copy.finishRandomly();
                    final Color winner = copy.simpletonWinnerUsingChineseRules();
                    if (Color.BLACK == winner)
                    {
                        blackWins++;
                    }
                    else
                    {
                        whiteWins++;
                    }
                    if (account)
                    {
                        ttable.account(game, move, winner == Move.color(move) ? 1 : 0, winner == Move.color(move) ? 0 : 1);
                    }
                }
            }
            genMovePlys.addAndGet(validMovesQ * seedingSize);
            return IntIntAsLong.enc(blackWins, whiteWins);
        }
        finally
        {
            gameBuffers.ret(copy);
            gameBuffers.ret(validMovesGameBuffer);
        }
    }


    long lastLog = System.currentTimeMillis();

    private void logUtc(final Game game, final short[] exploration, final int explorationIdx)
    {
        if (System.currentTimeMillis() - lastLog < 1000)
        {
            return;
        }
        lastLog = System.currentTimeMillis();

        debug(" UCT " + tickNo.get() + "; " + timeManager + "; Candidates: ");
        logMoves(this::debug, cutOnFirstInvalid(ttable.uct(game, uctTopCandidates, game.playerToPlay(), uctFactor)));
        debug("[");
        logRatios(this::debug, ttable.lastRatiosSorted(), uctTopCandidates);
        debug("\b] -> ");
        logMoves(this::debug, cutOnFirstInvalid(exploration));
        debug("\b] -> Tops: ");
        logMoves(this::debug, cutOnFirstInvalid(ttable.topsFor(game, 3, game.playerToPlay())));
        debug("[");
        logRatios(this::debug, ttable.lastRatiosSorted(), 3);
        debug("         \r");
    }


}
