package uk.kukino.sgo.sgf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Benchmark
{
    public static void main(String[] args) throws IOException
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
                    sgfReader.parse(r, header -> {
                        headers[0]++;
                    }, node -> {
                        nodes[0]++;
                    });
                }
                catch (final IOException e)
                {
                    System.out.println("Parsing ... " + p.getParent() + "/" + p.getFileName());
                    System.out.println(e.getMessage().substring(0, e.getMessage().indexOf('\n') + 20));
                }
            });

        System.out.println("Total: " + count[0] + " tried, headers: " + headers[0] + ", nodes: " + nodes[0]);
        // Total: 165373 tried, headers: 152301, nodes: 31699051
        // Total: 165373 tried, headers: 152478, nodes: 31736630 // species
        // Total: 165373 tried, headers: 159283, nodes: 33130924 // if can't parse date, let it be null
        // Total: 165373 tried, headers: 159784, nodes: 33233633
        // Total: 394222 tried, headers: 388633, nodes: 78106488 // after added all KGS
        // Total: 394222 tried, headers: 388882, nodes: 78158569
        // Total: 394222 tried, headers: 393363, nodes: 79123151 // after fixing TM[] & TM[1h]

    }
}
