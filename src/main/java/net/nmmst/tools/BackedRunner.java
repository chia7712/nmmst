package net.nmmst.tools;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public abstract class BackedRunner implements Runnable {
    private final Closer closer;
    private final Timer timer;
    public BackedRunner(Closer closer) {
        this(closer, null);
    }
    public BackedRunner(Closer closer, Timer timer) {
        this.closer = closer;
        this.timer = timer;
    }
    @Override
    public final void run() {
        try {
            init();
            while(!closer.isClosed() && !Thread.interrupted()) {
                work();
                if (timer != null) {
                    timer.sleep();
                }
            }
        } catch(InterruptedException e) {
        } finally {
            clear();
        }
        
    }
    public final boolean isClosed() {
        return closer.isClosed();
    }
    protected abstract void work();
    protected abstract void init();
    protected abstract void clear();
}
