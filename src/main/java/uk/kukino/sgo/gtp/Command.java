package uk.kukino.sgo.gtp;

public enum Command
{
    PROTOCOL_VERSION, NAME, VERSION, LIST_COMMANDS, KNOWN_COMMAND, QUIT, BOARDSIZE, CLEAR_BOARD, KOMI, PLAY, GENMOVE,

    FIXED_HANDICAP, PLACE_FREE_HANDICAP, SET_FREE_HANDICAP;


    public boolean matches(final CharSequence cs, final int s, final int e)
    {
        if (e - s != name().length())
        {
            return false;
        }

        for (int i = 0; i < e - s; i++)
        {
            if (i > name().length())
            {
                return false;
            }
            if (cs.charAt(i + s) != Character.toLowerCase(name().charAt(i)))
            {
                return false;
            }
        }
        return true;
    }
}
