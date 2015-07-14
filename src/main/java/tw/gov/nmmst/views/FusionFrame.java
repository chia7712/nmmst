package tw.gov.nmmst.views;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.processor.ProcessorFactory;
import tw.gov.nmmst.media.BasePanel;
import tw.gov.nmmst.processor.LinearProcessor;
import tw.gov.nmmst.utils.RegisterUtil;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.RequestUtil.FusionTestRequest;
/**
 * The fusion node plays the movies with fusing the image edge.
 */
public final class FusionFrame {
    /**
     * Invokes a fusion frame.
     * @param args No use
     * @throws IOException If failed to open movie
     */
    public static void main(final String[] args) throws IOException {
        File file = null;
        if (args.length == 1) {
            file = new File(args[0]);
        }
        FusionFrameData frameData = new FusionFrameData(file);
        final int width = frameData.getNProperties().getInteger(
                NConstants.FRAME_WIDTH);
        final int height = frameData.getNProperties().getInteger(
                NConstants.FRAME_HEIGHT);
        final JFrame f = new BaseFrame(frameData);
        final Point point = new Point(16, 16);
        f.setCursor(f.getToolkit().createCustomCursor(
                new ImageIcon("").getImage(), point, ""));
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
    /**
     * Data of fusion nodes.
     */
    private static class FusionFrameData extends VideoData {
        /**
         * Media components.
         */
        private final MediaWorker media;
        /**
         * Constructs the data with specified media.
         * @param file The initial properties
         * @throws IOException If failed to open movies
         */
        FusionFrameData(final File file) throws IOException {
            super(file);
            media = MediaWorker.createMediaWorker(
                getNProperties(), getCloser(),
                ProcessorFactory.createFrameProcessor(
                        getNodeInformation().getLocation()));
            RegisterUtil.invokeReporter(getCloser(),
                    getNodeInformation(), media.getMovieBuffer());
            getFunctions().put(RequestUtil.RequestType.FUSION_TEST,
                (FrameData data, RequestUtil.Request request)
                -> {
                    if (request.getClass() == FusionTestRequest.class) {
                        RequestUtil.FusionTestRequest fusionReq
                                = (RequestUtil.FusionTestRequest) request;
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
        @Override
        public void setNextFlow(final int index) {
            media.setNextFlow(index);
        }
    }
    /**
     * Can't be instantiated with this ctor.
     */
    private FusionFrame() {
    }
}
