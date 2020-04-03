package uk.kukino.sgo.images;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.FileUtils;
import uk.kukino.sgo.base.Color;
import uk.kukino.sgo.base.Game;
import uk.kukino.sgo.base.Move;
import uk.kukino.sgo.sgf.Header;
import uk.kukino.sgo.sgf.Node;
import uk.kukino.sgo.sgf.Result;
import uk.kukino.sgo.util.PacksUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Maker
{

    static final String TAR_FILENAME = "misc/sgfs/gnugo-lvl3-100k-self-play.tar.xz";
    static final File TARGET_FOLDER = new File("data/datasets/gnugo-self-play-lvl3-9x9/");

    private Stopwatch stopwatch;

    private void cleanUpTarget() throws IOException
    {
        FileUtils.deleteDirectory(TARGET_FOLDER);
    }

    private void createSGFImages() throws IOException
    {
        final Renderer renderer = new Renderer();
        final String filename[] = new String[] {null};
        final Game game[] = new Game[] {null};
        final Result result[] = new Result[] {Result.fromCs("UNDEF")};
        final int fileCount[] = new int[] {0};

        final Consumer<String> newFileC = fn ->
        {
            filename[0] = fn;
            if (fileCount[0] % 10000 == 0)
            {
                System.out.print(stopwatch + " - " + fileCount[0] + " " + fn + " ... \r");
                System.out.flush();
            }
            fileCount[0]++;
        };
        final Consumer<Header> headerC = header ->
        {
            game[0] = new Game(header.size, header.handicap, (byte) (header.komi * 10f));
            result[0] = header.result;
        };
        final Consumer<Node> nodeC = node ->
        {
            final Game g = game[0];
            if (Move.isValid(node.move))
            {
                g.play(node.move);
            }
            if (!g.finished() && g.lastMove() % 20 != 0)
            {
                return;
            }
            if (result[0] == null || !result[0].hasWinner() ||
                result[0].outcome() == Result.Outcome.Timeout ||
                result[0].outcome() == Result.Outcome.Forfeit)
            {
                return;
            }

            try
            {
                final BufferedImage image = renderer.render(g, result[0], 224);

                final String baseName = filename[0].substring(filename[0].lastIndexOf('/'), filename[0].length() - 4);
                final String folderWinner = result[0].winner() == Color.WHITE ? "white" : "black";
                final String folderGameState;
                if (g.finished())
                {
                    folderGameState = "finished";
                }
                else
                {
                    folderGameState = "move-" + game[0].lastMove();
                }
                final String folderRate;
                if (result[0].outcome() == Result.Outcome.Resign)
                {
                    folderRate = "resign";
                }
                else if (result[0].outcome() == Result.Outcome.Standard)
                {
                    if (result[0].points() < 5)
                    {
                        folderRate = "close";
                    }
                    else if (result[0].points() < game[0].getBoard().size())
                    {
                        folderRate = "fair";
                    }
                    else
                    {
                        folderRate = "easy";
                    }
                }
                else
                {
                    folderRate = "unknown";
                }

                final File target = new File(TARGET_FOLDER,
                    folderGameState + '/' + folderRate + '/' + folderWinner + '/' + baseName + ".png");
                target.getParentFile().mkdirs();

                ImageIO.write(image, "PNG", target);
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        };

        PacksUtils.forEachInTarXz(TAR_FILENAME, newFileC, headerC, nodeC);
    }

    public static void main(final String[] args) throws IOException
    {
        final Maker maker = new Maker();
        maker.stopwatch = Stopwatch.createStarted();
        maker.cleanUpTarget();
        maker.createSGFImages();
        // $ tar cvjf gnugo-self-play-lvl3-9x9.txz gnugo-self-play-lvl3-9x9/
        System.out.println(maker.stopwatch);
    }
}
