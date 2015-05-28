package net.nmmst.views;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import net.nmmst.utils.RequestUtil.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Inherits the methods of {@link java.awt.event.WindowListener}.
 * The purpose of this class is to focus subclass to implement
 * {@link java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)}
 */
public class BaseFrame
    extends JFrame implements WindowListener {
    private static final Logger LOG
            = LoggerFactory.getLogger(BaseFrame.class);
    private final FrameData data;
    public BaseFrame(final FrameData frameData) {
        data = frameData;
        add(data.getMainPanel());
        data.getCloser().invokeNewThread(() -> {
            try {
                Request request = data.getQueue().take();
                LOG.info("request : " + request.getType());
                RequestFunction f = data.getFunctions().get(request.getType());
                if (f != null) {
                    f.work(data, request);
                }
                LOG.info("request : " + request.getType() + " done");
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        });
    }
    protected final FrameData getData() {
        return data;
    }
    @Override
    public final void windowClosed(WindowEvent e) {
        data.getCloser().close();
    }
    @Override
    public void windowOpened(WindowEvent e) {
    }
    @Override
    public void windowClosing(WindowEvent e) {
    }
    @Override
    public void windowIconified(WindowEvent e) {
    }
    @Override
    public void windowDeiconified(WindowEvent e) {
    }
    @Override
    public void windowActivated(WindowEvent e) {
    }
    @Override
    public void windowDeactivated(WindowEvent e) {
    }
}
