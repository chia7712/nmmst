package tw.gov.nmmst.threads;

import java.io.Closeable;
/**
 * A thread interface for doing the loop work.
 */
public interface Taskable extends Closeable {
    /**
     * Periodically work.
     */
    void work();
    /**
     * Initialize this thread.
     */
    default void init() {
    }
    /**
     * Clears this object in the end phase.
     */
    default void clear() {
    }
    @Override
    default void close() {
    }
}
