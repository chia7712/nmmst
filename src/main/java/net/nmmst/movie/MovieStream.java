package net.nmmst.movie;

import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
/**
 *
 * @author Tsai ChiaPing <chia7712@gmail.com>
 */
public class MovieStream implements MovieAttribute {
    public enum	Type{VIDEO, AUDIO, EOF};
    private final IPacket packet = IPacket.make();
    private final IContainer container;
    private final Map<Integer, IStreamCoder> openedCoders;
    private final IConverter converter;
    private final IVideoPicture picture;
    private final int videoStreamIndex;
    private final int audioStreamIndex;
    private final long duration;
    private final AudioFormat audioFormat;
    private final int movieIndex;
    private final String moviePath;
    private static Map<Integer, IStreamCoder> newOpenedCoders(IContainer container) throws IOException {
        Map<Integer, IStreamCoder> openedCoders	= new HashMap();
        for (int indexStream = 0; indexStream != container.getNumStreams(); ++indexStream) {
            IStream stream = container.getStream(indexStream);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.isOpen()) {
                throw new IllegalArgumentException();
            }
            switch(coder.getCodecType()) {
                case CODEC_TYPE_VIDEO:
                    if (openedCoders.get(indexStream) == null && coder.open(null, null) >= 0) {
                        openedCoders.put(indexStream, coder);		
                    }
                    break;
                case CODEC_TYPE_AUDIO:
                    if (openedCoders.get(indexStream) == null && coder.open(null, null) >= 0) {
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
    private static IContainer newIContainer(String moviePath) throws IOException {
        IContainer container = IContainer.make();
        if (container.open(moviePath, IContainer.Type.READ, null) < 0) {
            throw new IOException();
        }
        return container;
    }
    public MovieStream(String moviePath, int movieIndex) throws IOException {
        container = newIContainer(moviePath);
        openedCoders = newOpenedCoders(container);
        if (openedCoders.get(0).getCodecType() == com.xuggle.xuggler.ICodec.Type.CODEC_TYPE_VIDEO) {
            videoStreamIndex = 0;
            audioStreamIndex = 1;
        } else {
            videoStreamIndex = 1;
            audioStreamIndex = 0;

        }
        duration = (long)( (openedCoders.get(videoStreamIndex).getStream().getTimeBase().getDouble() * openedCoders.get(videoStreamIndex).getStream().getDuration()) * 1000);
        audioFormat = new AudioFormat(
            openedCoders.get(audioStreamIndex).getSampleRate(),
            (int)IAudioSamples.findSampleBitDepth(openedCoders.get(audioStreamIndex).getSampleFormat()),
            openedCoders.get(audioStreamIndex).getChannels(),
            true, 
            false
        );	
        IStreamCoder coder = openedCoders.get(videoStreamIndex);
        picture	= IVideoPicture.make(coder.getPixelType(), coder.getWidth(), coder.getHeight());
        converter = ConverterFactory.createConverter(new BufferedImage(coder.getWidth(), coder.getHeight(), BufferedImage.TYPE_3BYTE_BGR), picture.getPixelType());
        this.movieIndex = movieIndex;
        this.moviePath = moviePath;
    }
    @Override
    public long getDuration() {
        return duration;
    }
    public AudioFormat getAudioFormt() {
        return  new AudioFormat(
            audioFormat.getEncoding(),
            audioFormat.getSampleRate(), 
            audioFormat.getSampleSizeInBits(), 
            audioFormat.getChannels(),
            audioFormat.getFrameSize(),
            audioFormat.getFrameRate(),
            audioFormat.isBigEndian());
    }
    public Type readNextType() {
        if (container.readNextPacket(packet) < 0) {
            return Type.EOF;
        }
        IStreamCoder coder = openedCoders.get(packet.getStreamIndex());
        if (coder == null) {
            return Type.EOF; 
        }
        switch(coder.getCodecType()) {
            case CODEC_TYPE_VIDEO:
                return Type.VIDEO;
            case CODEC_TYPE_AUDIO:
                return Type.AUDIO;
            default: 
                return Type.EOF;
        }
    }
    public Frame getFrame() {
        int offset = 0;
        while (offset < packet.getSize()) {
            int bytesDecoded = openedCoders.get(videoStreamIndex).decodeVideo(picture, packet, offset);
            if (bytesDecoded >= 0) {
                offset += bytesDecoded;
                if (picture.isComplete()) {
                    return new Frame(this, picture.getTimeStamp(), converter.toImage(picture));
                }							
            } else {
                return null;
            }
        }	
        return null;
    }
    public Sample getSample() {
        IAudioSamples samples = IAudioSamples.make(packet.getSize(), openedCoders.get(audioStreamIndex).getChannels());
        int offset = 0;
        while (offset < packet.getSize()) {
            int bytesDecoded = openedCoders.get(audioStreamIndex).decodeAudio(samples, packet, offset);
            if (bytesDecoded >= 0) {
                offset += bytesDecoded;
                if (samples.isComplete()) {
                    return new Sample(this, samples.getData().getByteArray(0, samples.getSize()));
                }
            } else {
                return null;
            }
        }	
        return null;
    }
    public void close() {
        for (Map.Entry<Integer, IStreamCoder> entry : openedCoders.entrySet()) {
            entry.getValue().close();
        }
        openedCoders.clear();
        container.close();
    }
    public boolean isClosed() {
        return openedCoders.isEmpty();
    }
    @Override
    public int getIndex() {
        return movieIndex;
    }
    @Override
    public String getPath() {
        return moviePath;
    }
    @Override
    public AudioFormat getAutioFormat() {
        return audioFormat;
    }
}
