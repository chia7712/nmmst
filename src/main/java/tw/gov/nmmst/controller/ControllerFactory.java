package tw.gov.nmmst.controller;

import java.util.LinkedList;
import java.util.List;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.NProperties;
import tw.gov.nmmst.threads.Closer;
import tw.gov.nmmst.threads.Taskable;

/**
 * Captures all control events and use specified trigger to handle with the
 * control events.
 */
public final class ControllerFactory {

  /**
   *
   */
  private static final Log LOG = LogFactory.getLog(ControllerFactory.class);

  /**
   * Handles with the controller data.
   */
  public interface Trigger {

    /**
     * @param component The controller component
     */
    void triggerOff(Component component);

    /**
     * @return The type of control trigger
     */
    Controller.Type getType();
  }

  /**
   * Invokes all triggers with a inner listener which provides the data of I/O
   * device. The inner listener is a thread invoked by closer.
   *
   * @param properties NProperties
   * @param closer Invokes a inner listener thread
   * @param triggerList The triggers to add
   */
  public static void invokeTriggers(final NProperties properties,
          final Closer closer,
          final List<ControllerFactory.Trigger> triggerList) {
    if (properties.getBoolean(NConstants.CONTROLLER_ENABLE)) {
      closer.invokeNewThread(new BaseControlEvent(triggerList));
    }
  }

  /**
   * The base implementation for control event.
   */
  private static class BaseControlEvent implements Taskable {

    /**
     * Collects the controllers for polling data.
     */
    private final List<Controller> controllers = new LinkedList<>();
    /**
     * Collects the triggers for handlering with data.
     */
    private final List<ControllerFactory.Trigger> triggers
            = new LinkedList<>();

    /**
     * Constructs a control event for monitoring I/O.
     *
     * @param triggerList A list of triggers
     */
    BaseControlEvent(final List<ControllerFactory.Trigger> triggerList) {
      triggers.addAll(triggerList);
      for (Controller controller : ControllerEnvironment
              .getDefaultEnvironment().getControllers()) {
        LOG.info("controller:" + controller);
        for (ControllerFactory.Trigger trigger : triggers) {
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
      LOG.info("Total triggers:" + triggers.size());
      LOG.info("Total controller:" + controllers.size());
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

  /**
   * Can't be instantiated with this ctor.
   */
  private ControllerFactory() {
  }
}
