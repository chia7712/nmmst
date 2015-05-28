package net.nmmst.views;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.nmmst.NConstants;
import net.nmmst.media.MediaWorker;
import net.nmmst.processor.ProcessorFactory;
import net.nmmst.media.BasePanel;
import net.nmmst.processor.LinearProcessor;
import net.nmmst.utils.RegisterUtil;
import net.nmmst.utils.RequestUtil;
/**
 * The frame for fusion player.
 */
public class FusionFrame {
    public static void main(String[] args) throws IOException {
        FusionFrameData frameData = new FusionFrameData();
        final int width = frameData.getNProperties().getInteger(
                NConstants.FRAME_WIDTH);
        final int height = frameData.getNProperties().getInteger(
                NConstants.FRAME_HEIGHT);
        final JFrame f = new BaseFrame(frameData);
        f.setCursor(f.getToolkit().createCustomCursor(
                new ImageIcon("").getImage(),new Point(16, 16),""));
        SwingUtilities.invokeLater(() -> {
            if (width <= 0 || height <= 0) {
                f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                f.setSize(new Dimension(width, height));
            }
            f.requestFocusInWindow();
            f.setUndecorated(true);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
    public static class FusionFrameData extends BaseFrameData {
        private final MediaWorker media;
        public FusionFrameData() throws IOException {
            media = MediaWorker.createMediaWorker(
                getNProperties(), getCloser(),
                ProcessorFactory.createFrameProcessor(
                        getNodeInformation().getLocation()));
            RegisterUtil.invokeReporter(getCloser(),
                    getNodeInformation(), media.getMovieBuffer());
            getFunctions().put(RequestUtil.RequestType.FUSION_TEST,
                (FrameData data, RequestUtil.Request request)
                -> {
                    if (request instanceof RequestUtil.FusionTestRequest) {
                        RequestUtil.FusionTestRequest fusionReq
                                = (RequestUtil.FusionTestRequest)request;
                        BufferedImage image = fusionReq.getImage();
                        LinearProcessor processor
                            = new LinearProcessor(
                                data.getNodeInformation().getLocation(),
                                fusionReq.getFactor());
                        processor.process(image);
                        data.getMainPanel().write(image);
                    }
                });
        }
        @Override
        public BasePanel getMainPanel() {
            return media.getPanel();
        }
        @Override
        public MediaWorker getMediaWorker() {
            return media;
        }
    }
}
