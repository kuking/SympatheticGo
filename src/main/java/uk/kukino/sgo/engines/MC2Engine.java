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

import static uk.kukino.sgo.util.TUtils.*;

public class MC2Engine extends BaseEngine
{
    private TimeManager timeManager;

    private TTable ttable;
    private Buffers<Game> gameBuffers;
    private byte maxDepth = 5;
    private short[] validMoves;
    private int tickNo = 0;

    private int seedingSize = 500;
    private int uctTopCandidates = 1; // 10
    private int expansionThreshold = 5;
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
//            .maxMoveSecs(15)
            .maxMovePlys(100_000)
            .enoughConfidence(0.99f);
    }


    @Override
    public short genMove(final Color color)
    {
        if (game.getBoard().size() == 9 && ttable.getCoordByMove9x9() != null && ttable.getCoordByMove9x9().get(game.lastMove()) != null)
        {
            System.err.println(ttable.getCoordByMove9x9().get(game.lastMove()));
        }
        final short move;

        mcts();

        System.err.println("Final UTC Heatmap");
        System.err.println(ttable.uctHeatmap(game, game.playerToPlay()));
        System.err.println("Final Tops Moves");
        System.err.println(ttable.topsHeatmap(game, game.playerToPlay()));

        final int idx = bestMove(this.game, color);
        if (idx == -1)
        {
            System.err.println("No best move found! Resign!");
            move = Move.resign(color); //BUG!
        }
        else if (ttable.lastRatiosSorted()[idx] < minWinRatioForResign)
        {
            move = Move.resign(color);
        }
        else if (ttable.lastRatiosSorted()[idx] > minWinRatioForPass)
        {
            move = Move.pass(color);
        }
        else
        {
            final short[] moves = ttable.topsFor(game, boardSize * boardSize + 1, color);
            move = moves[idx]; //do better instead of ttable.topsFor twice
        }
        debugln("genMove " + Move.shortToString(move) + " with confidence " + (idx < 0 ? "Unknown" : ttable.lastRatiosSorted()[idx]) + ".");

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
        tickNo = 0;
        ttable.clearInterest();
        timeManager.newGenerateMove();
        final short[] exploration = new short[boardSize * boardSize * boardSize];
        exploration[0] = Move.INVALID;
        while (!timeManager.tick(0, 0.1f))
        {
            tickNo++;
            mcts(game, exploration, 0);
        }
        debugln("");
    }

    private long mcts(final Game game, final short[] exploration, final int explorationIdx)
    {
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

            if (!ttable.contains(game)) // never been here, it needs to seed with some random moves for UTC
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
                final short move = validMoves[m];
                int moveBlackWins = 0;
                int moveWhiteWins = 0;
                for (int s = 0; s < lotSize; s++)
                {
                    game.copyTo(copy);
                    copy.play(move);
                    copy.finishRandomly();
                    final Color winner = copy.simpletonWinnerUsingChineseRules();
                    if (Color.BLACK == winner)
                    {
                        blackWins++;
                        moveBlackWins++;
                    }
                    else
                    {
                        whiteWins++;
                        moveWhiteWins++;
                    }
                }
                if (account)
                {
                    if (Move.color(move) == Color.WHITE)
                    {
                        ttable.account(game, move, moveWhiteWins, moveBlackWins);
                    }
                    else if (Move.color(move) == Color.BLACK)
                    {
                        ttable.account(game, move, moveBlackWins, moveWhiteWins);
                    }
                    else
                    {
                        throw new IllegalStateException("game.validMoves() returned a non black/white move?");
                    }
                }
            }
            timeManager.tick(validMovesQ * lotSize, 0f);

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

        debug(" UCT " + tickNo + "; " + timeManager + "; Candidates: ");
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
