package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.entities.Guild;

import java.io.IOException;
import java.io.OutputStream;

public class AudioLib {
    public static final int CHUNK_PER_SECOND = 50; //20ms audio chunks
    public static final int BYTES_PER_SEC = (int) AudioReceiveHandler.OUTPUT_FORMAT.getFrameRate()*AudioReceiveHandler.OUTPUT_FORMAT.getFrameSize();
    public static final int CHUNK_SIZE = BYTES_PER_SEC/CHUNK_PER_SECOND; //in byte

    public static final int OUT_MP3_BITRATE = 128; //kbps ofc
    public static final int AUDIOBUF_MAXSIZE = 128; //MB

    private PCMBufferSender sender = new PCMBufferSender();
    private RecorderAudioHandler recorder = new RecorderAudioHandler(this);

    public AudioLib(Guild guild) {
        attachToGuild(guild);
    }


    public void attachToGuild(Guild guild) {
        flush();
        guild.getAudioManager().setSendingHandler(sender);
        guild.getAudioManager().setReceivingHandler(recorder);
    }

    public void detachFromGuild(Guild guild) {
        flush();
        guild.getAudioManager().setSendingHandler(null);
        guild.getAudioManager().setReceivingHandler(null);
    }

    public void flush() {
        recorder.handoutAudio();
        System.gc();
    }

    public void flushToOStream( OutputStream dest) throws IOException {

        PCMtoMP3Encoder encoder = new PCMtoMP3Encoder(dest);
        for(byte[] chunk : recorder.handoutAudio())
            encoder.feed(chunk);
        encoder.close();
        System.gc();
    }

    public void flushToSender(PCMBufferSender dest) {
        sender.sendPCM(recorder.handoutAudio().iterator());
    }


}
