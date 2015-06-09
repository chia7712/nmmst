package tw.gov.nmmst.threads;

/**
 * Controls the period for {@link BackedRunner}
 * to invoke {@link BackedRunner#work()}.
 */
public interface Timer {
    /**
     * Goes to sleep for a while.
     * @throws InterruptedException If someone breaks the sleep process
     */
    void sleep() throws InterruptedException;
}