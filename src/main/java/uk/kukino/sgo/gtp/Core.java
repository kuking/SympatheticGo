package uk.kukino.sgo.gtp;

import org.jetbrains.annotations.NotNull;

import static uk.kukino.sgo.gtp.Command.*;

public class Core
{
    private final Engine engine;
    private final StringBuilder out;
    private CharSequence input;

    private int id;
    private boolean closed;

    public Core(final Engine engine)
    {
        this.engine = engine;
        this.out = new StringBuilder(4096);
        this.closed = false;
    }

    public boolean isClosed()
    {
        return closed;
    }

    /***
     * Main method for executing GTP commands, this method is probably called via a Socket or StdIn/StdOut wrapper.
     * @param input an input line called should split this via LFs
     * @return the answer or empty string
     */
    public CharSequence input(final CharSequence input)
    {
        this.input = input;
        out.delete(0, out.length());
        if (input == null || closed)
        {
            return out;
        }

        int s = skipSpaces(0);
        int t = skipNonSpaces(s);
        this.id = parsePositiveNumber(s, t);
        if (this.id != Integer.MIN_VALUE)
        {
            s = skipSpaces(t);
            t = skipNonSpaces(s);
        }
        if (s == input.length() || input.charAt(s) == '#')
        {
            return out;
        }

        final boolean done = doName(s, t) || doProtocolVersion(s, t) || doVersion(s, t) ||
            doBoardSize(s, t) || doClearBoard(s, t) ||
            doListCommands(s, t) || doKnownCommand(s, t) || doQuit(s, t);

        if (!done)
        {
            doUnknownCommand();
        }


        return out;
    }

    private boolean doBoardSize(final int s, final int t)
    {
        if (BOARDSIZE.matches(input, s, t))
        {
            final int s2 = skipSpaces(t);
            final int t2 = skipNonSpaces(s2);

            final int size = parsePositiveNumber(s2, t2);
            if (size == Integer.MIN_VALUE)
            {
                failure().append("boardsize not an integer");
            }
            else if (engine.setBoardSize((byte) size))
            {
                success();
            }
            else
            {
                failure().append("unacceptable size");
            }
            return true;
        }
        return false;
    }

    private boolean doClearBoard(final int s, final int t)
    {
        if (CLEAR_BOARD.matches(input, s, t))
        {
            engine.clearBoard();
            success();
            return true;
        }
        return false;
    }

    private boolean doKnownCommand(int s, int t)
    {
        if (KNOWN_COMMAND.matches(input, s, t))
        {
            final int s2 = skipSpaces(t);
            final int t2 = skipNonSpaces(s2);
            boolean found = false;
            for (final Command cmd : Command.values())
            {
                if (cmd.matches(input, s2, t2))
                {
                    found = true;
                    break;
                }
            }
            success().append(found);
            return true;
        }
        return false;
    }

    private boolean doName(final int s, final int t)
    {
        if (NAME.matches(input, s, t))
        {
            success().append(engine.name());
            return true;
        }
        return false;
    }

    private boolean doProtocolVersion(final int s, final int t)
    {
        if (PROTOCOL_VERSION.matches(input, s, t))
        {
            success().append(2);
            return true;
        }
        return false;
    }

    private boolean doVersion(final int s, final int t)
    {
        if (VERSION.matches(input, s, t))
        {
            success().append(engine.version());
            return true;
        }
        return false;
    }

    private boolean doListCommands(final int s, final int t)
    {
        // maybe sorted ..
        if (LIST_COMMANDS.matches(input, s, t))
        {
            success();
            for (final Command cmd : Command.values())
            {
                out.append(cmd.name().toLowerCase()).append("\n");
            }
            out.deleteCharAt(out.length() - 1);
            return true;
        }
        return false;
    }

    private boolean doQuit(final int s, final int t)
    {
        if (QUIT.matches(input, s, t))
        {
            success();
            closed = true;
            return true;
        }
        return false;
    }


    private boolean doUnknownCommand()
    {
        failure().append("unknown command");
        return true;
    }


    // ---------------------------------------------------------------------------------------------------------------------------------

    @NotNull
    private StringBuilder success()
    {
        out.append("=");
        if (id != Integer.MIN_VALUE)
        {
            out.append(id);
        }
        return out.append(' ');
    }

    private StringBuilder failure()
    {
        out.append("?");
        if (id != Integer.MIN_VALUE)
        {
            out.append(id);
        }
        return out.append(' ');
    }

    private int skipNonSpaces(final int i)
    {
        int j = i;
        while (j < input.length() && input.charAt(j) != ' ' && input.charAt(j) >= 32)
        {
            j++;
        }
        return j;
    }

    private int skipSpaces(final int i)
    {
        int j = i;
        while (j < input.length() && (input.charAt(j) == ' ' || input.charAt(j) < 32))
        {
            j++;
        }
        return j;
    }

    private int parsePositiveNumber(final int s, final int t)
    {
        int j = s;
        int value = 0;
        boolean negative = false;

        if (j < t && input.charAt(j) == '-')
        {
            j++;
            negative = true;
        }
        while (j < t && input.charAt(j) >= '0' && input.charAt(j) <= '9')
        {
            value = value * 10 + (input.charAt(j) - '0');
            j++;
        }
        if (j != t || s == t)
        {
            return Integer.MIN_VALUE;
        }
        else if (negative)
        {
            return -value;
        }
        else
        {
            return value;
        }
    }

}
