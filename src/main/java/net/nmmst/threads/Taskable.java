package net.nmmst.threads;

import java.io.Closeable;
import java.io.IOException;


public interface Taskable extends Closeable {
    /**
     * Periodically work.
     */
    public void work();
    /**
     * Initialize this thread.
     */
    public default void init() {
    }
    /**
     * Clears this object in the end phase.
     */
    public default void clear() {
    }
    @Override
    public default void close() {
    }
}
