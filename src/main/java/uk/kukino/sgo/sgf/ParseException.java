package uk.kukino.sgo.sgf;

import java.io.IOException;

public class ParseException extends IOException
{
    public ParseException(final String message)
    {
        super(message);
    }
}
