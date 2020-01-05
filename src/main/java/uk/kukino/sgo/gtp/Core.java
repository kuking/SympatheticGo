package uk.kukino.sgo.gtp;

import org.jetbrains.annotations.NotNull;
import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.base.Score;
import uk.kukino.sgo.util.Parsing;

import static uk.kukino.sgo.gtp.Command.*;

public class Core
{
    private final Engine engine;
    private final StringBuilder out;
    private CharSequence input;
    private int cmdIdxStart, cmdIdxEnd;

    private int id;
    private boolean shutdown;

    public Core(final Engine engine)
    {
        this.engine = engine;
        this.out = new StringBuilder(4096);
        this.shutdown = false;
    }

    public boolean isShutdown()
    {
        return shutdown;
    }

    public void shutdown()
    {
        if (!isShutdown())
        {
            engine.shutdown();
            shutdown = true;
        }
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
        if (input == null || shutdown)
        {
            return out;
        }

        cmdIdxStart = skipSpaces(0);
        cmdIdxEnd = skipNonSpaces(cmdIdxStart);
        this.id = Parsing.parseInteger(input, cmdIdxStart, cmdIdxEnd);
        if (this.id != Integer.MIN_VALUE)
        {
            cmdIdxStart = skipSpaces(cmdIdxEnd);
            cmdIdxEnd = skipNonSpaces(cmdIdxStart);
        }
        if (cmdIdxStart == input.length() || input.charAt(cmdIdxStart) == '#')
        {
            return out;
        }

        final boolean done = doPlay() || doGenMove() || doUndo() ||

            doName() || doProtocolVersion() || doVersion() ||
            doBoardSize() || doClearBoard() || doKomi() || doTimeSettings() ||
            doListCommands() || doKnownCommand() || doShowBoard() || doFixedHandicapOrPlaceFreeHandicap() || doFinalScore() ||
            doQuit();

        if (!done)
        {
            doUnknownCommand();
        }

        return out;
    }

    private boolean doPlay()
    {
        if (PLAY.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            final int start = skipSpaces(cmdIdxEnd);
            final int end = skipNonSpaces(skipSpaces(skipNonSpaces(start))); // So two words
            final short move = Move.parseToVal(input.subSequence(start, end));
            if (Move.isValid(move))
            {
                if (engine.play(move))
                {
                    success();
                }
                else
                {
                    failure().append("illegal move");
                }
            }
            else
            {
                failure().append("invalid color or coordinate");
            }
            return true;
        }
        return false;
    }

    private boolean doGenMove()
    {
        if (GENMOVE.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            final int start = skipSpaces(cmdIdxEnd);
            final int end = skipNonSpaces(start);
            final Color color = Color.parse(input.subSequence(start, end));

            if (color == null)
            {
                failure().append("invalid color");
            }
            else
            {
                final short move = engine.genMove(color);
                success();
                Move.write(out, move, false, false);
            }
            return true;
        }
        return false;
    }

    private boolean doUndo()
    {
        if (UNDO.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            if (engine.undo())
            {
                success();
            }
            else
            {
                failure().append("cannot undo");
            }
            return true;
        }
        return false;
    }

    private boolean doBoardSize()
    {
        if (BOARDSIZE.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            final int start = skipSpaces(cmdIdxEnd);
            final int end = skipNonSpaces(start);

            final int size = Parsing.parseInteger(input, start, end);
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

    private boolean doClearBoard()
    {
        if (CLEAR_BOARD.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            engine.clearBoard();
            success();
            return true;
        }
        return false;
    }

    private boolean doKomi()
    {
        if (KOMI.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            final int start = skipSpaces(cmdIdxEnd);
            final int end = skipNonSpaces(start);
            final float komi = Parsing.parseFloat(input, start, end);
            if (Float.isNaN(komi))
            {
                failure().append("komi not a float");
            }
            else
            {
                engine.setKomi(komi);
                success();
            }
            return true;
        }
        return false;
    }

    private boolean doKnownCommand()
    {
        if (KNOWN_COMMAND.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            final int start = skipSpaces(cmdIdxEnd);
            final int end = skipNonSpaces(start);
            boolean found = false;
            for (final Command cmd : Command.values())
            {
                if (cmd.matches(input, start, end))
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

    private boolean doName()
    {
        if (NAME.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            success().append(engine.name());
            return true;
        }
        return false;
    }

    private boolean doProtocolVersion()
    {
        if (PROTOCOL_VERSION.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            success().append(2);
            return true;
        }
        return false;
    }

    private boolean doVersion()
    {
        if (VERSION.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            success().append(engine.version());
            return true;
        }
        return false;
    }

    private boolean doListCommands()
    {
        // maybe sorted ..
        if (LIST_COMMANDS.matches(input, cmdIdxStart, cmdIdxEnd))
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

    private boolean doQuit()
    {
        if (QUIT.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            success();
            shutdown = true;
            return true;
        }
        return false;
    }

    private boolean doShowBoard()
    {
        if (SHOWBOARD.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            success().append('\n').append(engine.displayableBoard());
            return true;
        }
        return false;
    }

    private boolean doTimeSettings()
    {
        if (TIME_SETTINGS.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            int start = skipSpaces(cmdIdxEnd);
            int end = skipNonSpaces(start);
            final int mainSecs = Parsing.parseInteger(input, start, end);
            start = skipSpaces(end);
            end = skipNonSpaces(start);
            final int byoYomiSecs = Parsing.parseInteger(input, start, end);
            start = skipSpaces(end);
            end = skipNonSpaces(start);
            final int byoYomiStones = Parsing.parseInteger(input, start, end);

            if (mainSecs == Integer.MIN_VALUE || byoYomiSecs == Integer.MIN_VALUE || byoYomiStones == Integer.MIN_VALUE)
            {
                failure().append("not three integers");
            }
            else if (mainSecs < 0 || byoYomiSecs < 0 || byoYomiStones < 0)
            {
                failure().append("invalid positive integer");
            }
            else if (engine.setTimeSettings(mainSecs, byoYomiSecs, byoYomiStones))
            {
                success();
            }
            else
            {
                failure().append("time not accepted");
            }
            return true;
        }
        return false;
    }

    private boolean doFixedHandicapOrPlaceFreeHandicap()
    {
        if (FIXED_HANDICAP.matches(input, cmdIdxStart, cmdIdxEnd) ||
            PLACE_FREE_HANDICAP.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            final int start = skipSpaces(cmdIdxEnd);
            final int end = skipNonSpaces(start);
            final int handicap = Parsing.parseInteger(input, start, end);
            if (handicap == Integer.MIN_VALUE)
            {
                failure().append("handicap not an integer");
            }
            else if (handicap < 2 || handicap > 9)
            {
                failure().append("invalid handicap");
            }
            else
            {
                final short[] coords = engine.fixedHandicap(handicap);
                if (coords == null)
                {
                    failure().append("board not empty");
                }
                else
                {
                    success();
                    for (final short coord : coords)
                    {
                        Coord.write(out, coord);
                        out.append(' ');
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean doFinalScore()
    {
        if (FINAL_SCORE.matches(input, cmdIdxStart, cmdIdxEnd))
        {
            final int score = engine.calculateFinalScore();
            if (Score.isUnknown(score))
            {
                failure().append("cannot score");
            }
            else if (Score.isDraw(score))
            {
                success().append(0);
            }
            else
            {
                success();
                Score.write(out, score);
            }
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

}
