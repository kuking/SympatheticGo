package uk.kukino.sgo.sgf;

public enum Species
{
    Human, Computer;

    public static Species fromInt(final int val)
    {
        if (val == 0)
        {
            return Human;
        }
        if (val > 0)
        {
            return Computer;
        }
        else
        {
            throw new IllegalArgumentException("I don't know negative species.");
        }
    }

}
