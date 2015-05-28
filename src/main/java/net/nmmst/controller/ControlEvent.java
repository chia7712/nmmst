package net.nmmst.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.nmmst.NConstants;
import net.nmmst.NProperties;
import net.nmmst.threads.Taskable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
* Captures all control events and use specified trigger to handle with
* the control events.
*/
public abstract class ControlEvent implements Taskable {
    public static ControlEvent createControlEvent(
            final NProperties properties,
            final List<ControlTrigger> triggerList) {
        if (properties.getBoolean(NConstants.CONTROLLER_ENABLE)) {
            return new BaseControlEvent(triggerList);
        }
        return new ControlEvent() {
            @Override
            public void work() {
                try {
                    TimeUnit.SECONDS.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ex) {
                }
            }

            @Override
            public void close() {
            }
        };
    }
    /**
    * The base implementation for control event.
    */
   private static class BaseControlEvent extends ControlEvent {
       /**
        * Log.
        */
       private static final Logger LOG
               = LoggerFactory.getLogger(BaseControlEvent.class);
       /**
        * Collects the controllers for polling data.
        */
       private final List<Controller> controllers = new LinkedList();
       /**
        * Collects the triggers for handlering with data.
        */
       private final List<ControlTrigger> triggers = new LinkedList();
       /**
        * Constructs a control event for monitoring I/O.
        * @param triggerList A list of triggers
        */
       public BaseControlEvent(final List<ControlTrigger> triggerList) {
           triggers.addAll(triggerList);
           for (Controller controller
               : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
               for (ControlTrigger trigger : triggers) {
                   if (controller.getType() == trigger.getType()) {
                       LOG.info("use device : "
                               + controller
                               + ", trigger : "
                               + trigger.getType());
                       controllers.add(controller);
                       break;
                   }
               }
           }
       }
       @Override
       public void work() {
           controllers.stream().map((controller) -> {
               controller.poll();
               return controller;
           }).forEach((controller) -> {
               EventQueue queue = controller.getEventQueue();
               Event event = new Event();
               while (queue.getNextEvent(event)) {  
                   triggers.stream()
                       .filter((trigger)
                           -> (controller.getType() == trigger.getType()))
                       .forEach((trigger) -> {
                           trigger.triggerOff(event.getComponent());
                   });
               }
           });
       }
       @Override
       public void clear() {
           controllers.clear();
           triggers.clear();
       }
   }
}
