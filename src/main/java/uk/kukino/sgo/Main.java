package uk.kukino.sgo;

import uk.kukino.sgo.gtp.Streamed;
import uk.kukino.sgo.util.RandomEngine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class Main
{

    public static void main(final String[] args) throws FileNotFoundException
    {
        if (args.length == 0 || "help".equals(args[0]))
        {
            System.out.println("Main for SympatheticGO.");
            System.out.println("try: ./gradlew run --args 'perf'");
            System.out.println("..or: java -jar build/libs/sgo.jar gtp");

        }
        else if ("perf".equals(args[0]))
        {
            PerfStats.main(args);
        }
        else if ("gtp".equals(args[0]))
        {
            final FileOutputStream monitor = new FileOutputStream("/tmp/random-engine.log");
            final RandomEngine randomEngine = new RandomEngine();
            final Streamed stream = new Streamed(randomEngine, System.in, System.out, monitor);
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
