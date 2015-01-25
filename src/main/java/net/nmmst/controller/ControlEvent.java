package net.nmmst.controller;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.ControllerEnvironment;
import net.nmmst.tools.Closure;


public class ControlEvent implements Closure
{
    private final List<Controller> controllers = new LinkedList();
    private final List<ControlTrigger> triggerList = new LinkedList();
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    public ControlEvent(List<ControlTrigger> triggerList) throws IOException
    {
        this.triggerList.addAll(triggerList);
        for(Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers())
        {
            
            for(ControlTrigger trigger : this.triggerList)
            {
                if(controller.getType() == trigger.getType())
                {
                    System.out.println("use device : " + controller);
                    controllers.add(controller);
                    break;
                }
            }
        }
    }
//    public ControlEvent(ControlTrigger ... triggers) throws IOException
//    {
//        this.triggers.addAll(Arrays.asList(triggers));
//        for(Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers())
//        {
//            for(ControlTrigger trigger : triggers)
//            {
//                if(controller.getType() == trigger.getType())
//                {
//                    controllers.add(controller);
//                    break;
//                }
//            }
//        }
//    }
    @Override
    public void close()
    {
        close.set(true);
    }
    @Override
    public void run() 
    {
        while(!close.get())
        {
            for(Controller controller : controllers)
            {
                controller.poll();
                EventQueue queue = controller.getEventQueue();
                Event event = new Event();
                while(queue.getNextEvent(event)) 
                {  
                    for(ControlTrigger trigger : triggerList)
                    {
                        if(controller.getType() == trigger.getType())
                            trigger.triggerOff(event.getComponent());
                    }
                }
            }
        }
    }
    @Override
    public boolean isClosed() 
    {
        return isClosed.get();
    }
}
