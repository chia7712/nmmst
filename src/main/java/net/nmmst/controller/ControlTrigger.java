package net.nmmst.controller;

import net.java.games.input.Component;
import net.java.games.input.Controller;
/**
 * Handles with the controller data.
 */
public interface ControlTrigger {
    /**
     * @param component The controller component
     */
    public void triggerOff(Component component);
    /**
     * @return The type of control trigger
     */
    public Controller.Type getType();
}
