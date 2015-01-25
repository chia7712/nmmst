package net.nmmst.controller;

import net.java.games.input.Component;
import net.java.games.input.Controller;
public interface ControlTrigger 
{
    public void triggerOff(Component component);
    public Controller.Type getType();
}
