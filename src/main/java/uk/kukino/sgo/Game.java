package uk.kukino.sgo;

import net.openhft.chronicle.bytes.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Game
{

    // state
    private final Board board;
    private byte handicap;
    private byte komi;
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
    private final Buffers<Bytes<ByteBuffer>> adjacentBuffers;


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
        adjacentBuffers = new Buffers<>(size * size / 2, () -> Bytes.elasticByteBuffer(2 * 4));
        blackDeaths = 0;
        whiteDeaths = 0;
        moves = new short[size * size * 2];
        lastMove = 0;
        Arrays.fill(moves, Move.INVALID);
        superKos = new int[(size * size) / 2];
        Arrays.fill(superKos, Move.INVALID);
        lastSuperKoP = 0;
        finished = false;

        komi = komiX10;
        this.handicap = handicap;
        initializeHandicap();
    }

    public void copyTo(final Game other)
    {
        board.copyTo(other.board);
        other.handicap = handicap;
        other.komi = komi;
        other.playerToPlay = playerToPlay;
        other.lastMove = lastMove;
        other.blackDeaths = blackDeaths;
        other.whiteDeaths = whiteDeaths;
        System.arraycopy(moves, 0, other.moves, 0, moves.length);
        System.arraycopy(superKos, 0, other.superKos, 0, superKos.length);
        other.lastSuperKoP = lastSuperKoP;
        other.finished = finished;
        // probably no at they are tmp: altBoard, chainLibertyBoard, adjacentBuffers;
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

    /***
     *
     * @param base
     * @param coord
     * @return true if alive (and partially marked)
     */
    private boolean markChainAndLiberties(final Board base, final short coord)
    {
        final Color color = base.get(coord);
        if (color == Color.EMPTY)
        {
            return true;
        }
        chainLibertyBoard.clear();
        return recursivePaint(base, coord, color);
    }


    private boolean recursivePaint(final Board base, final short coord, final Color color)
    {
        chainLibertyBoard.set(coord, color);
        final Bytes<ByteBuffer> adj = adjacentBuffers.lease();
        try
        {
            Coord.adjacents(adj, coord, base.size());
            while (!adj.isEmpty())
            {
                final short value = adj.readShort();
                final Color baseAdjColor = base.get(value);
                if (baseAdjColor == Color.EMPTY)
                {
                    // chainLibertyBoard.set(adj[i], Color.MARK);
                    return true;
                }
                else if (baseAdjColor == color && chainLibertyBoard.get(value) == Color.EMPTY)
                {
                    if (recursivePaint(base, value, color))
                    {
                        return true;
                    }
                }
            }
        }
        finally
        {
            adjacentBuffers.ret(adj);
        }
        return false;
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
        if (Move.X(move) >= board.size())
        {
            return false;
        }
        if (Move.Y(move) >= board.size())
        {
            return false;
        }
        return board.get(move) == Color.EMPTY;
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

            final Bytes<ByteBuffer> adjs = adjacentBuffers.lease();
            try
            {
                board.adjacentsWithColor(adjs, move, playerToPlay.opposite());
                while (!adjs.isEmpty())
                {
                    final short adj = adjs.readShort();
                    if (!markChainAndLiberties(board, adj)) //killed
                    {
                        if (!killsOccured)
                        {
                            board.copyTo(altBoard); // lazy makes a copy of the board, just in case it has to be rollback for a superKo
                            altBoard.set(Move.X(move), Move.Y(move), Color.EMPTY);
                            killsOccured = true;
                        }
                        moveKills += board.extract(chainLibertyBoard);
                    }
                }

                if (killsOccured)
                {
                    if (isSuperKo(board.hashCode()))
                    {
                        altBoard.copyTo(board);
                        return false;
                    }
                    superKos[lastSuperKoP++] = altBoard.hashCode();
                }
                else
                {
                    board.adjacentsWithColor(adjs, move, Color.EMPTY);
                    if (adjs.isEmpty())
                    {
                        if (!markChainAndLiberties(board, move)) // suicide?
                        {
                            board.set(Move.X(move), Move.Y(move), Color.EMPTY); //undo
                            return false;
                        }
                    }
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
            finally
            {
                adjacentBuffers.ret(adjs);
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

    public void finishRandomly()
    {
        int invalidCount = 0;
        while (!finished())
        {
            short move = Move.INVALID;
            if (invalidCount < board.size())
            {
                move = Move.random(board.size(), playerToPlay);
            }
            else
            {
                final int empties = board.count(Color.EMPTY);
                int npos = 0;
                if (empties > 0)
                {
                    npos = Move.RND.nextInt(empties);
                }
                for (byte x = 0; x < board.size() && npos != 0; x++)
                {
                    for (byte y = 0; y < board.size() && npos != 0; y++)
                    {
                        if (board.get(x, y) == Color.EMPTY)
                        {
                            npos--;
                        }
                        if (npos == 0)
                        {
                            move = Move.move(x, y, playerToPlay);
                            npos = -1;
                        }
                    }
                }
            }

            if (move == Move.INVALID || invalidCount > board.size() * 2 || lastMove > board.size() * board.size())
            {
                move = Move.pass(playerToPlay);
            }

            if (play(move))
            {
                invalidCount = 0;
            }
            else
            {
                invalidCount++;
            }
        }
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
