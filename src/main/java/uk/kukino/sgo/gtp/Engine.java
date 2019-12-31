package uk.kukino.sgo.gtp;

public interface Engine
{
    CharSequence name();

    CharSequence version();

    boolean setBoardSize(byte size);

    void clearBoard();

}
