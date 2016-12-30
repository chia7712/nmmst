package tw.gov.nmmst.utils;

import java.io.IOException;

/**
 * Invokes the windows command by windows shell.
 */
public final class WindowsUtil {

  /**
   * Turns off node.
   *
   * @throws IOException If failed to invoke shell
   */
  public static void shutdown() throws IOException {
    Runtime.getRuntime().exec("shutdown -s -t 0");
  }

  /**
   * Reboots the node.
   *
   * @throws IOException If failed to invoke shell
   */
  public static void reboot() throws IOException {
    Runtime.getRuntime().exec("shutdown -r -t 0");
  }

  /**
   * Can't be instantiated with this ctor.
   */
  private WindowsUtil() {
  }
}
