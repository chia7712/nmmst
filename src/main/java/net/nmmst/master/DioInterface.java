package net.nmmst.master;

import java.io.Closeable;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface DioInterface extends Closeable {
    public void grayUptoEnd() throws InterruptedException;
    public void lightWork()throws InterruptedException;
    public void light(int mode)throws InterruptedException;
    public void lightParty(int mode)throws InterruptedException;
    public void lightOff()throws InterruptedException; 
    public void initializeSubmarineAndGray() throws InterruptedException;
    public void submarineGotoEnd() throws InterruptedException;
    public void stoneGotoRight() throws InterruptedException;
    public void stoneGotoLeft() throws InterruptedException;
}
