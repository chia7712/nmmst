package net.nmmst.controller;

import java.io.Closeable;
import java.io.IOException;

/**
 * Encapsulates the detail about dio command. 
 */
public interface DioInterface extends Closeable {
    public default void grayUptoEnd() throws InterruptedException {
    }
    public default void lightWork()throws InterruptedException {
    }
    public default void light(int mode)throws InterruptedException {
    }
    public default void lightParty1()throws InterruptedException {
    }
    public default void lightParty2()throws InterruptedException {
    }
    public default void lightOff()throws InterruptedException {
    }
    public default void initializeSubmarineAndGray()
            throws InterruptedException {
    }
    public default void submarineGotoEnd() throws InterruptedException {
    }
    public default void stoneGotoRight() throws InterruptedException {
    }
    public default void stoneGotoLeft() throws InterruptedException {
    }
    @Override
    public default void close() throws IOException {
    }
}
