package uk.kukino.sgo.util;

import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Move;

import java.io.PrintStream;

public class TUtils
{
    public static short[] cutOnFirstInvalid(final short[] bigList)
    {
        int size = 0;
        while (bigList[size] != Coord.INVALID)
        {
            size++;
        }
        final short[] result = new short[size];
        System.arraycopy(bigList, 0, result, 0, size);
        return result;
    }

    public static void printMoves(final PrintStream stream, final short[] moves)
    {
        stream.print("[");
        for (int i = 0; i < moves.length; i++)
        {
            stream.print(Move.shortToString(moves[i]));
            if (i + 1 != moves.length)
            {
                stream.print(", ");
            }
        }
        stream.println("]");
    }
}
