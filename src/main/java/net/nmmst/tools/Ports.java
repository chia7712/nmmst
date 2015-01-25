package net.nmmst.tools;

public enum Ports 
{
    REQUEST(10001),
    REGISTER(10002),
    TEST(10003);
    private int port;
    Ports(int port)
    {
        this.port = port;
    }
    public int get()
    {
        return port;
    }
}
