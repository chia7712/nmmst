/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.nmmst.tools;

import java.io.Closeable;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface Closer extends Closeable {
    @Override
    public void close();
    public boolean isClosed();
}
