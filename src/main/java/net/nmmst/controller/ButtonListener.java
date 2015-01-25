package net.nmmst.controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;

public class ButtonListener extends KeyAdapter
{
    private final BlockingQueue<KeyDescriptor> buttonQueue;
    public ButtonListener(BlockingQueue<KeyDescriptor> buttonQueue)
    {
        this.buttonQueue = buttonQueue;
    }
    @Override
    public void keyPressed(KeyEvent arg0) 
    {
        String key = String.valueOf(arg0.getKeyChar());
        for(KeyDescriptor event : KeyDescriptor.values())
        {
            if(event.isValid(key))
            {
                buttonQueue.offer(event);
                return;
            }
        }
    }
}