package net.nmmst.threads;

import java.io.Closeable;

/**
 * It is used for make {@link BackedRunner} stop the thread.
 */
public interface Closer extends Closeable {
    /**
     * Invokes an new thread for executing the task.
     * @param <T> A subclass of {@link Taskable}
     * @param task The executed task
     * @return The task
     */
    public <T extends Taskable> T invokeNewThread(final T task);
    /**
     * Invokes an new thread for executing the task.
     * @param <T> A subclass of {@link Taskable}
     * @param task The executed task
     * @param timer Execution period
     * @return The task
     */
    public <T extends Taskable> T invokeNewThread(
            final T task, final Timer timer);
    @Override
    public void close();
    /**
     * @return true if we should stop thread
     */
    public boolean isClosed();
}
