package uk.kukino.sgo;

public class Game
{

    // state
    private final Board board;
    private final byte handicap;
    private final byte komi;
    private Color playerToPlay;
    private int lastMove;

    // workplace
    private final Board altBoard;
    private final Board chainLibertyBoard;
    private final Buffers<short[]> adjacentBuffers;

    private static short[] HANDICAP_19 = new short[] {
        Move.parseToVal("B D4"), Move.parseToVal("B Q16"), Move.parseToVal("B D16"),
        Move.parseToVal("B Q4"), Move.parseToVal("B D10"), Move.parseToVal("B Q10"),
        Move.parseToVal("B K4"), Move.parseToVal("B K16"), Move.parseToVal("B K10")
    };


    Game(final byte size, final byte handicap, final byte komiX10)
    {
        board = new Board(size);
        altBoard = new Board(size);
        chainLibertyBoard = new Board(size);
        adjacentBuffers = new Buffers<>(size * 4, () -> new short[4]);

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
                recursivePaint(base, adj[i], color);
            }
        }
        finally
        {
            adjacentBuffers.ret(adj);
        }
    }

    boolean isValidMove(final short move)
    {
        if (Move.color(move) != playerToPlay)
        {
            return false;
        }
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

    boolean play(final short value)
    {
        if (!isValidMove(value))
        {
            return false;
        }

        board.set(value);

        playerToPlay = playerToPlay.opposite();
        lastMove++;

        return true;
    }

    boolean play(final CharSequence move)
    {
        return play(Move.parseToVal(move));
    }

    int deadStones(final Color color)
    {
        return 0;
    }

    boolean finished()
    {
        return false;
    }

    Board getBoard()
    {
        return board;
    }

    int lastMove()
    {
        return lastMove;
    }


}
