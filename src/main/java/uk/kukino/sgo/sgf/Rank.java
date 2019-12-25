package uk.kukino.sgo.sgf;

public class Rank
{

    public static final Rank UNKNOWN = new Rank("UNKNOWN");

    private String text;

    private Rank(final String st)
    {
        this.text = st;
    }

    @Override
    public String toString()
    {
        return text;
    }

    public static Rank fromCs(final CharSequence cs)
    {
        return new Rank(cs.toString());
    }

    public Rank clone()
    {
        return fromCs(text);
    }
}
