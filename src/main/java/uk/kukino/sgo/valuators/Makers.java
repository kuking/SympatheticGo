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
                System.out.print(fileCount[0] + " " + fn + " ... \r");
                System.out.flush();
            }
            fileCount[0]++;
        };
        final Consumer<Header> headerC = header ->
        {
            skip[0] = header.size != 9;
            if (skip[0])
            {
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
        System.out.println("Ingesting " + tarFilename + " ...");
        forEachInTarXz(tarFilename, newFileC, headerC, nodeC);
        System.out.print("                                                   \r");
        System.out.println(fileCount[0] + " game files ingested.");
        System.out.println(highestMoveNo[0] + " moves in the longest game.");
        System.out.println(totalCount[0] + " moves in total.");

        return Arrays.copyOf(samples, highestMoveNo[0]);
    }

    private float[][] frequenciesForSamples(final int[][] samples)
    {
        System.out.println("Generating frequencies ...");
        final float[][] res = new float[samples.length][samples[0].length];
        for (int b = 0; b < samples.length; b++)
        {
            final int total = Arrays.stream(samples[b]).sum();
            for (int o = 0; o < samples[0].length; o++)
            {
                res[b][o] = (float) samples[b][o] / (float) total;
            }
        }
        return res;
    }

    private float[][] normalizeFrequencies(final float[][] freqs)
    {
        System.out.println("Normalizing frequencies within range [0..1] ...");
        final float[][] res = new float[freqs.length][freqs[0].length];
        for (int b = 0; b < freqs.length; b++)
        {
            final Heatmap heatmap = new Heatmap((byte) 9, freqs[b]);
            heatmap.normalize();
            //System.out.println(b + "\n" + heatmap);
            res[b] = heatmap.getCopy();
        }
        return res;
    }

    private void store9x9CoordMoveFrequencies(final float[][] frequencies) throws IOException
    {
        final String outputFile = "data/9x9-coord-by-move-dist.xz";
        System.out.println("Writting " + outputFile + " ...");
        final FileOutputStream fout = new FileOutputStream(outputFile);
        final XZCompressorOutputStream xzout = new XZCompressorOutputStream(fout);
        final ObjectOutputStream oos = new ObjectOutputStream(xzout);

        oos.writeByte(9);
        oos.writeInt(frequencies.length);
        for (int b = 0; b < frequencies.length; b++)
        {
            for (int i = 0; i < frequencies[b].length; i++)
            {
                oos.writeFloat(frequencies[b][i]);
            }
        }
        oos.close();
    }

    public void gen9x9MoveProbabilityMatrix() throws IOException
    {
        System.out.println("Generating 9x9 probability matrix ...");
        final int[][] samples = ingest9x9GnuGoLvl3SelfPlay();
        final float[][] frequencies = frequenciesForSamples(samples);
        final float[][] normalized = normalizeFrequencies(frequencies);
        store9x9CoordMoveFrequencies(normalized);
    }


    public static void main(final String[] args) throws IOException
    {
        final Stopwatch sw = Stopwatch.createStarted();
        final Makers makers = new Makers();
        makers.gen9x9MoveProbabilityMatrix();
        System.out.println(sw);
    }

}
