package uk.kukino.sgo.gtp;

import uk.kukino.sgo.base.Color;

public interface Engine
{
    CharSequence name();

    CharSequence version();

    boolean setBoardSize(byte size);

    void clearBoard();

    void setKomi(float komi);

    boolean play(short move);

    short genMove(Color color);

    boolean undo();

    CharSequence displayableBoard();

    boolean setTimeSettings(int mainTimeSecs, int byoYomiSecs, int byoYomiStones);

    /***
     *
     * @param stones black handicap
     * @return null if board is not empty or handicap can't be set, or the list of stones placed in the board.
     */
    short[] fixedHandicap(int stones);

    int calculateFinalScore();

    void shutdown();
}
