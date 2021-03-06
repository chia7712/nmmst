package codes.chia7712.nmmst.threads;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Instantiate a closer with atomic method.
 */
public class AtomicCloser implements Closer {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(AtomicCloser.class);
  /**
   * Indicates closable state.
   */
  private final AtomicBoolean closed = new AtomicBoolean(false);
  /**
   * The closeable to close with {@link #close()}.
   */
  private final ExecutorService service = Executors.newCachedThreadPool();
  /**
   * Collects all tasks for invokeing the
   * {@link codes.chia7712.nmmst.threads.Taskable#close()} when closing this closer.
   */
  private final List<Taskable> tasks
          = Collections.synchronizedList(new LinkedList<>());

  @Override
  public final <T extends Taskable> T invokeNewThread(final T task) {
    return invokeNewThread(task, null);
  }

  @Override
  public final <T extends Taskable> T invokeNewThread(
          final T task, final Timer timer) {
    if (isClosed()) {
      throw new RuntimeException("AtomicCloser is closed");
    }
    tasks.add(task);
    service.execute(() -> {
      try {
        task.init();
        while (!isClosed() && !Thread.interrupted()) {
          task.work();
          if (timer != null) {
            timer.sleep();
          }
        }
      } catch (Exception e) {
        LOG.error(e);
      } finally {
        task.clear();
      }
    });
    return task;
  }

  @Override
  public final void close() {
    if (closed.compareAndSet(false, true)) {
      service.shutdownNow();
      tasks.stream().forEach(task -> {
        try {
          task.close();
        } catch (IOException e) {
          LOG.error(e);
        }
      });
    }
  }

  @Override
  public final boolean isClosed() {
    return closed.get();
  }
}
