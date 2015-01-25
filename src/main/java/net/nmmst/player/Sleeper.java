package net.nmmst.player;

import java.util.concurrent.TimeUnit;

public class Sleeper 
{
    private final long microTolerance;
    private long streamStartTime = 0;
    private long clockStartTime = 0;
    public Sleeper(long microTolerance)
    {
        this.microTolerance = microTolerance;
    }
    public long sleepByTimeStamp(long streamCurrentTime) throws InterruptedException
    {

        if(streamStartTime == 0)
        {
            clockStartTime = System.nanoTime();
            streamStartTime = streamCurrentTime;
            return 0;
        }
        final long clockTimeInterval 	= (System.nanoTime() - clockStartTime) / 1000;
        final long streamTimeInterval 	= (streamCurrentTime - streamStartTime);
        final long microsecondsToSleep	= (streamTimeInterval - (clockTimeInterval + microTolerance));
        if(microsecondsToSleep > 0)
        {
            TimeUnit.MICROSECONDS.sleep(microsecondsToSleep);
        }
        return microsecondsToSleep;
    }
    public void reset()
    {
        clockStartTime = 0;
        streamStartTime = 0;

    }
}
