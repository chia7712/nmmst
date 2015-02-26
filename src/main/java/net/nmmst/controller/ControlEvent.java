package net.nmmst.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.ControllerEnvironment;
import net.nmmst.tools.BackedRunner;
import net.nmmst.tools.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class ControlEvent extends BackedRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ControlEvent.class);  
    private final List<Controller> controllers = new LinkedList();
    private final List<ControlTrigger> triggerList = new LinkedList();
    public ControlEvent(Closer closer) {
        this(closer, new ArrayList());
    }
    public ControlEvent(Closer closer, List<ControlTrigger> triggerList) {
        super(closer);
        this.triggerList.addAll(triggerList);
        for (Controller controller : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            for (ControlTrigger trigger : this.triggerList) {
                if (controller.getType() == trigger.getType()) {
                    LOG.info("use device : " + controller + ", trigger : " + trigger.getType());
                    controllers.add(controller);
                    break;
                }
            }
        }
    }
    @Override
    protected void work() {
        controllers.stream().map((controller) -> {
            controller.poll();
            return controller;
        }).forEach((controller) -> {
            EventQueue queue = controller.getEventQueue();
            Event event = new Event();
            while (queue.getNextEvent(event)) {  
                triggerList.stream()
                        .filter((trigger) -> (controller.getType() == trigger.getType()))
                        .forEach((trigger) -> {
                    trigger.triggerOff(event.getComponent());
                });
            }
        });
    }
    @Override
    protected void init() {
    }
    @Override
    protected void clear() {
    }
}
