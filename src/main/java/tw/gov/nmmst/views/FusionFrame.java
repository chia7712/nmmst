package tw.gov.nmmst.views;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tw.gov.nmmst.NConstants;
import tw.gov.nmmst.media.MediaWorker;
import tw.gov.nmmst.processor.ProcessorFactory;
import tw.gov.nmmst.media.BasePanel;
import tw.gov.nmmst.processor.LinearProcessor;
import tw.gov.nmmst.utils.RegisterUtil;
import tw.gov.nmmst.utils.RequestUtil;
import tw.gov.nmmst.utils.RequestUtil.FusionTestRequest;
import tw.gov.nmmst.utils.RequestUtil.Request;
import tw.gov.nmmst.utils.RequestUtil.SetImageRequest;
/**
 * The fusion node plays the movies with fusing the image edge.
 */
public final class FusionFrame {
    /**
     * Log.
     */
    private static final Log LOG
            = LogFactory.getLog(FusionFrame.class);
    /**
     * Invokes a fusion frame.
     * @param args Properties path or no use
     * @throws IOException If failed to open movie
     */
    public static void main(final String[] args) throws IOException {
        File file = null;
        if (args.length == 1) {
            file = new File(args[0]);
        }
        if (file == null) {
            LOG.info("No found of configuration, use the default");
        } else {
            LOG.info("use the configuration : " + file.getPath());
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
                        getNodeInformation().getLocation()), null);
            RegisterUtil.invokeReporter(getCloser(),
                    getNodeInformation(), media.getMovieBuffer());
            getFunctions().put(RequestUtil.RequestType.FUSION_TEST,
                (FrameData data, Request previousReq, Request currentReq)
                -> {
                    if (currentReq.getClass() == FusionTestRequest.class) {
                        FusionTestRequest fusionReq
                                = (RequestUtil.FusionTestRequest) currentReq;
                        BufferedImage image = fusionReq.getImage();
                        LinearProcessor processor
                            = new LinearProcessor(
                                data.getNodeInformation().getLocation(),
                                fusionReq.getFactor());
                        processor.process(image);
                        data.getMainPanel().write(image);
                    }
                });
            getFunctions().put(RequestUtil.RequestType.SET_IMAGE,
                (FrameData data, Request previousReq, Request currentReq)
                -> {
                    if (currentReq.getClass() == SetImageRequest.class) {
                        SetImageRequest fusionReq
                                = (SetImageRequest) currentReq;
                        List<BufferedImage> images = fusionReq.getImage();
                        if (images.isEmpty()) {
                            return;
                        }
                        int index
                            = getNodeInformation().getLocation().ordinal();
                        if (index >= images.size()) {
                            index = 0;
                        }
                        data.getMainPanel().write(images.get(index));
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
