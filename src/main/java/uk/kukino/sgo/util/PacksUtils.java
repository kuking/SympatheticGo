package uk.kukino.sgo.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import uk.kukino.sgo.sgf.Header;
import uk.kukino.sgo.sgf.Node;
import uk.kukino.sgo.sgf.SGFReader;

import java.io.*;
import java.util.function.Consumer;

public class PacksUtils
{
    public static void forEachInTarXz(final String tarXz,
                                      final Consumer<String> newFile,
                                      final Consumer<Header> headerC,
                                      final Consumer<Node> nodeC) throws IOException
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
}
