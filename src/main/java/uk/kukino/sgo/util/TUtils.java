package uk.kukino.sgo.util;

import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Move;

import java.util.function.Consumer;

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

    //TODO: convert this into a logger lambda

    public static void logMoves(final Consumer<String> logger, final short[] moves)
    {
        for (final short move : moves)
        {
            if (!Move.isValid(move))
            {
                logger.accept("INV");
            }
            else
            {
                logger.accept(Move.color(move).name().substring(0, 1));
                logger.accept("-");

                if (Move.isPass(move))
                {
                    logger.accept("PASS");
                }
                else
                {
                    logger.accept(Coord.shortToString(move));
                }
            }
            logger.accept(" ");
        }
    }

}
