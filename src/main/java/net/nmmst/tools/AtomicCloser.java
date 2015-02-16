/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.tools;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class AtomicCloser implements Closer {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    @Override
    public void close() {
        closed.set(true);
    }
    @Override
    public boolean isClosed() {
        return closed.get();
    }
    public boolean ifNoClosed() {
        return closed.compareAndSet(false, true);
    }
}
