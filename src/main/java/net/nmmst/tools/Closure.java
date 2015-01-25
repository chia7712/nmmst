package net.nmmst.tools;

import java.io.Closeable;

public interface Closure extends Runnable, Closeable
{
    @Override
    public void close();
    public boolean isClosed();
}
