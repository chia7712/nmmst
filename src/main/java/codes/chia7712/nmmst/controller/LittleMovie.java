package codes.chia7712.nmmst.controller;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import codes.chia7712.nmmst.media.Frame;
import codes.chia7712.nmmst.media.MovieStream;

public class LittleMovie implements Iterable<Frame> {

  private final List<Frame> images;
  private final String description;
  private volatile int index = 0;

  public LittleMovie(
          final File file,
          final long start,
          final long end,
          final TimeUnit timeUnit,
          final double scale,
          final String description) throws IOException {
    images = new LinkedList<>();
    this.description = description;
    final long startTime = TimeUnit.MICROSECONDS.convert(start, timeUnit);
    final long endTime = TimeUnit.MICROSECONDS.convert(end, timeUnit);
    try (MovieStream stream = new MovieStream(file, 0)) {
      boolean over = false;
      while (!over) {
        switch (stream.readNextType()) {
          case VIDEO:
            stream.getFrame(scale)
                    .filter(v -> v.getTimestamp() >= startTime)
                    .ifPresent(v -> images.add(v));
            if (!images.isEmpty()
                    && images.get(images.size() - 1).getTimestamp() >= endTime) {
              over = true;
            }
            break;
          case AUDIO:
            break;
          default:
            over = true;
        }
      }
    }
    if (images.isEmpty()) {
      throw new IOException("No frame is captured from the file:" + file);
    }
  }

  public String getDescription() {
    return description;
  }

  public Frame next() {
    if (index >= images.size()) {
      resetIndex();
      return images.get(index);
    } else {
      return images.get(index++);
    }
  }

  public void resetIndex() {
    index = 0;
  }

  @Override
  public Iterator<Frame> iterator() {
    return images.iterator();
  }
}
