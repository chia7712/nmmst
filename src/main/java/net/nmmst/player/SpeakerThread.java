package net.nmmst.player;

import java.util.concurrent.atomic.AtomicBoolean;

import net.nmmst.movie.BufferFactory;
import net.nmmst.movie.MovieBuffer;
import net.nmmst.movie.Sample;
import net.nmmst.tools.Closure;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class SpeakerThread implements Closure {
    private final MovieBuffer buffer = BufferFactory.getMovieBuffer();
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final Speaker speaker;
    public SpeakerThread(Speaker speaker) {
        this.speaker = speaker;
    }

    @Override
    public void close() {
        close.set(true);
    }
    @Override
    public boolean isClosed() {
        return isClosed.get();
    }
    @Override
    public void run() {
        try {
            while(!Thread.interrupted() && !close.get()) {
                Sample sampler = buffer.readSample();
                if(sampler.getData() == null) {
                    break;
                }
                speaker.write(sampler.getData());
            }
        } catch(InterruptedException e) {
            //TODO
            e.printStackTrace();
        } finally {
            speaker.flush();
            isClosed.set(true);
        }

    }

}
