package codes.chia7712.nmmst.media;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sound.sampled.AudioFormat;
import codes.chia7712.nmmst.utils.Painter;

/**
 * Decodes the audio and video from a movie file.
 *
 * @see Frame
 * @see Sample
 */
public final class MovieStream implements MovieAttribute, Closeable {

  /**
   * Indicates the type for current media.
   */
  public enum Type {
    /**
     * Video media.
     */
    VIDEO,
    /**
     * Audio media.
     */
    AUDIO,
    /**
     * End of the movie file. It indicates that no more media can be decoded for
     * thie MovieStream.
     */
    EOF
  };
  /**
   * Scales the timestamp to micro base.
   */
  private static final int TIME_SCALE = 1000 * 1000;
  /**
   * Packets the audio and video data.
   */
  private final IPacket packet = IPacket.make();
  /**
   * Media container.
   */
  private final IContainer container;
  /**
   * Indexes and coders.
   */
  private final Map<Integer, IStreamCoder> openedCoders;
  /**
   * Image converter.
   */
  private final IConverter converter;
  /**
   * Video data.
   */
  private final IVideoPicture picture;
  /**
   * Index of video stream in movie.
   */
  private final int videoStreamIndex;
  /**
   * Index of audio stream in movie.
   */
  private final int audioStreamIndex;
  /**
   * Movie duration.
   */
  private final long duration;
  /**
   * Audio format of movie.
   */
  private final AudioFormat audioFormat;
  /**
   * Index of this movie.
   */
  private final int index;
  /**
   * Local movie file.
   */
  private final File file;

  /**
   * Opens the audio and video coders.
   *
   * @param container The movie container
   * @return The audio and video coders
   * @throws IOException If failed to open the coders
   */
  private static Map<Integer, IStreamCoder> newOpenedCoders(
          final IContainer container) throws IOException {
    Map<Integer, IStreamCoder> openedCoders = new HashMap<>();
    for (int indexStream = 0;
            indexStream != container.getNumStreams();
            ++indexStream) {
      IStream stream = container.getStream(indexStream);
      IStreamCoder coder = stream.getStreamCoder();
      if (coder.isOpen()) {
        throw new IllegalArgumentException();
      }
      switch (coder.getCodecType()) {
        case CODEC_TYPE_VIDEO:
          if (openedCoders.get(indexStream) == null
                  && coder.open(null, null) >= 0) {
            openedCoders.put(indexStream, coder);
          }
          break;
        case CODEC_TYPE_AUDIO:
          if (openedCoders.get(indexStream) == null
                  && coder.open(null, null) >= 0) {
            openedCoders.put(indexStream, coder);
          }
          break;
        default:
          break;
      }
    }
    if (openedCoders.size() != 2) {
      throw new IOException();
    }
    return openedCoders;
  }

  /**
   * Creates a container for specified movie path.
   *
   * @param moviePath The local path of movie
   * @return A container
   * @throws IOException If failed to open local file
   */
  private static IContainer newIContainer(final String moviePath)
          throws IOException {
    IContainer container = IContainer.make();
    if (container.open(moviePath, IContainer.Type.READ, null) < 0) {
      throw new IOException();
    }
    return container;
  }

  /**
   * Constructs a movie stream for local file and specified index. The index
   * should be unique.
   *
   * @param file The local file
   * @param index Movie index
   * @throws IOException If failed to open movie file
   */
  public MovieStream(final File file,
          final int index) throws IOException {
    this.index = index;
    this.file = file;
    container = newIContainer(file.getAbsolutePath());
    openedCoders = newOpenedCoders(container);
    if (openedCoders.get(0).getCodecType()
            == com.xuggle.xuggler.ICodec.Type.CODEC_TYPE_VIDEO) {
      videoStreamIndex = 0;
      audioStreamIndex = 1;
    } else {
      videoStreamIndex = 1;
      audioStreamIndex = 0;
    }
    duration = (long) ((openedCoders.get(videoStreamIndex)
            .getStream()
            .getTimeBase()
            .getDouble()
            * openedCoders.get(videoStreamIndex)
                    .getStream()
                    .getDuration())
            * TIME_SCALE);
    audioFormat = new AudioFormat(
            openedCoders.get(audioStreamIndex).getSampleRate(),
            (int) IAudioSamples.findSampleBitDepth(
                    openedCoders.get(audioStreamIndex).getSampleFormat()),
            openedCoders.get(audioStreamIndex).getChannels(),
            true,
            false);
    IStreamCoder coder = openedCoders.get(videoStreamIndex);
    picture = IVideoPicture.make(
            coder.getPixelType(),
            coder.getWidth(),
            coder.getHeight());
    converter = ConverterFactory.createConverter(new BufferedImage(
            coder.getWidth(),
            coder.getHeight(),
            BufferedImage.TYPE_3BYTE_BGR),
            picture.getPixelType());

  }

