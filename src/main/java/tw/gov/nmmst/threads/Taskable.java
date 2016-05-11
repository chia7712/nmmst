package tw.gov.nmmst.threads;

import java.io.Closeable;
import java.io.IOException;
/**
 * A thread interface for doing the loop work.
 */
public interface Taskable extends Closeable {
    /**
     * Periodically work.
     * @throws java.lang.Exception
     */
    void work() throws Exception;
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
    default void close() throws IOException {
    }
}
