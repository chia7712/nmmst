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

public class FusionTuner extends JFrame implements WindowListener {
    private static final int FONT_SIZE = 30;
    public static <T extends Component> T setFont(T component) {
        Font textFont = new Font(component.getFont().getName(),
        component.getFont().getStyle(), FONT_SIZE);
        component.setFont(textFont);
        return component;
    }
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    private static final BufferedImage INIT_IMAGE
        = Painter.getFillColor(DEFAULT_WIDTH, DEFAULT_HEIGHT, Color.WHITE);
    private static final LinearProcessor.Factor TENTATIVE_FACTOR
        = new LinearProcessor.Factor(
        0.0335,
        0.118,
        0.6,
        0.9,
        0.6,
        0.9
    );
    private static final BufferedImage NO_NODE_IMAGE =
        Painter.getStringImage("No node", DEFAULT_WIDTH, DEFAULT_HEIGHT, 30);
    private final BlockingQueue<Pair<NodeInformation, Factor>> queue
        = new LinkedBlockingQueue();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    public FusionTuner(NProperties properties) {
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
            }
        });
    }
    @Override
    public void windowOpened(WindowEvent e) {
    }
    @Override
    public void windowClosing(WindowEvent e) {
    }
    @Override
    public void windowClosed(WindowEvent e) {
        service.shutdownNow();
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
    private static class NodePanel extends JPanel {
        private static final List<String> DESCRIPTIONS = Arrays.asList(
          "Overlay X",
          "Overlay Y",
          "Min X",
          "Max X",
          "Min Y",
          "Max Y"
        );
        private static final int OVERLAY_X_INDEX = 0;
        private static final int OVERLAY_Y_INDEX = 1;
        private static final int MIX_X = 2;
        private static final int MAX_X = 3;
        private static final int MIX_Y = 4;
        private static final int MAX_Y = 5;
        private final List<Pair<JLabel, JTextField>> components
            = new LinkedList();
        private final JTextField addressText
            = FusionTuner.setFont(new JTextField());
        private final NodeInformation.Location location;
        private final BlockingQueue<Pair<NodeInformation, Factor>> queue;
        public NodePanel(BlockingQueue<Pair<NodeInformation, Factor>>
                requestQueue, NodeInformation nodeInfo) {
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
        private static void initFactor(
                List<Pair<JLabel, JTextField>> components) {
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
                    MIX_X))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMinX())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    MAX_X))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMaxX())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    MIX_Y))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMinY())))));
            components.add(new Pair(
                FusionTuner.setFont(new JLabel(DESCRIPTIONS.get(
                    MAX_Y))),
                FusionTuner.setFont(new JTextField(String.valueOf(
                    TENTATIVE_FACTOR.getScaleMaxY())))));
        }
        
        private Factor getFactor() {
            List<Double> values
                = components.stream()
                            .map(entry ->
                                Double.valueOf(entry.getValue().getText()))
                            .collect(Collectors.toCollection(ArrayList::new));
            return new Factor(
                    values.get(OVERLAY_X_INDEX),
                    values.get(OVERLAY_Y_INDEX),
                    values.get(MIX_X),
                    values.get(MAX_X),
                    values.get(MIX_Y),
                    values.get(MAX_Y));
        }
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

    public static void main(String[] args) throws Exception {
        JFrame frame = new FusionTuner(new NProperties());
        SwingUtilities.invokeLater(() -> {
            frame.setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
