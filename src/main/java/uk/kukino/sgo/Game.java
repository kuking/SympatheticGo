package uk.kukino.sgo;

public class Game {

    // state
    private final Board board;
    private final byte handicap;
    private final byte komi;
    private Color playerToPlay;
    private int lastMove;

    // workplace
    private final Board chainMarks;
    private final Board libertiesMarks;
    private final Buffers<short[]> adjacentBuffers;

    private static short[] HANDICAP_19 = new short[]{
            Move.parseToVal("B D4"), Move.parseToVal("B Q16"), Move.parseToVal("B D16"),
            Move.parseToVal("B Q4"), Move.parseToVal("B D10"), Move.parseToVal("B Q10"),
            Move.parseToVal("B K4"), Move.parseToVal("B K16"), Move.parseToVal("B K10")
    };


    Game(final byte size, final byte handicap, final byte komiX10) {
        board = new Board(size);
        chainMarks = new Board(size);
        libertiesMarks = new Board(size);
        adjacentBuffers = new Buffers<>(size * 4, () -> new short[4]);

        komi = komiX10;
        this.handicap = handicap;
        initializeHandicap();
        lastMove = 0;
    }

    private void initializeHandicap() {
        if (handicap == (byte) 0) {
            this.playerToPlay = Color.BLACK;
        } else {
            if (board.size() != 19) {
                throw new IllegalArgumentException("I am sorry, I know only to deal handicap for 19x19 boards.");
            }
            for (int i = 0; i < handicap; i++) {
                board.set(HANDICAP_19[i]);
            }
            this.playerToPlay = Color.WHITE;
        }

    }

    public byte getHandicap() {
        return handicap;
    }

    public byte getKomiX10() {
        return komi;
    }

    public Color playerToPlay() {
        return playerToPlay;
    }

    private void markChainAndLiberties(final short coord) {
        board.copyTo(chainMarks);
        board.copyTo(libertiesMarks);
        Color color = board.get(coord);

        if (color == Color.EMPTY) {
            return;
        }
        recursivePaint(coord, color);
    }

    private void recursivePaint(final short coord, final Color color) {
        if (board.get(coord) != color) {
            return;
        }
        chainMarks.set(Move.move(coord, color));

        short[] adj = adjacentBuffers.lease();
        try {
            byte adjN = board.adjacentsWithColor(adj, coord, Color.EMPTY);
            for (byte i = 0; i < adjN; i++) {
                libertiesMarks.set(Move.move(adj[i], Color.MARK));
            }
            adjN = board.adjacentsWithColor(adj, coord, color);
            for (byte i = 0; i < adjN; i++) {
                recursivePaint(adj[i], color);
            }
        } finally {
            adjacentBuffers.ret(adj);
        }
    }

    boolean isValidMove(final short move) {
        if (Move.color(move) != playerToPlay) return false;
        if (Move.x(move) >= board.size() || Move.x(move) == 0) return false;
        if (Move.y(move) >= board.size() || Move.y(move) == 0) return false;
        if (board.get(Move.x(move), Move.y(move)) != Color.EMPTY) return false;

        short[] adjs = adjacentBuffers.lease();
        try {
            byte adjsN = board.adjacentsWithColor(adjs, move, Color.EMPTY);
            if (adjsN > 0) {
                return true;
            }

            adjsN = board.adjacentsWithColor(adjs, move, playerToPlay);
            if (adjsN == 0) {
                recursivePaint(move, playerToPlay.opposite());




                // more complicated, to be implemented
                return false;
            }


            return false;
        } finally {
            adjacentBuffers.ret(adjs);
        }
    }

    boolean play(final short value) {
        if (!isValidMove(value)) {
            return false;
        }

        board.set(value);

        playerToPlay = playerToPlay.opposite();
        lastMove++;

        return true;
    }

    boolean play(final CharSequence move) {
        return play(Move.parseToVal(move));
    }

    int deadStones(final Color color) {
        return 0;
    }

    boolean finished() {
        return false;
    }

    Board getBoard() {
        return board;
    }

    int lastMove() {
        return lastMove;
    }


}
