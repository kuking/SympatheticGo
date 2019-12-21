package uk.kukino.sgo;

import net.openhft.chronicle.bytes.Bytes;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardTest
{

    Board board;

    Bytes<ByteBuffer> adjs = Bytes.elasticByteBuffer(2 * 10);

    @Test
    public void simple()
    {
        board = new Board((byte) 19);
        board.set((byte) 4, (byte) 8, Color.BLACK);
        assertThat(board.get((byte) 4, (byte) 8), equalTo(Color.BLACK));
    }

    @Test
    public void full()
    {
        board = new Board((byte) 19);

        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                board.set(x, y, Color.WHITE);
                assertThat(board.get(x, y), equalTo(Color.WHITE));
            }
        }

        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                assertThat(board.get(x, y), equalTo(Color.WHITE));
            }
        }
    }

    @Test
    public void random()
    {
        board = new Board((byte) 120);
        Random random = new Random();
        List<Color> colors = new ArrayList<>();
        for (byte x = 0; x < 19; x++)
        {
            for (byte y = 0; y < 19; y++)
            {
                Color color = Color.values()[random.nextInt(3)];
                colors.add(color);
                board.set(x, y, color);
            }
        }
        for (byte x = 0; x < 19; x++)
        {
            for (byte y = 0; y < 19; y++)
            {
                Color color = colors.remove(0);
                assertThat(board.get(x, y), equalTo(color));
            }
        }
    }

    @Test
    public void copyTo()
    {
        board = new Board((byte) 19);
        Board boardCopy = new Board((byte) 19);

        board.set((byte) 4, (byte) 8, Color.BLACK);
        assertThat(board.get((byte) 4, (byte) 8), equalTo(Color.BLACK));
        assertThat(boardCopy.get((byte) 4, (byte) 8), equalTo(Color.EMPTY));

        board.copyTo(boardCopy);
        assertThat(boardCopy.get((byte) 4, (byte) 8), equalTo(Color.BLACK));
    }

    @Test
    public void adjacentsWithColor()
    {
        board = new Board((byte) 19);
        board.set(Move.parseToVal("W C17"));
        board.set(Move.parseToVal("B B18"));

        long adjs = board.adjacentsWithColor(Coord.parseToVal("B17"), Color.BLACK);
        AdjacentTest.assertPositions(adjs, "B18");

        adjs = board.adjacentsWithColor(Coord.parseToVal("B17"), Color.WHITE);
        AdjacentTest.assertPositions(adjs, "C17");

        adjs = board.adjacentsWithColor(Coord.parseToVal("B17"), Color.EMPTY);
        AdjacentTest.assertPositions(adjs, "B16", "A17");

        adjs = board.adjacentsWithColor(Coord.parseToVal("K10"), Color.EMPTY);
        AdjacentTest.assertPositions(adjs, "K11", "L10", "K9", "J10");
    }

    @Test
    public void clear()
    {
        board = new Board((byte) 19);

        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                board.set(x, y, Color.WHITE);
            }
        }

        board.clear();

        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                assertThat(board.get(x, y), equalTo(Color.EMPTY));
            }
        }
    }

    @Test
    public void hashing()
    {
        board = new Board((byte) 19);
        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                board.set(x, y, Color.EMPTY);
            }
        }

        int hashOrig = board.hashCode();
        for (byte x = 0; x < board.size(); x++)
        {
            for (byte y = 0; y < board.size(); y++)
            {
                board.set(x, y, Color.WHITE);
                assertThat(board.hashCode(), not(equalTo(hashOrig)));
                int hashForWhite = board.hashCode();

                board.set(x, y, Color.BLACK);
                assertThat(board.hashCode(), not(equalTo(hashOrig)));
                assertThat(board.hashCode(), not(equalTo(hashForWhite)));

                board.set(x, y, Color.EMPTY);
                assertThat(board.hashCode(), equalTo(hashOrig));
            }
        }


    }

    @Test
    public void onlyOneAtATime()
    {
        for (byte size = 9; size <= 25; size++)
        {
            board = new Board(size);
            for (byte c = 1; c < 4; c++)
            {
                for (byte x = 0; x < board.size(); x++)
                {
                    for (byte y = 0; y < board.size(); y++)
                    {
                        board.set(x, y, Color.fromByte(c));


                        for (byte xx = 0; xx < board.size(); xx++)
                        {
                            for (byte yy = 0; yy < board.size(); yy++)
                            {
                                if (xx == x && yy == y)
                                {
                                    assertThat(board.get(xx, yy), equalTo(Color.fromByte(c)));
                                }
                                else
                                {
                                    assertThat(board.get(xx, yy), equalTo(Color.EMPTY));
                                }
                            }
                        }
                        assertThat(board.count(Color.fromByte(c)), equalTo(1));
                        assertThat(board.count(Color.EMPTY), equalTo((size * size) - 1));
                        board.set(x, y, Color.EMPTY);
                    }
                }
            }
        }
    }

    @Test
    public void testToString()
    {
        final Board board = new Board((byte) 19);
        board.set(Move.parseToVal("B C12"));
        assertThat(board.toString(), equalTo(
            "hash: 8519682\n" +
                "   A B C D E F G H J K L M N O P Q R S T \n" +
                "19 . . . . . . . . . . . . . . . . . . . 19\n" +
                "18 . . . . . . . . . . . . . . . . . . . 18\n" +
                "17 . . . . . . . . . . . . . . . . . . . 17\n" +
                "16 . . . . . . . . . . . . . . . . . . . 16\n" +
                "15 . . . . . . . . . . . . . . . . . . . 15\n" +
                "14 . . . . . . . . . . . . . . . . . . . 14\n" +
                "13 . . . . . . . . . . . . . . . . . . . 13\n" +
                "12 . . X . . . . . . . . . . . . . . . . 12\n" +
                "11 . . . . . . . . . . . . . . . . . . . 11\n" +
                "10 . . . . . . . . . . . . . . . . . . . 10\n" +
                " 9 . . . . . . . . . . . . . . . . . . . 9\n" +
                " 8 . . . . . . . . . . . . . . . . . . . 8\n" +
                " 7 . . . . . . . . . . . . . . . . . . . 7\n" +
                " 6 . . . . . . . . . . . . . . . . . . . 6\n" +
                " 5 . . . . . . . . . . . . . . . . . . . 5\n" +
                " 4 . . . . . . . . . . . . . . . . . . . 4\n" +
                " 3 . . . . . . . . . . . . . . . . . . . 3\n" +
                " 2 . . . . . . . . . . . . . . . . . . . 2\n" +
                " 1 . . . . . . . . . . . . . . . . . . . 1\n" +
                "   A B C D E F G H J K L M N O P Q R S T \n"));
    }

}
