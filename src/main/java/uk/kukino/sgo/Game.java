package uk.kukino.sgo;

import java.util.Arrays;

public class Game
{

    // state
    private final Board board;
    private final byte handicap;
    private final byte komi;
    private Color playerToPlay;
    private int lastMove;
    private int blackDeaths;
    private int whiteDeaths;
    private short[] moves;
    private int[] superKos;
    private int lastSuperKoP;
    private boolean finished;

    // workplace
    private final Board altBoard;
    private final Board chainLibertyBoard;
    private final Buffers<short[]> adjacentBuffers;

    private static short[] HANDICAP_19 = new short[] {
        Move.parseToVal("B D4"), Move.parseToVal("B Q16"), Move.parseToVal("B D16"),
        Move.parseToVal("B Q4"), Move.parseToVal("B D10"), Move.parseToVal("B Q10"),
        Move.parseToVal("B K4"), Move.parseToVal("B K16"), Move.parseToVal("B K10")
    };

    public Game(final byte size, final byte handicap, final byte komiX10)
    {
        board = new Board(size);
        altBoard = new Board(size);
        chainLibertyBoard = new Board(size);
        adjacentBuffers = new Buffers<>(size * 4, () -> new short[4]);
        moves = new short[size * size];
        blackDeaths = 0;
        whiteDeaths = 0;
        Arrays.fill(moves, Move.INVALID);
        superKos = new int[size * size];
        lastSuperKoP = 0;
        finished = false;

        komi = komiX10;
        this.handicap = handicap;
        initializeHandicap();
        lastMove = 0;
    }

    private void initializeHandicap()
    {
        if (handicap == (byte) 0)
        {
            this.playerToPlay = Color.BLACK;
        }
        else
        {
            if (board.size() != 19)
            {
                throw new IllegalArgumentException("I am sorry, I know only to deal handicap for 19x19 boards.");
            }
            for (int i = 0; i < handicap; i++)
            {
                board.set(HANDICAP_19[i]);
            }
            this.playerToPlay = Color.WHITE;
        }

    }

    private void markChainAndLiberties(final Board base, final short coord)
    {
        chainLibertyBoard.clear();
        final Color color = base.get(coord);

        if (color == Color.EMPTY)
        {
            return;
        }
        recursivePaint(base, coord, color);
    }

    private void recursivePaint(final Board base, final short coord, final Color color)
    {
        if (base.get(coord) != color)
        {
            return;
        }
        chainLibertyBoard.set(Move.move(coord, color));
        final short[] adj = adjacentBuffers.lease();
        try
        {
            byte adjN = base.adjacentsWithColor(adj, coord, Color.EMPTY);
            for (byte i = 0; i < adjN; i++)
            {
                chainLibertyBoard.set(Move.move(adj[i], Color.MARK));
            }
            adjN = base.adjacentsWithColor(adj, coord, color);
            for (byte i = 0; i < adjN; i++)
            {
                if (chainLibertyBoard.get(adj[i]) == Color.EMPTY)
                {
                    recursivePaint(base, adj[i], color);
                }
            }
        }
        finally
        {
            adjacentBuffers.ret(adj);
        }
    }

    private boolean isOKMove(final short move)
    {

        if (Move.color(move) != playerToPlay)
        {
            return false;
        }
        if (finished)
        {
            return false;
        }
        if (Move.isPass(move))
        {
            return true;
        }

        // from now on, all stone checks
        if (Move.x(move) >= board.size())
        {
            return false;
        }
        if (Move.y(move) >= board.size())
        {
            return false;
        }
        if (board.get(Move.x(move), Move.y(move)) != Color.EMPTY)
        {
            return false;
        }


        final short[] adjs = adjacentBuffers.lease();
        try
        {
            byte adjsN = board.adjacentsWithColor(adjs, move, Color.EMPTY);
            if (adjsN > 0)
            {
                return true;
            }

            adjsN = board.adjacentsWithColor(adjs, move, playerToPlay.opposite());
            for (int i = 0; i < adjsN; i++)
            {
                markChainAndLiberties(board, adjs[i]);
                if (chainLibertyBoard.count(Color.MARK) == 1)
                {
                    return true;
                }
            }


            adjsN = board.adjacentsWithColor(adjs, move, playerToPlay);
            if (adjsN == 0)
            {
                board.copyTo(altBoard);
                altBoard.set(move);
                markChainAndLiberties(altBoard, move);
                if (chainLibertyBoard.count(Color.MARK) == 0)
                {
                    return false;
                    // unless kills
                }
                return true;
            }
            else
            {
                for (int i = 0; i < adjsN; i++)
                {
                    markChainAndLiberties(board, adjs[i]);
                    if (chainLibertyBoard.count(Color.MARK) > 1)
                    { // +1 because 1 liberty is the current pos
                        return true;
                    }
                }
                return false;
            }
        }
        finally
        {
            adjacentBuffers.ret(adjs);
        }
    }

    public boolean play(final short move)
    {
        if (!isOKMove(move))
        {
            return false;
        }
        boolean killsOccured = false;

        if (Move.isPass(move))
        {
            if (lastMove > 0 && Move.isPass(moves[lastMove - 1]))
            {
                finished = true;
            }
        }
        else if (Move.isStone(move))
        {
            int moveKills = 0;
            board.set(move);

            final short[] adjs = adjacentBuffers.lease();
            final byte adjsN = board.adjacentsWithColor(adjs, move, playerToPlay.opposite());
            for (int i = 0; i < adjsN; i++)
            {
                markChainAndLiberties(board, adjs[i]);
                if (chainLibertyBoard.count(Color.MARK) == 0) //killed
                {
                    if (!killsOccured)
                    {
                        board.copyTo(altBoard); // lazy makes a copy of the board, just in case it has to be rollback for a superKo
                        altBoard.set(Move.x(move), Move.y(move), Color.EMPTY);
                        killsOccured = true;
                    }
                    moveKills += board.extract(chainLibertyBoard);
                }
            }
            adjacentBuffers.ret(adjs);

            if (killsOccured)
            {
                if (isSuperKo(board.hashCode()))
                {
                    altBoard.copyTo(board);
                    return false;
                }
                superKos[lastSuperKoP++] = altBoard.hashCode();
            }

            // latest accounts and moves
            if (playerToPlay == Color.WHITE)
            {
                blackDeaths += moveKills;
            }
            else
            {
                whiteDeaths = moveKills;
            }
        }

        playerToPlay = playerToPlay.opposite();
        moves[lastMove++] = move;

        return true;
    }

    private boolean isSuperKo(final int hash)
    {
        for (int i = 0; i < lastSuperKoP; i++)
        {
            if (superKos[i] == hash)
            {
                return true;
            }
        }
        return false;
    }

    /*
        Below the typical public methods for a Game
     */

    public byte getHandicap()
    {
        return handicap;
    }

    public byte getKomiX10()
    {
        return komi;
    }

    public Color playerToPlay()
    {
        return playerToPlay;
    }

    public boolean play(final CharSequence move)
    {
        return play(Move.parseToVal(move));
    }

    public int deadStones(final Color color)
    {
        if (color == Color.WHITE)
        {
            return whiteDeaths;
        }
        else if (color == Color.BLACK)
        {
            return blackDeaths;
        }
        throw new IllegalArgumentException("I don't count dead stones for: " + color);
    }

    boolean finished()
    {
        return finished;
    }

    public Board getBoard()
    {
        return board;
    }

    public int lastMove()
    {
        return lastMove;
    }

}
