package net.nmmst.master;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface DioInterface {
    public void write(int port, byte data);
    public void write(int portStart, int portCount, byte[] data);
    public byte[] read();
}
