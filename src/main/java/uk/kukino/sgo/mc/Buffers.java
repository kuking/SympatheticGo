package uk.kukino.sgo.mc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

public class Buffers<T>
{
    private BlockingQueue<T> availables;

    public Buffers(final int quantity, final Supplier<T> generator)
    {
        availables = new ArrayBlockingQueue<>(quantity);
        for (int i = 0; i < quantity; i++)
        {
            availables.add(generator.get());
        }
    }

    public T lease()
    {
        try
        {
            return availables.take();
        }
        catch (final InterruptedException e)
        {
            return null;
        }
    }

    public void ret(final T t)
    {
        availables.add(t);
    }

}
