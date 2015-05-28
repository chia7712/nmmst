package net.nmmst.controller;

import java.io.IOException;
import net.nmmst.NProperties;

/**
 * Instaniates the dio controller which encapsulates the detail of
 * communication with dio device.
 */
public class DioFactory {
    /**
     * Retrieves the dio controller.
     * @param properties NMMST properties
     * @return A real controller if the dio device is working fine.
     * The empty instance of dio interface will be returned, and
     * the empty instance is used for testing function of master node
     * because it may does not exist the pci device in the test enviroment.
     */
    public static DioInterface getDefault(NProperties properties) {
        try {
            return new DioBdaq(properties);
        } catch (Exception e) {
            return new DioInterface() {
            };
        }
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private DioFactory(){
    }
}
