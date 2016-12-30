package codes.chia7712.nmmst.media;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Support three mode for showing the image on the panel.
 */
public class BasePanel extends JPanel {

  private static final Log LOG = LogFactory.getLog(BasePanel.class);
  /**
   * Serial id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Indicates how to paint specified image on the panel.
   */
  public enum Mode {
    /**
     * Full screen.
     */
    FULL_SCREEN,
    /**
     * Fill screen.
     */
    FILL,
    /**
     * Extends the image.
     */
    EXTENSION
  };
  /**
   * The image to draw.
   */
  private BufferedImage image = null;
  /**
   * The display mode.
   */
  private Mode mode = Mode.FULL_SCREEN;
  /**
   * Locks the current image.
   */
  private final AtomicBoolean lockImage = new AtomicBoolean(false);

  /**
   * Constructs a empty panel.
   */
  public BasePanel() {
  }

  /**
   * Constructs a panel with initial image.
   *
   * @param mode The mode indicates the appearance for drawing image
   */
  public BasePanel(final Mode mode) {
    this(null, mode);
  }

  /**
   * Constructs a panel with initial image.
   *
   * @param image The image to draw
   */
  public BasePanel(final BufferedImage image) {
    this(image, null);
  }

  /**
   * Constructs a panel with initial image and mode.
   *
   * @param image The image to draw
   * @param mode The mode indicates the appearance for drawing image
   */
  public BasePanel(final BufferedImage image, final Mode mode) {
    this.image = image;
    this.mode = mode == null ? Mode.FULL_SCREEN : mode;
  }

  /**
   * Sets a new mode for this panel. It does not repaint the image right now.
   *
   * @param newMode New mode
   */
  public final void setMode(final Mode newMode) {
    mode = newMode;
  }

  /**
   * Draws a new image right now.
   *
   * @param writeImage The image to draw
   */
  public final void write(final BufferedImage writeImage) {
    if (writeImage != null && !lockImage.get()) {
      image = writeImage;
      repaint();
    }
  }

  /**
   * Unlocks the image.
   */
  public final void unlockImage() {
    lockImage.set(false);
  }

  /**
   * Draws a new image right now and lock the current image.
   *
   * @param writeImage The image to draw
   */
  public final void writeAndLock(final BufferedImage writeImage) {
    if (image != null) {
      lockImage.set(true);
      image = writeImage;
      repaint();
    }
  }

  /**
   * Cleans the panle right now.
   */
  public final void clearImage() {
    if (!lockImage.get()) {
      image = null;
      repaint();
    }

  }

  /**
   * @return The current image to write
   */
  public final Optional<BufferedImage> getCurrentImage() {
    return Optional.ofNullable(image);
  }

  @Override
  public final void paintComponent(final Graphics g) {
    super.paintComponent(g);
    if (image != null) {
      final int width = image.getWidth();
      final int height = image.getHeight();
      if (width != -1 && height != -1) {
        int xAxis = 0;
        int yAxis = 0;
        int finalW, finalH;
        switch (mode) {
          case FULL_SCREEN:
            double minScale = Math.min(
                    (double) getWidth() / (double) width,
                    (double) getHeight() / (double) height);
            finalW = (int) (width * minScale);
            finalH = (int) (height * minScale);
            xAxis = (getWidth() - finalW) / 2;
            yAxis = (getHeight() - finalH) / 2;
            break;
          case FILL:
            finalW = getWidth();
            finalH = getHeight();
            break;
          case EXTENSION:
            double maxScale = Math.max(
                    (double) getWidth() / (double) width,
                    (double) getHeight() / (double) height);
            finalW = (int) (width * maxScale);
            finalH = (int) (height * maxScale);
            break;
          default:
            return;
        }
        g.drawImage(image, xAxis, yAxis, finalW, finalH, null);
      }
      image.flush();
    }
  }
}
