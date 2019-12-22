package uk.kukino.sgo.base;

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
    private long[] paintBuf;
    private int[] superKos;
    private int lastSuperKoP;
    private boolean finished;

    // workplace
    private final Board altBoard;
    private final Board chainLibertyBoard;


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
        blackDeaths = 0;
        whiteDeaths = 0;
        moves = new short[(size * size) + (3 * size)]; // based on: https://homepages.cwi.nl/~aeb/go/misc/gostat.html
        lastMove = 0;
        Arrays.fill(moves, Move.INVALID);
        superKos = new int[size * 2];
        Arrays.fill(superKos, Move.INVALID);
        lastSuperKoP = 0;
        paintBuf = new long[size * size];
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
     * @param base base board
     * @param coord coordinate to start
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
        long adjs = Adjacent.asVal(coord, base.size());
        while (Adjacent.iterHasNext(adjs))
        {
            final short adjCoord = Adjacent.iterPosition(adjs);
            final Color baseAdjColor = base.get(adjCoord);
            if (baseAdjColor == Color.EMPTY)
            {
                return true;
            }
            else if (baseAdjColor == color && chainLibertyBoard.get(adjCoord) == Color.EMPTY)
            {
                if (recursivePaint(base, adjCoord, color))
                {
                    return true;
                }
            }
            adjs = Adjacent.iterMoveNext(adjs);
        }
        return false;
    }

    private boolean nonRecursiveMarkChainAndLiberties(final Board base, final short firstCoord)
    {
        final Color color = base.get(firstCoord);
        if (color == Color.EMPTY)
        {
            return true;
        }
        chainLibertyBoard.clear();

        int paintBufIdx = 0;
        chainLibertyBoard.set(firstCoord, color);
        paintBuf[paintBufIdx] = Adjacent.asVal(firstCoord, base.size());

        while (paintBufIdx != -1) // points to the last usable position, easier so no constant maths
        {
            if (Adjacent.iterHasNext(paintBuf[paintBufIdx]))
            {
                final short adjCoord = Adjacent.iterPosition(paintBuf[paintBufIdx]);
                paintBuf[paintBufIdx] = Adjacent.iterMoveNext(paintBuf[paintBufIdx]);
                final Color baseAdjColor = base.get(adjCoord);
                if (baseAdjColor == Color.EMPTY)
                {
                    return true;
                }
                else if (baseAdjColor == color && chainLibertyBoard.get(adjCoord) == Color.EMPTY)
                {
                    paintBufIdx++;
                    chainLibertyBoard.set(adjCoord, color);
                    paintBuf[paintBufIdx] = Adjacent.asVal(adjCoord, base.size());
                }
            }
            else
            {
                paintBufIdx -= 1;
            }

        }
        return false; // all painted without finding an empty border in the base board, dead
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

            long adjs = board.adjacentsWithColor(move, playerToPlay.opposite());
            while (Adjacent.iterHasNext(adjs))
            {
                final short adj = Adjacent.iterPosition(adjs);
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
                adjs = Adjacent.iterMoveNext(adjs);
            }

            if (killsOccured)
            {
                if (isSuperKo(board.hashCode()))
                {
                    altBoard.copyTo(board);
                    return false;
                }
                superKos[lastSuperKoP++ % superKos.length] = altBoard.hashCode();
            }
            else
            {
                if (!Adjacent.iterHasNext(board.adjacentsWithColor(move, Color.EMPTY)))
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

        playerToPlay = playerToPlay.opposite();
        moves[lastMove++] = move;

        return true;
    }

    private boolean isSuperKo(final int hash)
    {
        for (int i = 0; i < lastSuperKoP % superKos.length; i++)
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
            final short move;
            if (invalidCount > board.size() / 2 ||
                blackDeaths > board.size() ||
                whiteDeaths > board.size() ||
                lastMove > moves.length - 3)
            {
                move = Move.pass(playerToPlay);
            }
            else
            {
                move = randomMove();
            }

            if (play(move))
            {
                invalidCount = 0;
            }
            else
            {
                invalidCount++;
            }
//            if (lastMove > moves.length - 6)
//            {
//                System.out.println(Coord.shortToString(move));
//                System.out.println(this);
//            }
        }
//        if (lastMove > moves.length - 6)
//        {
//            System.exit(1);
//        }
    }

    private short randomMove()
    {
        final short coord = board.emptyRandom();
        if (coord == Move.INVALID)
        {
            return Move.pass(playerToPlay);
        }
        else
        {
            return Move.move(coord, playerToPlay);
        }
    }

    @Override
    public String toString()
    {
        return board.toString() +
            "move: " + lastMove() + "  next: " + playerToPlay + "\n" +
            " Blk: " + blackDeaths + " Wht: " + whiteDeaths + "\n";
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

    public boolean finished()
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
