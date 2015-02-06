package net.nmmst.controller;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public enum KeyDescriptor  {
    DASHBOARD("a"),
    MOVIE("b"),
    STOP("e");
    private final String key;
    KeyDescriptor(String key) {
        this.key = key.toLowerCase();
    }
    public boolean isValid(String str) {
        return key.compareTo(str.toLowerCase()) == 0;
    }
}
