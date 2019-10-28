package uk.kukino.sgo;

public class Game {

    private final Board board;
    private final byte handicap;
    private final byte komi;

    private Color playerToPlay;
    private int lastMove;

    private static short[] HANDICAP_19 = new short[]{
            Move.parseToVal("B D4"), Move.parseToVal("B Q16"), Move.parseToVal("B D16"),
            Move.parseToVal("B Q4"), Move.parseToVal("B D10"), Move.parseToVal("B Q10"),
            Move.parseToVal("B K4"), Move.parseToVal("B K16"), Move.parseToVal("B K10")
    };


    Game(final byte size, final byte handicap, final byte komiX10) {
        board = new Board(size);
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

    boolean isValidMove(final short value) {
        if (board.get(Move.x(value), Move.y(value)) != Color.EMPTY) return false;
        return true;
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
