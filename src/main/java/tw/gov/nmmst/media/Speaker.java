package tw.gov.nmmst.media;

import java.io.Closeable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Audio output.
 */
public final class Speaker implements Closeable {

  /**
   * Control the audio using standard java library.
   */
  private final SourceDataLine line;

  /**
   * Construct a speaker by audio format.
   *
   * @param audioFormat Audio format
   * @throws LineUnavailableException if failed to open audio output
   */
  public Speaker(final AudioFormat audioFormat)
          throws LineUnavailableException {
    DataLine.Info info
            = new DataLine.Info(SourceDataLine.class, audioFormat);
    line = (SourceDataLine) AudioSystem.getLine(info);
    line.open(audioFormat);
    line.start();
  }

  /**
   * Writes the audio data.
   *
   * @param data Audio data
   */
  public void write(final byte[] data) {
    int count = 0;
    while (count != data.length) {
      count += line.write(data, count, data.length - count);
    }
  }

  /**
   * Gets the reference to inner audio format.
   *
   * @return Audio format
   */
  public AudioFormat getAudioFormat() {
    return line.getFormat();
  }

  /**
   * Drains queued data from the line by continuing data I/O until the data
   * line's internal buffer has been emptied.
   *
   * @see #flush()
   */
  public void drain() {
    line.drain();
  }

  /**
   * Flushes queued data from the line.
   *
   * @see #drain()
   */
  public void flush() {
    line.flush();
  }

  @Override
  public void close() {
    line.drain();
    line.close();
  }
}
