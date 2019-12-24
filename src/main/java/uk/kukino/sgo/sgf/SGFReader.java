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
    private boolean headerConsumed = false;
    private Node node = new Node();
    private Reader buffer;

    private Consumer<Header> headerConsumer;
    private Consumer<Node> nodeConsumer;

    public void parse(final java.io.Reader buf, Consumer<Header> headerConsumer, Consumer<Node> nodeConsumer) throws IOException
    {
        header.reset();
        node.reset();
        buffer = buf;
        this.headerConsumer = headerConsumer;
        this.nodeConsumer = nodeConsumer;

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
                final var fval = readFloatValue();
                header.komiX10 = (byte) (fval * 10f);
                break;
            case Date:
                final var dateTimeCs = readCharSequenceValue();
                try
                {
                    header.dateTime = LocalDateTime.parse(dateTimeCs);
                }
                catch (final DateTimeParseException e)
                {
                    header.dateTime = LocalDateTime.of(LocalDate.parse(dateTimeCs), LocalTime.NOON); // here it should throw
                }
                break;
            case TimeLimit:
                header.timeLimitSecs = readIntegerValue();
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
            default:
                throwWithDetails("I don't know how to parse identity " + (char) (identity >> 8) + (char) (identity & 0xff));
        }
    }

    private CharSequence readCharSequenceValue() throws IOException
    {
        final var buffer = new StringBuilder(); // FIXME: maybe we want to reuse buffers here, and encode property
        while ((char) peek() != ']' && peek() != -1)
        {
            buffer.append((char) read());
        }
        read(); // the last ]
        return buffer.toString();
    }

    private short readMoveValue(final Color color) throws IOException
    {
        skipNonPrintable();
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
        return Move.move((byte) (Character.toUpperCase((char) a) - 65), (byte) (header.size - Character.toUpperCase((char) b) + 64), color);
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

    private float readFloatValue() throws IOException
    {
        skipNonPrintable();
        long value = 0;
        int decimalPlaces = 0;
        while ((peek() >= '0' && peek() <= '9') || peek() == '.')
        {
            if (decimalPlaces > 0)
            {
                decimalPlaces *= 10;
            }
            if (peek() == '.')
            {
                read();
                decimalPlaces = 1;
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
        return (float) value / (float) decimalPlaces;
    }

    private int readIdentity() throws IOException
    {
        skipNonPrintable();
        final var point1 = read();
        if ((char)point1  == '(')
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

    private int readPosition = 0;
    private int peeked = Integer.MIN_VALUE;

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
