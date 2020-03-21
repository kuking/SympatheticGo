package uk.kukino.sgo;

import uk.kukino.sgo.gtp.Engine;
import uk.kukino.sgo.gtp.Streamed;
import uk.kukino.sgo.engines.MC1Engine;
import uk.kukino.sgo.engines.MC2Engine;
import uk.kukino.sgo.engines.RandomEngine;

import java.io.FileOutputStream;
import java.io.IOException;


public class Main
{

    public static void main(final String[] args) throws IOException
    {
        if (args.length == 0 || "help".equals(args[0]))
        {
            System.out.println("Main for SympatheticGO.");
            System.out.println("try: ./gradlew run --args 'perf'");
            System.out.println("..or: java -jar build/libs/sgo.jar gtp:random");
            System.out.println("..or: java -jar build/libs/sgo.jar gtp:mc1");
            System.out.println("..or: java -jar build/libs/sgo.jar gtp:mc2");

        }
        else if ("perf".equals(args[0]))
        {
            PerfStats.main(args);
        }
        else if (args[0].startsWith("gtp:"))
        {
            final Engine engine;
            if ("gtp:random".equals(args[0]))
            {
                engine = new RandomEngine();
            }
            else if ("gtp:mc1".equals(args[0]))
            {
                engine = new MC1Engine();
            }
            else if ("gtp:mc2".equals(args[0]))
            {
                engine = new MC2Engine();
            }
            else
            {
                engine = null;
                System.err.println("I don't know how to build the engine: " + args[0]);
                System.exit(1);
            }
            final FileOutputStream monitor = new FileOutputStream("/tmp/" + engine.name() + "-" + engine.version() + ".log");
            final Streamed stream = new Streamed(engine, System.in, System.out, monitor);
            while (!stream.isClosed())
            {
                stream.doWork(true);
            }
        }
        else

        {
            System.err.println("I don't know how to do: " + args[0]);
        }
    }
}
