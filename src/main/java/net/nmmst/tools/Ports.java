package net.nmmst.tools;

/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public enum Ports {
    REQUEST(10001),
    REGISTER(10002),
    TEST(10003);
    private final int port;
    Ports(int port) {
        this.port = port;
    }
    public int get() {
        return port;
    }
}
