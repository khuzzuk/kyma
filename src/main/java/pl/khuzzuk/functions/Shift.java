package pl.khuzzuk.functions;

import java.util.ArrayDeque;
import java.util.Queue;

public class Shift<T> {
    private Queue<T> switchQueue;

    private Shift() {
    }

    public static <T> Shift<T> get(T first, T second) {
        Shift<T> shift = new Shift<>();
        shift.switchQueue = new ArrayDeque<>(2);
        shift.switchQueue.add(first);
        shift.switchQueue.add(second);
        return shift;
    }

    public T get() {
        T element = switchQueue.poll();
        switchQueue.add(element);
        return element;
    }
}
