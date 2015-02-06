package net.nmmst.tools;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 * @param <T>
 * @param <U>
 */
public class Pair<T, U> {
    private final T first;
    private final U second;
    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }
    public T getFirst() {
        return first;
    }
    public U getSecond() {
        return second;
    }
    public Pair<T, U> copyOf() {
        return new Pair(first, second);
    }
}