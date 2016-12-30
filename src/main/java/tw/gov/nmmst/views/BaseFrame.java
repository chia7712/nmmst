package tw.gov.nmmst.views;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.utils.RequestUtil.Request;
import tw.gov.nmmst.threads.Taskable;

/**
 * Inherits the methods of {@link java.awt.event.WindowListener}. The purpose of
 * this class is to focus subclass to implement null {@link java.awt.event.WindowListener#windowClosed(
 * java.awt.event.WindowEvent)}
 */
public final class BaseFrame
        extends JFrame implements WindowListener {

  /**
   * Log.
   */
  private static final Log LOG
          = LogFactory.getLog(BaseFrame.class);
  /**
   * Frame data.
   */
  private final FrameData data;

  /**
   * Constructs a base frame with specified frame data. A thread is invoked for
   * handleing with request by executeing the request function.
   *
   * @param frameData Frame data
   */
  public BaseFrame(final FrameData frameData) {
    data = frameData;
    add(data.getMainPanel());
    data.getCloser().invokeNewThread(new Taskable() {
      private Request previousReq;

      @Override
      public void work() {
        try {
          Request request = data.getQueue().take();
          LOG.info("request : " + request.getType());
          RequestFunction f
                  = data.getFunctions().get(request.getType());
          if (f != null) {
            f.work(data, previousReq, request);
            previousReq = request;
          }
          LOG.info("request : " + request.getType() + " done");
        } catch (Exception e) {
          LOG.error(e);
        } finally {
          data.getQueue().clear();
        }
      }
    });
  }

  @Override
  public void windowClosed(final WindowEvent e) {
    data.getCloser().close();
  }

  @Override
  public void windowOpened(final WindowEvent e) {
  }

  @Override
  public void windowClosing(final WindowEvent e) {
  }

  @Override
  public void windowIconified(final WindowEvent e) {
  }

  @Override
  public void windowDeiconified(final WindowEvent e) {
  }

  @Override
  public void windowActivated(final WindowEvent e) {
  }

  @Override
  public void windowDeactivated(final WindowEvent e) {
  }
}
