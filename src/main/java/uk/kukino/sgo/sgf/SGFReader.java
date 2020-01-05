package uk.kukino.sgo.sgf;

import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Move;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class SGFReader
{
    private Header header = new Header();
    private boolean headerConsumed;
    private Node node = new Node();
    private Reader buffer;

    private int readPosition;
    private int peeked;

    private Consumer<Header> headerConsumer;
    private Consumer<Node> nodeConsumer;

    public void parse(final Reader buf, final Consumer<Header> headerConsumer, final Consumer<Node> nodeConsumer) throws IOException
    {
        header.reset();
        node.reset();
        buffer = buf;
        headerConsumed = false;
        this.headerConsumer = headerConsumer;
        this.nodeConsumer = nodeConsumer;
        readPosition = 0;
        peeked = Integer.MIN_VALUE;

        mainCollection();
        onNewNode();
    }

    private void onNewNode()
    {
        if (!headerConsumed && !header.isEmpty())
        {
            headerConsumer.accept(header);
            headerConsumed = true;
        }
        if (!node.isEmpty())
        {
            nodeConsumer.accept(node);
            node.reset();
        }
    }

    private boolean mainCollection() throws IOException
    {
        skipNonPrintable();
        if ((char) read() != '(')
        {
            throwWithDetails("Couldn't find the beginning of the main Collection, expecting a '('.");
        }

        while ((char) peek() != ')')
        {
            processPropertiesAndNodes();
            skipNonPrintable();
        }
        if ((char) read() != ')')
        {
            throwWithDetails("Couldn't find the end of the main collection, expecting a ')'");
        }

        return true;
    }


    private void processPropertiesAndNodes() throws IOException
    {
        skipNonPrintable();
        if ((char) peek() == ';') // beginning of a new node?
        {
            read();
            onNewNode();
            skipNonPrintable();
            if ((char) peek() == ')')
            {
                return;
            }
        }
        handleProperty(readIdentity());
    }

    private void handleProperty(final int identity) throws IOException
    {
        skipNonPrintable();
        if (read() != '[')
        {
            throwWithDetails("Start of Property expected (a '[' character)");
        }
//        System.out.println("Case " + Identity.fromInt(identity) + " (" + identity + ")");
        switch (Identity.fromInt(identity))
        {
            case Black:
                node.move = readMoveValue(Color.BLACK);
                break;
            case White:
                node.move = readMoveValue(Color.WHITE);
                break;

            case FileFormat:
                header.fileFormat = (byte) readIntegerValue();
                break;
            case Game:
                header.gameType = GameType.byId(readIntegerValue());
                break;
            case Size:
                header.size = (byte) readIntegerValue();
                break;
            case GameName:
                header.name = readCharSequenceValue();
                break;
            case BlackName:
                header.blackName = readCharSequenceValue();
                break;
            case WhiteName:
                header.whiteName = readCharSequenceValue();
                break;
            case Handicap:
                header.handicap = (byte) readIntegerValue();
                break;
            case Komi:
                header.komi = readFloatValue();
                break;
            case Date:
                final var dateTimeCs = readCharSequenceValue();
                try
                {
                    header.dateTime = LocalDateTime.parse(dateTimeCs);
                }
                catch (final DateTimeParseException e)
                {
                    try
                    {
                        header.dateTime = LocalDateTime.of(LocalDate.parse(dateTimeCs), LocalTime.NOON); // here it should throw
                    }
                    catch (final DateTimeParseException e2)
                    {
                        header.dateTime = null; //throwWithDetails("Can't parse date: '" + dateTimeCs + "'");
                    }
                }
                break;
            case TimeLimit:
                header.timeLimitSecs = readTimeInSeconds();
                break;
            case Rules:
                header.rules = Rule.fromCs(readCharSequenceValue());
                break;
            case Charset:
                header.charset = Charset.forName(readCharSequenceValue().toString());
                break;
            case Application:
                header.application = readCharSequenceValue();
                break;
            case VariationFormat:
                header.variationsFormat = (byte) readIntegerValue();
                break;
            case Place:
                header.place = readCharSequenceValue();
                break;
            case Result:
                header.result = Result.fromCs(readCharSequenceValue());
                break;
            case Username:
            case Round:
            case Event:
            case OverTime:
            case Source:
            case Copyright:
            case PlayerToPlay:
            case Identifier:
            case GeneralComment:
            case BlackTeam:
            case WhiteTeam:
            case BlackTimeLeft:
            case WhiteTimeLeft:
            case BlackMovesLeft:
            case WhiteMovesLeft:
                // Username, Round, Event, etc...
                // (B/W)Team, (B/W)TimeLeft, (B/W)MovesLeft in Byo-yomi : ignored.
                readCharSequenceValue();
                break;
            case Circles:
            case Triangles:
            case Squares:
            case SelectedPoints:
            case Marks:
            case ViewOnly:
            case AddBlack:
            case AddWhite:
            case BlackTerritory:
            case WhiteTerritory:
                // list of points ignored for: Circle, ViewOnly, AddBlack, AddWhite
                // (last two are redundant due to Handicap.. at least in our user case)
                readCharSequenceValue();
                skipNonPrintable();
                while ((char) peek() == '[')
                {
                    readCharSequenceValue();
                    skipNonPrintable();
                }
                break;
            case Comment:
                if (headerConsumed)
                {
                    node.comment = readCharSequenceValue();
                }
                else
                {
                    header.comment = readCharSequenceValue();
                }
                break;
            case BlackRank:
                header.blackRank = Rank.fromCs(readCharSequenceValue());
                break;
            case WhiteRank:
                header.whiteRank = Rank.fromCs(readCharSequenceValue());
                break;
            case BlackSpecies:
                header.blackSpecies = Species.fromInt(readIntegerValue());
                break;
            case WhiteSpecies:
                header.whiteSpecies = Species.fromInt(readIntegerValue());
                break;

            default:
                throwWithDetails("I don't know how to parse identity " + (char) (identity >> 8) + (char) (identity & 0xff));
        }
    }

    private CharSequence readCharSequenceValue() throws IOException
    {
        final var buffer = new StringBuilder(); // FIXME: maybe we want to reuse buffers here, and encode property
        boolean nextIsEscaped = false;
        while ((nextIsEscaped || (char) peek() != ']') && peek() != -1)
        {
            final int point = read();
            if ((char) point == '\\')
            {
                nextIsEscaped = true;
            }
            else
            {
                nextIsEscaped = false;
                buffer.append((char) point);
            }

        }
        read(); // the last ]
        return buffer.toString();
    }

    private short readMoveValue(final Color color) throws IOException
    {
        skipNonPrintable();
        if ((char) peek() == ']')
        {
            read();
            return Move.pass(color);
        }
        final int a = read();
        final int b = read();
        if (a == -1 | b == -1)
        {
            return Move.INVALID;
        }
        skipNonPrintable();
        if (read() != ']')
        {
            throwWithDetails("Expected a closing property tag (']').");
        }
        if (header.size <= 19 && Character.toUpperCase((char) a) == 'T' && Character.toUpperCase((char) b) == 'T')
        {
            return Move.pass(color);
        }
        return Move.move((byte) (Character.toUpperCase((char) a) - 65), (byte) (header.size - (Character.toUpperCase((char) b) - 64)), color);
    }


    private int readIntegerValue() throws IOException
    {
        skipNonPrintable();
        int value = 0;
        while (peek() >= '0' && peek() <= '9')
        {
            value = value * 10 + read() - '0';
        }
        skipNonPrintable();
        if (read() != ']')
        {
            throwWithDetails("Property does not seems to be a number.");
        }
        return value;
    }

    private int readTimeInSeconds() throws IOException
    {
        skipNonPrintable();
        if ((char) peek() == ']')
        {
            read();
            return 0;
        }
        int value = 0;
        while (peek() >= '0' && peek() <= '9')
        {
            value = value * 10 + read() - '0';
        }
        if (peek() == 'h' || peek() == 'H')
        {
            read();
            value = value * 60 * 60;
        }
        if (peek() == 'm' || peek() == 'M')
        {
            read();
            value = value * 60;
        }
        skipNonPrintable();
        if (read() != ']')
        {
            throwWithDetails("Property does not seems to be a number.");
        }
        return value;

    }

    private float readFloatValue() throws IOException
    {
        skipNonPrintable();
        boolean negative = false;
        long value = 0;
        boolean decimalSeparatorFound = false;
        int decimalPlaces = 1;
        if ((char) peek() == '-')
        {
            read();
            negative = true;
        }
        while ((peek() >= '0' && peek() <= '9') || peek() == '.')
        {
            if (decimalSeparatorFound)
            {
                decimalPlaces *= 10;
            }
            if (peek() == '.')
            {
                read();
                decimalSeparatorFound = true;
            }
            else
            {
                value = value * 10 + read() - '0';
            }
        }
        skipNonPrintable();
        if (read() != ']')
        {
            throwWithDetails("Property does not seems to be a floating point number.");
        }
        if (negative)
        {
            value = -value;
        }
        return (float) value / (float) decimalPlaces;
    }

    private int readIdentity() throws IOException
    {
        skipNonPrintable();
        final var point1 = read();
        if ((char) point1 == '(')
        {
            throwWithDetails("Variants not implemented in this SGF reader.");
        }
        skipNonPrintable();
        var point2 = peek();
        if ((char) point2 == '[')
        {
            return point1;
        }
        point2 = read();
        skipNonPrintable();
        return (point1 << 8) + (point2 & 0xff);
    }

    private void skipNonPrintable() throws IOException
    {
        while (true)
        {
            final var ch = (char) peek();
            if (ch != '\n' && ch != '\r' && ch != '\t' && ch != ' ')
            {
                return;
            }
            read();
        }
    }

    // reader/peeker api

    private int read() throws IOException
    {
        final int retVal;
        if (peeked != Integer.MIN_VALUE)
        {
            retVal = peeked;
            peeked = Integer.MIN_VALUE;
        }
        else
        {
            retVal = buffer.read();
            readPosition++;
        }
//        System.out.println("READ: " + (char) retVal);
        return retVal;
    }

    private int peek() throws IOException
    {
        if (peeked == Integer.MIN_VALUE)
        {
            peeked = buffer.read();
            readPosition++;
        }
        return peeked;
    }

    // misc

    private void throwWithDetails(final String msg) throws IOException
    {
        String parserState = "readPos=" + readPosition + " @ '";
        while (peek() != -1)
        {
            parserState += (char) read();
        }
        throw new ParseException(msg + "\n" + parserState + "'");
    }

}
