package net.nmmst.controller;

import net.java.games.input.Component;
import net.java.games.input.Controller;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public interface ControlTrigger {
    public void triggerOff(Component component);
    public Controller.Type getType();
}
