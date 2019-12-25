package uk.kukino.sgo.sgf;

import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Game;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Benchmark
{

    private static void readAll() throws IOException
    {
        final int[] count = new int[] {0};
        final int[] headers = new int[] {0};
        final int[] nodes = new int[] {0};
        final SGFReader sgfReader = new SGFReader();
        Files.walk(Paths.get("data/"))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().toUpperCase().endsWith(".SGF"))
            .forEach(p -> {
                count[0]++;
                try
                {
                    final Reader r = new InputStreamReader(new FileInputStream(p.toFile()));
                    sgfReader.parse(r, header -> headers[0]++, node -> nodes[0]++);
                }
                catch (final IOException e)
                {
                    System.out.println("Parsing ... " + p.getParent() + "/" + p.getFileName());
                    System.out.println(e.getMessage().substring(0, e.getMessage().indexOf('\n') + 20));
                }
            });

        System.out.println("Total: " + count[0] + " tried, headers: " + headers[0] + ", nodes: " + nodes[0]);
    }

    private static void replayKgsForRegression() throws IOException
    {
        final SGFReader sgfReader = new SGFReader();
        final Game[] game = new Game[1];
        Files.walk(Paths.get("data/kgs/"))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().toUpperCase().endsWith(".SGF"))
            .forEach(p -> {
                try
                {
                    final Reader r = new InputStreamReader(new FileInputStream(p.toFile()));
                    sgfReader.parse(r, header -> {
                        if (header.size == (byte) 9 || header.handicap == 0)
                        {
                            game[0] = new Game(header.size, header.handicap, (byte) (header.komi * 10));
                        }
                        else
                        {
                            game[0] = null;
                        }
                    }, node ->
                    {
                        if (game[0] != null)
                        {
                            if (!game[0].play(node.move))
                            {
                                System.out.println(p.getParent() + "/" + p.getFileName() + " ...");
                                System.out.println("UPS! Invalid move? " + Coord.shortToString(node.move));
                                System.out.println(game[0].toString());
                                game[0] = null;
                            }
                        }
                    });
                }
                catch (final IOException e)
                {
                    System.out.println("Parsing ... " + p.getParent() + "/" + p.getFileName());
                    System.out.println(e.getMessage().substring(0, e.getMessage().indexOf('\n') + 20));
                }


            });
    }

    public static void main(String[] args) throws IOException
    {
        //readAll();
        replayKgsForRegression();
    }
}
