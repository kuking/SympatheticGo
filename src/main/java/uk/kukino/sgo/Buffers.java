package uk.kukino.sgo;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

public class Buffers<T> {

    private Queue<T> availables;

    public Buffers(final int quantity, Supplier<T> generator) {
        availables = new ArrayDeque<>(quantity * 2);
        for (int i = 0; i < quantity; i++) {
            availables.add(generator.get());
        }
    }

    public T lease() {
        return availables.poll();
    }

    public void ret(final T t) {
        availables.add(t);
    }

}
