package uk.kukino.sgo.sgf;

import uk.kukino.sgo.base.Move;

public class Node
{
    CharSequence comment; // C
    short move; // B | W

    //    String text;
//    String name;
//    float value;
//    Double good4White;
//    Double good4Black;
//    Double hotSpot;
//    Double unclear;
//    Double bad;
//    boolean doubtful;
//    boolean interesting;
//    Double tesuji;

    public void reset()
    {
        comment = null;
        move = Move.INVALID;
    }

    public Node clone()
    {
        final Node clone = new Node();
        clone.comment = comment;
        clone.move = move;
        return clone;
    }

    public boolean isEmpty()
    {
        return comment == null &&
            move == Move.INVALID;
    }
}
