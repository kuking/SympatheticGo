package uk.kukino.sgo.engines;

import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;

public class RandomEngine extends BaseEngine
{

    public RandomEngine()
    {
        super("RandomEngine", "1.1b");
    }

    @Override
    public short genMove(final Color color)
    {
        final Game tmp = new Game(boardSize, handicap, komiX10);
        for (int i = 0; i < 1000; i++)
        {
            game.copyTo(tmp);
            final short candidate = Move.random(boardSize, tmp.playerToPlay());
            if (tmp.play(candidate))
            {
                game.play(candidate);
                return candidate;
            }
        }
        final short itIsPass = Move.pass(game.playerToPlay());
        game.play(itIsPass);
        return itIsPass;
    }
}
