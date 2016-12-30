package codes.chia7712.nmmst.controller;

import java.util.concurrent.atomic.AtomicInteger;

public class CircularNumber {

  private final AtomicInteger count = new AtomicInteger(0);

  public CircularNumber() {
    this(0);
  }

  public CircularNumber(final int initNumber) {
    count.set(initNumber);
  }

  public int get(final int maxNumber) {
    final int v = count.get() % maxNumber;
    if (v < 0) {
      return v + maxNumber;
    } else {
      return v;
    }
  }

  public void set(final int value) {
    count.set(value);
  }

  public void next() {
    count.incrementAndGet();
  }

  public void back() {
    count.decrementAndGet();
  }
}
