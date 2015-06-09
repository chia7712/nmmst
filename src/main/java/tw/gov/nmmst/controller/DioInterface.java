package tw.gov.nmmst.controller;

import java.io.Closeable;
import java.io.IOException;

/**
 * Encapsulates the detail about dio command.
 */
public interface DioInterface extends Closeable {
    /**
     * Rises the gray screen.
     * @throws InterruptedException If this method is breaked
     */
    default void grayUptoEnd() throws InterruptedException {
    }
    /**
     * Turns on the work light.
     * @throws InterruptedException If this method is breaked
     */
    default void lightWork()throws InterruptedException {
    }
    /**
     * Turns on the light for specified mode.
     * @param mode The light mode
     * @throws InterruptedException If this method is breaked
     */
    default void light(int mode)throws InterruptedException {
    }
    /**
     * Turns on the first party light.
     * @throws InterruptedException If this method is breaked
     */
    default void lightParty1()throws InterruptedException {
    }
    /**
     * Turns on the second party light.
     * @throws InterruptedException If this method is breaked
     */
    default void lightParty2()throws InterruptedException {
    }
    /**
     * Turns off the light.
     * @throws InterruptedException If this method is breaked
     */
    default void lightOff()throws InterruptedException {
    }
    /**
     * Falls the gray screen and moves the submarine to initial position.
     * @throws InterruptedException If this method is breaked
     */
    default void initializeSubmarineAndGray() throws InterruptedException {
    }
    /**
     * Moves the submarine to end position.
     * @throws InterruptedException If this method is breaked
     */
    default void submarineGotoEnd() throws InterruptedException {
    }
    /**
     * Moves the stone screen to right side.
     * @throws InterruptedException If this method is breaked
     */
    default void stoneGotoRight() throws InterruptedException {
    }
    /**
     * Moves the stone screen to left side.
     * @throws InterruptedException If this method is breaked
     */
    default void stoneGotoLeft() throws InterruptedException {
    }
    @Override
    default void close() throws IOException {
    }
}
