package tw.gov.nmmst.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;

/**
 * Instaniates the dio controller which encapsulates the detail of communication
 * with dio device.
 */
public final class DioFactory {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(DioFactory.class);

  /**
   * Retrieves the dio controller.
   *
   * @param properties NMMST properties
   * @return A real controller if the dio device is working fine. The empty
   * instance of dio interface will be returned, and the empty instance is used
   * for testing function of master node because it may does not exist the pci
   * device in the test enviroment.
   */
  public static DioInterface getDefault(final NProperties properties) {
    if (!properties.getBoolean(NConstants.DIO_ENABLE)) {
      LOG.info("virtual dio device");
      return new DioInterface() {
      };
    }
    DioInterface rval;
    try {
      rval = new DioBdaq(properties);
      LOG.info("real dio device");
    } catch (Exception e) {
      LOG.info(e);
      rval = new DioInterface() {
      };
      LOG.info("virtual dio device");
    }
    return rval;
  }

  /**
   * Can't be instantiated with this ctor.
   */
  private DioFactory() {
  }
}
