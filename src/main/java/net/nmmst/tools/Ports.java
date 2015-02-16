package net.nmmst.tools;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public enum Ports {
    REQUEST_MASTER(10001),
    REQUEST_OTHERS(10002),
    REGISTER(10003),
    TEST(10004);
    private final int port;
    Ports(int port) {
        this.port = port;
    }
    public int get() {
        return port;
    }
}
