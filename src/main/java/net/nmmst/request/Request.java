package net.nmmst.request;

import java.io.Serializable;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 5201297223045482865L;
    public enum Type {
        START,
        STOP,
        PAUSE,
        INIT,
        SELECT,
        TEST,
        PARTY1,
        PARTY2,
        LIGHT_OFF,
        REBOOT,
        SHUTDOWN,
        WOL
    }
    private final Type type;
    private final Object arg;
    public Request(Type type) {
        this.type = type;
        this.arg = null;
    }
    public Request(Type type, Serializable serial) {
        this.type = type;
        this.arg = serial;
    }
    public Type getType() {
        return type;
    }
    public Object getArgument() {
        return arg;
    }
}
