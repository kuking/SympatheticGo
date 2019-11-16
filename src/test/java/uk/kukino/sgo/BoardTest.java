package uk.kukino.sgo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class BoardTest
{

    Board board;

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

        short[] arr = new short[4];
        byte res = board.adjacentsWithColor(arr, Coord.parseToVal("B17"), Color.BLACK);
        assertThat(res, equalTo((byte) 1));
        assertThat(arr[0], equalTo(Coord.parseToVal("B18")));

        res = board.adjacentsWithColor(arr, Coord.parseToVal("B17"), Color.WHITE);
        assertThat(res, equalTo((byte) 1));
        assertThat(arr[0], equalTo(Coord.parseToVal("C17")));

        res = board.adjacentsWithColor(arr, Coord.parseToVal("B17"), Color.EMPTY);
        assertThat(res, equalTo((byte) 2));
        assertThat(arr[0], equalTo(Coord.parseToVal("B16")));
        assertThat(arr[1], equalTo(Coord.parseToVal("A17")));

        res = board.adjacentsWithColor(arr, Coord.parseToVal("K10"), Color.EMPTY);
        assertThat(res, equalTo((byte) 4));
        assertThat(arr[0], equalTo(Coord.parseToVal("K9")));
        assertThat(arr[1], equalTo(Coord.parseToVal("L10")));
        assertThat(arr[2], equalTo(Coord.parseToVal("K11")));
        assertThat(arr[3], equalTo(Coord.parseToVal("J10")));
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

}
