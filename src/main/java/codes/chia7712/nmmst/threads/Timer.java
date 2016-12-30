package codes.chia7712.nmmst.threads;

/**
 * Controls the period for {@link codes.chia7712.nmmst.threads.Taskable} to invoke
 * {@link codes.chia7712.nmmst.threads.Taskable#work()}.
 */
public interface Timer {

  /**
   * Goes to sleep for a while.
   *
   * @throws InterruptedException If someone breaks the sleep process
   */
  void sleep() throws InterruptedException;
}
