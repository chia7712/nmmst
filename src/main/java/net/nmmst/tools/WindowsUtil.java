package net.nmmst.tools;

import java.io.IOException;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class WindowsUtil {
    private WindowsUtil(){}
    public static void shutdown() throws IOException {
        Runtime.getRuntime().exec("shutdown -s -t 0");
    }
    public static void reboot() throws IOException {
        Runtime.getRuntime().exec("shutdown -r -t 0");
    }
}