  /**
   * Constructs a movie stream for local file and specified index. The index
   * should be unique.
   *
   * @param path The path of local file
   * @param index Movie index
   * @throws IOException If failed to open movie file
   */
  public MovieStream(final String path,
          final int index) throws IOException {
    this(new File(path), index);
  }

  @Override
  public long getDuration() {
    return duration;
  }

  @Override
  public AudioFormat getAudioFormat() {
    return audioFormat;
  }

  /**
   * Retrieves the next media type. User should call this method before invoking
   * {@link #getFrame()} and {@link #getSample()}.
   *
   * @return The type for next media
   */
  public Type readNextType() {
    if (container.readNextPacket(packet) < 0) {
      return Type.EOF;
    }
    IStreamCoder coder = openedCoders.get(packet.getStreamIndex());
    if (coder == null) {
      return Type.EOF;
    }
    switch (coder.getCodecType()) {
      case CODEC_TYPE_VIDEO:
        return Type.VIDEO;
      case CODEC_TYPE_AUDIO:
        return Type.AUDIO;
      default:
        return Type.EOF;
    }
  }

  public Optional<Frame> getFrame() {
    return getFrame(-1);
  }

  /**
   * @param scale Tje resolution scala factor
   * @return If the {@link Frame} is decoded successfully, a optional which
   * maintains a frame will return. Otherwise, a empty optional will return
   */
  public Optional<Frame> getFrame(final double scale) {
    int offset = 0;
    while (offset < packet.getSize()) {
      int bytesDecoded = openedCoders.get(videoStreamIndex)
              .decodeVideo(picture, packet, offset);
      if (bytesDecoded >= 0) {
        offset += bytesDecoded;
        if (picture.isComplete()) {
          BufferedImage image = converter.toImage(picture);
          if (scale > 0) {
            image = Painter.resizeImage(image, scale);
          }
          return Optional.of(new Frame(
                  this,
                  (long) (picture.getTimeStamp()
                  * picture.getTimeBase().getDouble()
                  * TIME_SCALE),
                  image));
        }
      }
      return Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * @return If the {@link Sample} is decoded successfully, a optional which
   * maintains a frame will return. Otherwise, a empty optional will return
   */
  public Optional<Sample> getSample() {
    IAudioSamples samples = IAudioSamples.make(
            packet.getSize(),
            openedCoders.get(audioStreamIndex).getChannels());
    int offset = 0;
    while (offset < packet.getSize()) {
      int bytesDecoded = openedCoders.get(audioStreamIndex)
              .decodeAudio(samples, packet, offset);
      if (bytesDecoded >= 0) {
        offset += bytesDecoded;
        if (samples.isComplete()) {
          return Optional.of(new Sample(this, samples.getData()
                  .getByteArray(0, samples.getSize())));
        }
      }
      return Optional.empty();
    }
    return Optional.empty();
  }

  @Override
  public void close() {
    openedCoders.entrySet().stream().forEach((entry) -> {
      entry.getValue().close();
    });
    openedCoders.clear();
    container.close();
  }

  /**
   * @return If the coders are closed, the {@code true} will return
   */
  public boolean isClosed() {
    return openedCoders.isEmpty();
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public File getFile() {
    return file;
  }
}
