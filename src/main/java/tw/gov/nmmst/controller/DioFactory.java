package tw.gov.nmmst.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.gov.nmmst.NProperties;

/**
 * Instaniates the dio controller which encapsulates the detail of
 * communication with dio device.
 */
public final class DioFactory {
    /**
     * Log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DioFactory.class);
    /**
     * Retrieves the dio controller.
     * @param properties NMMST properties
     * @return A real controller if the dio device is working fine.
     * The empty instance of dio interface will be returned, and
     * the empty instance is used for testing function of master node
     * because it may does not exist the pci device in the test enviroment.
     */
    public static DioInterface getDefault(final NProperties properties) {
        DioInterface rval;
        try {
            rval = new DioBdaq(properties);
            LOG.info("real dio device");
        } catch (Exception e) {
            LOG.info(e.getMessage());
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
