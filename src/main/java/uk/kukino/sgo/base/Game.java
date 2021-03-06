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
    private int[] paintBuf;
    private int[] superKos;
    private int lastSuperKoP;
    private boolean finished;

    // workplace
    private final Board stashBoard;
    private final Board chainLibertyBoard;


    public Game(final byte size, final byte handicap, final byte komiX10)
    {
        board = new Board(size);
        stashBoard = new Board(size);
        chainLibertyBoard = new Board(size);
        blackDeaths = 0;
        whiteDeaths = 0;
        moves = new short[(size * size) + (3 * size)]; // based on: https://homepages.cwi.nl/~aeb/go/misc/gostat.html
        lastMove = 0;
        Arrays.fill(moves, Move.INVALID);
        superKos = new int[size * 2];
        Arrays.fill(superKos, Move.INVALID);
        lastSuperKoP = 0;
        paintBuf = new int[size * size];
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
        if (other.moves.length != moves.length)
        {
            other.moves = new short[moves.length]; // this was extended, so the other has to ..
        }
        System.arraycopy(moves, 0, other.moves, 0, lastMove);
        System.arraycopy(superKos, 0, other.superKos, 0, Math.min(lastSuperKoP, superKos.length)); // superKos is a ring-buffer
        other.lastSuperKoP = lastSuperKoP;
        other.finished = finished;
        // probably no at they are tmp: stashBoard, chainLibertyBoard
    }

    private void initializeHandicap()
    {
        if (handicap == 1 || handicap > 9)
        {
            throw new IllegalArgumentException("Invalid Handicap, must satisfy: 1 < handicap < 10");
        }
        if (handicap == (byte) 0)
        {
            this.playerToPlay = Color.BLACK;
        }
        else
        {
            if (board.size() == 9)
            {
                setHighlightsAsHandicap(Coord.HIGHLIGHTS_9X9, handicap);
            }
            else if (board.size() == 13)
            {
                setHighlightsAsHandicap(Coord.HIGHLIGHTS_13X13, handicap);
            }
            else if (board.size() == 19)
            {
                setHighlightsAsHandicap(Coord.HIGHLIGHTS_19X19, handicap);
            }
            else
            {
                throw new IllegalArgumentException("I don't know how do initialise the handicap board for board size: " + board.size());
            }
            this.playerToPlay = Color.WHITE;
        }
    }

    private void setHighlightsAsHandicap(final short[] highlights, final byte handicap)
    {
        if (handicap >= 2)
        {
            board.set(highlights[Coord.NE], Color.BLACK);
            board.set(highlights[Coord.SW], Color.BLACK);
        }
        if (handicap >= 3)
        {
            board.set(highlights[Coord.SE], Color.BLACK);
        }
        if (handicap >= 4)
        {
            board.set(highlights[Coord.NW], Color.BLACK);
        }
        if (handicap == 5 | handicap == 7 | handicap == 9)
        {
            board.set(highlights[Coord.C], Color.BLACK);
        }
        if (handicap >= 6)
        {
            board.set(highlights[Coord.W], Color.BLACK);
            board.set(highlights[Coord.E], Color.BLACK);
        }
        if (handicap >= 8)
        {
            board.set(highlights[Coord.N], Color.BLACK);
            board.set(highlights[Coord.S], Color.BLACK);
        }

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
                    chainLibertyBoard.set(adjCoord, color);
                    paintBufIdx++;
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
        if (Move.isPass(move))
        {
            if (lastMove > 0 && Move.isPass(moves[lastMove - 1]))
            {
                //FIXME: it is not recording the last move in the move list
                finished = true;
            }
        }
        else if (Move.isStone(move))
        {
            int moveKills = 0;
            board.set(move);

            int adjs = board.adjacentsWithColor(move, playerToPlay.opposite());
            while (Adjacent.iterHasNext(adjs))
            {
                final short adj = Adjacent.iterPosition(adjs);
                if (!nonRecursiveMarkChainAndLiberties(board, adj)) //killed
                {
                    if (moveKills == 0)
                    {
                        // lazily makes a copy of the board, just in case it has to be rollback due to a superKo
                        board.copyTo(stashBoard);
                        stashBoard.set(Move.X(move), Move.Y(move), Color.EMPTY);
                    }
                    moveKills += board.extract(chainLibertyBoard);
                }
                adjs = Adjacent.iterMoveNext(adjs);
            }

            if (moveKills > 0)
            {
                if (isSuperKo(board.hashCode()))
                {
                    stashBoard.copyTo(board);
                    return false;
                }
                superKos[lastSuperKoP++ % superKos.length] = stashBoard.hashCode();
                if (playerToPlay == Color.WHITE)
                {
                    blackDeaths += moveKills;
                }
                else
                {
                    whiteDeaths += moveKills;
                }
            }
            else
            {
                if (!Adjacent.iterHasNext(board.adjacentsWithColor(move, Color.EMPTY)))
                {
                    if (!nonRecursiveMarkChainAndLiberties(board, move)) // suicide?
                    {
                        board.set(Move.X(move), Move.Y(move), Color.EMPTY); //undo
                        return false;
                    }
                }
            }
        }

        playerToPlay = playerToPlay.opposite();
        if (lastMove == moves.length) //resize, expensive but exceptional
        {
            final short[] oldMoves = moves;
            moves = new short[oldMoves.length * 2];
            System.arraycopy(oldMoves, 0, moves, 0, oldMoves.length);
        }
        moves[lastMove++] = move;

        return true;
    }

    private boolean isSuperKo(final int hash)
    {
        for (int i = 0; i < Math.min(lastSuperKoP, superKos.length); i++)
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
        final int tolerance = board.size() << 1;
        int invalidCount = 0;
        while (!finished())
        {
            final short move;
            if ((blackDeaths > tolerance && playerToPlay == Color.BLACK) ||
                (whiteDeaths > tolerance && playerToPlay == Color.WHITE))
            {
                move = Move.pass(playerToPlay);
            }
            else if (invalidCount > tolerance || lastMove > moves.length - 3)
            {
                move = Move.pass(playerToPlay);
            }
            else
            {
                move = randomMove();
            }

            if (!fillsOwnEye(move) && play(move))
            {
//                System.err.println(Move.shortToString(move));
//                System.err.println(this);
                invalidCount = 0;
            }
            else
            {
                invalidCount++;
            }
        }
    }

    private boolean fillsOwnEye(final short move)
    {
        if (Move.isStone(move))
        {
            final Color opposite = Move.color(move).opposite();
            int adjs = Adjacent.asVal(move, board.size());
            while (Adjacent.iterHasNext(adjs))
            {
                final Color adjCol = board.get(Adjacent.iterPosition(adjs));
                if (adjCol == Color.EMPTY || adjCol == opposite)
                {
                    return false;
                }
                adjs = Adjacent.iterMoveNext(adjs);
            }
            return true;
        }
        return false;
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

    public Color simpletonWinnerUsingChineseRules()
    {
        final int black = ((board.count(Color.BLACK) + whiteDeaths) * 10);
        final int white = ((board.count(Color.WHITE) + blackDeaths) * 10) + komi;
        if (black > white)
        {
            return Color.BLACK;
        }
        else
        {
            return Color.WHITE;
        }
    }

    public int validMoves(final short[] buffer, final Game copy)
    {
        int idx = 0;
        buffer[idx++] = Move.pass(playerToPlay());
        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                final short coord = Coord.XY(x, y);
                final short move = Move.move(coord, playerToPlay());
                if (isOKMove(move))
                {
                    if (board.adjacentsWithColor(coord, Color.WHITE) == 0 && board.adjacentsWithColor(coord, Color.BLACK) == 0)
                    {
                        buffer[idx++] = move;
                    }
                    else
                    {
                        copyTo(copy);
                        if (copy.play(move))
                        {
                            buffer[idx++] = move;
                        }
                    }
                    if (buffer.length == idx)
                    {
                        return idx;
                    }
                }
            }
        }
        buffer[idx] = Move.INVALID;
        return idx;
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

    public short moveN(final int moveNo)
    {
        if (moveNo >= lastMove)
        {
            return Move.INVALID;
        }
        else
        {
            return moves[moveNo];
        }
    }

}

