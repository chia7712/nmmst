package net.nmmst.threads;

import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class BaseTimer implements Timer {
    private final TimeUnit unit;
    private final int time;
    public BaseTimer(int sleepTime, TimeUnit timeUnit) {
        this(timeUnit, sleepTime);
    }
    public BaseTimer(TimeUnit timeUnit, int sleepTime) {
        unit = timeUnit;
        time = sleepTime;
    }
    @Override
    public void sleep() throws InterruptedException {
        unit.sleep(time);
    }
}
