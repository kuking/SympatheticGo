package uk.kukino.sgo.valuators;

import com.google.common.base.Stopwatch;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import uk.kukino.sgo.base.Coord;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.sgf.Header;
import uk.kukino.sgo.sgf.Node;
import uk.kukino.sgo.sgf.SGFReader;

import java.io.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class Makers
{

    private Stopwatch stopwatch;

    public void forEachInTarXz(final String tarXz,
                               final Consumer<String> newFile,
                               final Consumer<Header> headerC, final Consumer<Node> nodeC) throws IOException
    {
        final SGFReader sgfReader = new SGFReader();
        final InputStream fileIs = new FileInputStream(tarXz);
        final XZCompressorInputStream xzIs = new XZCompressorInputStream(fileIs);
        final TarArchiveInputStream tarIs = new TarArchiveInputStream(xzIs);
        TarArchiveEntry entry;
        while ((entry = tarIs.getNextTarEntry()) != null)
        {
            if (entry.isFile())
            {
                newFile.accept(entry.getName());
                final Reader r = new InputStreamReader(tarIs);
                sgfReader.parse(r, headerC, nodeC);
            }
        }
        tarIs.close();
        xzIs.close();
        fileIs.close();
    }

    private int[][] ingest9x9GnuGoLvl3SelfPlay() throws IOException
    {
        final boolean skip[] = new boolean[] {false};
        final int moveNo[] = new int[] {0};
        final short mirrorRotations[] = new short[9];
        final int samples[][] = new int[9 * 9 * 9][9 * 9 + 1];
        // stats
        final int highestMoveNo[] = new int[] {0};
        final int totalCount[] = new int[] {0};
        final int fileCount[] = new int[] {0};

        final Consumer<String> newFileC = fn ->
        {
            if (fileCount[0] % 10000 == 0)
            {
                System.out.print(stopwatch + " - " + fileCount[0] + " " + fn + " ... \r");
                System.out.flush();
            }
            fileCount[0]++;
        };
        final Consumer<Header> headerC = header ->
        {
            skip[0] = header.size != 9;
            if (skip[0])
            {
                System.out.println();
                System.out.println("Skipping as it is not 9x9.");
            }
            moveNo[0] = 0;

        };
        final Consumer<Node> nodeC = node ->
        {
            if (skip[0])
            {
                return;
            }
            if (Move.isPass(node.move))
            {
                samples[moveNo[0]][9 * 9]++;

            }
            else
            {
                final int rrSize = Coord.allRotationsAndReflections(mirrorRotations, node.move, (byte) 9);
                for (int rr = 0; rr < rrSize; rr++)
                {
                    final int ofs = Coord.linealOffset(mirrorRotations[rr], (byte) 9);
                    samples[moveNo[0]][ofs]++;
                }
            }

            moveNo[0]++;
            totalCount[0]++;
            if (highestMoveNo[0] < moveNo[0])
            {
                highestMoveNo[0] = moveNo[0];
            }
        };

        final String tarFilename = "misc/sgfs/gnugo-lvl3-100k-self-play.tar.xz";
        System.out.println(stopwatch + " - Ingesting " + tarFilename + " ... ");
        forEachInTarXz(tarFilename, newFileC, headerC, nodeC);
        System.out.print("                                                   \r");
        System.out.println(stopwatch + " - " + fileCount[0] + " game files ingested.");
        System.out.println(stopwatch + " - " + highestMoveNo[0] + " moves in the longest game.");
        System.out.println(stopwatch + " - " + totalCount[0] + " moves in total.");

        return Arrays.copyOf(samples, highestMoveNo[0]);
    }

    private Heatmaps buildHeatmapsFromMovesSamples(final int[][] samples)
    {
        System.out.println(stopwatch + " - Building Heatmaps from moves' samples ... ");
        final Heatmaps heatmaps = new Heatmaps();
        for (int moveNo = 0; moveNo < samples.length; moveNo++)
        {
            final int total = Arrays.stream(samples[moveNo]).sum();
            final float[] sample = new float[samples[moveNo].length];
            for (int o = 0; o < sample.length; o++)
            {
                sample[o] = (float) samples[moveNo][o] / (float) total;
            }

            heatmaps.put(moveNo, new Heatmap((byte) 9, sample));
        }
        return heatmaps;
    }

    private void normalizeHeatmaps(final Heatmaps heatmaps)
    {
        System.out.println(stopwatch + " - " + "Normalizing heatmaps ...");
        heatmaps.values().forEach(Heatmap::normalize);
    }

    private void store9x9CoordByMoveHeatmaps(final Heatmaps heatmaps) throws IOException
    {
        final String outputFile = "data/9x9-coord-by-move-heatmap.xz";
        System.out.println(stopwatch + " - Writting " + outputFile + " ...");
        heatmaps.writeToFile(outputFile);
    }

    public void gen9x9MoveProbabilityMatrix() throws IOException
    {
        System.out.println("Generating 9x9 probability matrix");
        System.out.println("---------------------------------");
        final int[][] samples = ingest9x9GnuGoLvl3SelfPlay();
        final Heatmaps heatmaps = buildHeatmapsFromMovesSamples(samples);
        normalizeHeatmaps(heatmaps);
        store9x9CoordByMoveHeatmaps(heatmaps);

//        heatmaps.keys().forEach(moveNo ->
//        {
//            System.out.println(moveNo);
//            System.out.println(heatmaps.get((Long) moveNo));
//        });
    }


    public static void main(final String[] args) throws IOException
    {
        final Makers makers = new Makers();
        makers.stopwatch = Stopwatch.createStarted();
        makers.gen9x9MoveProbabilityMatrix();
        System.out.println(makers.stopwatch);
    }

}
