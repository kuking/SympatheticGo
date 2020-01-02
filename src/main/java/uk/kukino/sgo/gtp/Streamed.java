package uk.kukino.sgo.gtp;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;

import java.io.*;
import java.nio.charset.Charset;

public class Streamed
{
    private static final Charset CHARSET = Charsets.UTF_8;

    private Core core;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private OutputStreamWriter monitor;

    public Streamed(final Engine engine, final InputStream in, final OutputStream out, final OutputStream monout)
    {
        core = new Core(engine);
        reader = new BufferedReader(new InputStreamReader(in, CHARSET));
        writer = new OutputStreamWriter(out, CHARSET);
        if (monout != null)
        {
            monitor = new OutputStreamWriter(monout, CHARSET);
        }
        else
        {
            monitor = null;
        }
    }

    public void doWork(final boolean block)
    {
        if (core.isShutdown())
        {
            close();
        }
        if (isClosed())
        {
            return;
        }

        try
        {
            if (!block && !reader.ready())
            {
                return;
            }
            final String line = reader.readLine();
            if (line == null)
            {
                close();
                return;
            }
            final CharSequence out = core.input(line);
            outputToWriter(writer, out, true);
            if (monitor != null)
            {
                outputToWriter(monitor, line, false);
                outputToWriter(monitor, out, true);
            }
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            close();
        }
    }

    private void outputToWriter(final Writer writer, final CharSequence out, boolean doubleLF) throws IOException
    {
        for (int i = 0; i < out.length(); i++)
        {
            writer.write(out.charAt(i));
        }
        if (out.length() > 0)
        {
            writer.write('\n');
            if (doubleLF)
            {
                writer.write('\n');
            }
        }
        writer.flush();
    }

    public void close()
    {
        Closeables.closeQuietly(reader);
        reader = null;
        try
        {
            writer.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        writer = null;

        if (monitor != null)
        {
            try
            {
                monitor.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
            monitor = null;
        }
    }

    public boolean isClosed()
    {
        return reader == null || writer == null;
    }


}
