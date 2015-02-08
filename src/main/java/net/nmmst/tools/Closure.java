package net.nmmst.tools;

import java.io.Closeable;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface Closure extends Runnable, Closeable {
    @Override
    public void close();
    public boolean isClosed();
}