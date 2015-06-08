package net.nmmst;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import javafx.util.Pair;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.nmmst.media.BasePanel;
import net.nmmst.processor.LinearProcessor;
import net.nmmst.processor.LinearProcessor.Factor;
import net.nmmst.utils.Painter;
import net.nmmst.utils.RequestUtil.FusionTestRequest;
import net.nmmst.utils.SerialStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Tunes the fusion factor.
 */
public class FusionTuner extends JFrame implements WindowListener {
    /**
     * Log.
     */
    private static final Logger LOG
        = LoggerFactory.getLogger(FusionTuner.class);
    /**
     * Default font size.
     */
    private static final int FONT_SIZE = 30;
    /**
     * Creates a component with default font size.
     * @param <T> The subclass of component
     * @param component The component to set
     * @return Component
     */
    public static <T extends Component> T setFont(final T component) {
        Font textFont = new Font(component.getFont().getName(),
        component.getFont().getStyle(), FONT_SIZE);
        component.setFont(textFont);
        return component;
    }
    /**
     * Default width of frame.
     */
    private static final int DEFAULT_WIDTH = 1024;
    /**
     * Default height of frame.
     */
    private static final int DEFAULT_HEIGHT = 768;
    /**
     * Default image for testing fusion factor.
     */
    private static final BufferedImage INIT_IMAGE
        = Painter.getFillColor(DEFAULT_WIDTH,
                              DEFAULT_HEIGHT,
                              Color.WHITE);
    /**
     * Default fusion factor.
     */
    private static final LinearProcessor.Factor TENTATIVE_FACTOR
        = new LinearProcessor.Factor(
        0.0335,
        0.118,
        0.6,
        0.9,
        0.6,
        0.9
    );
    /**
     * This image is set for absent node information.
     */
    private static final BufferedImage NO_NODE_IMAGE =
        Painter.getStringImage("No node",
                              DEFAULT_WIDTH,
                              DEFAULT_HEIGHT,
                              30);
    /**
     * Queues the {@link net.nmmst.utils.RequestUtil.FusionTestRequest}.
     * It is shared between node panels.
     */
    private final BlockingQueue<Pair<NodeInformation, Factor>> queue
        = new LinkedBlockingQueue();
    /**
     * A thread for sending
     * the {@link net.nmmst.utils.RequestUtil.FusionTestRequest}.
     */
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    /**
     * A fusion tuner for tesing the fusion format of video node.
     * @param properties NProperties
     */
    public FusionTuner(final NProperties properties) {
        List<NodeInformation> nodeList
            = new ArrayList(NodeInformation.getVideoNodes(properties));
        setLayout(new GridLayout(0, 2));
        final int maxNumber = 4;
        for (int index = 0; index != maxNumber; ++index) {
            if (index < nodeList.size()) {
                NodeInformation node = nodeList.get(index);
                add(new NodePanel(queue, node));
            } else {
                add(new BasePanel(NO_NODE_IMAGE));
            }
        }
        service.execute(() -> {
            try {
                while (!Thread.interrupted()) {
                    Pair<NodeInformation, Factor> pair
                        = queue.take();
                    SerialStream.send(
                        pair.getKey(),
                        new FusionTestRequest(INIT_IMAGE, pair.getValue()),
                        properties);
                }
            } catch (InterruptedException | IOException e) {
                LOG.error(e.getMessage());
            }
        });
    }
    @Override
    public void windowOpened(final WindowEvent e) {
    }
    @Override
    public void windowClosing(final WindowEvent e) {
    }
    @Override
    public final void windowClosed(final WindowEvent e) {
        service.shutdownNow();
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
    /**
     * A node panel provides many fields to write the
     * {@link NodeInformation} and {@link Factor}.
     */
    private static class NodePanel extends JPanel {
        /**
         * The name of labels.
         */
        private static final List<String> DESCRIPTIONS = Arrays.asList(
          "Overlay X",
          "Overlay Y",
          "Min X",
          "Max X",
          "Min Y",
          "Max Y"
        );
        /**
         * Description index of overlay x.
         */
        private static final int OVERLAY_X_INDEX = 0;
        /**
         * Description index of overlay y.
         */
        private static final int OVERLAY_Y_INDEX = 1;
        /**
         * Description index of min x.
         */
        private static final int MIX_X_INDEX = 2;
        /**
         * Description index of max x.
         */
        private static final int MAX_X_INDEX = 3;
        /**
         * Description index of min y.
         */
        private static final int MIX_Y_INDEX = 4;
        /**
         * Description index of max y.
         */
        private static final int MAX_Y_INDEX = 5;
        /**
         * The labels and text fields.
         */
        private final List<Pair<JLabel, JTextField>> components
            = new LinkedList();
        /**
         * A address field to set.
         */
        private final JTextField addressText
            = FusionTuner.setFont(new JTextField());
        /**
         * Node location.
         */
        private final NodeInformation.Location location;
        /**
         * Queues the {@link net.nmmst.utils.RequestUtil.FusionTestRequest}.
         */
        private final BlockingQueue<Pair<NodeInformation, Factor>> queue;
        /**
         * Constructs a node panel for shared queue and node information.
         * @param requestQueue Shared queue
         * @param nodeInfo Node inforamtion
         */
        NodePanel(final BlockingQueue<Pair<NodeInformation, Factor>>
                requestQueue, final NodeInformation nodeInfo) {
            queue = requestQueue;
            addressText.setText(
                    nodeInfo.getIP() + ":" + nodeInfo.getRequestPort());
            location = nodeInfo.getLocation();
            initFactor(components);
            setLayout(new GridLayout(0, 1));
            JButton button = FusionTuner.setFont(new JButton(location.name()));
            button.addActionListener(event -> {
                queue.offer(new Pair(getNodeInformation(), getFactor()));
            });
            add(button);
            add(addressText);
            components.forEach(entry -> {
                JPanel panel = new JPanel();
                panel.setLayout((new GridLayout(1, 2)));
                entry.getKey().setHorizontalAlignment(JLabel.CENTER);
                panel.add(entry.getKey());
                panel.add(entry.getValue());
                add(panel);
            });
        }
        /**
         * Initializes the factor fields.
         * @param components The components to initialize
         */
        private static void initFactor(
                final List<Pair<JLabel, JTextField>> components) {
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    OVERLAY_X_INDEX))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getOverlayX())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    OVERLAY_Y_INDEX))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getOverlayY())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    MIX_X_INDEX))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMinX())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    MAX_X_INDEX))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMaxX())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    MIX_Y_INDEX))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMinY())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    MAX_Y_INDEX))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMaxY())))));
        }
        /**
         * Retrieves a factor according to text fields.
         * @return A factor
         */
        private Factor getFactor() {
            List<Double> values
                = components.stream()
                            .map(entry ->
                                Double.valueOf(entry.getValue().getText()))
                            .collect(Collectors.toCollection(ArrayList::new));
            return new Factor(
                    values.get(OVERLAY_X_INDEX),
                    values.get(OVERLAY_Y_INDEX),
                    values.get(MIX_X_INDEX),
                    values.get(MAX_X_INDEX),
                    values.get(MIX_Y_INDEX),
                    values.get(MAX_Y_INDEX));
        }
        /**
         * Retrieves a node information according to text fields.
         * @return A node information
         */
        private NodeInformation getNodeInformation() {
            final int addressIndex = 0;
            final int portIndex = 1;
            final String address
                = addressText.getText().split(":")[addressIndex];
            final int port
                = Integer.valueOf(addressText.getText().split(":")[portIndex]);
            return new NodeInformation.Builder()
                                      .setLocation(location)
                                      .setAddress(address)
                                      .setRequestPort(port)
                                      .build();
        }
    }
    /**
     * Invokes a frame which provides the ability of testing fusion.
     * @param args No use
     * @throws Exception If any error
     */
    public static void main(final String[] args) throws Exception {
        JFrame frame = new FusionTuner(new NProperties());
        SwingUtilities.invokeLater(() -> {
            frame.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
