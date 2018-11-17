package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.entities.Guild;

import java.io.IOException;
import java.io.OutputStream;

public class AudioLib {
    public static final int CHUNK_PER_SECOND = 50; //20ms audio chunks
    public static final int BYTES_PER_SEC = (int) AudioReceiveHandler.OUTPUT_FORMAT.getFrameRate() * AudioReceiveHandler.OUTPUT_FORMAT.getFrameSize();
    public static final int CHUNK_SIZE = BYTES_PER_SEC / CHUNK_PER_SECOND; //in byte

    public static final int OUT_MP3_BITRATE = 128; //kbps ofc
    public static final int AUDIOBUF_MAXSIZE = 128; //MB

    private PCMBufferSender sender = new PCMBufferSender();
    private RecorderAudioHandler recorder = new RecorderAudioHandler();
    private Guild guild;


    public AudioLib() {
    }

    public AudioLib(Guild guild) {
        this.guild = guild;
        attachToGuild(guild);
    }


    public void attachToGuild(Guild guild) {
        if(this.guild != null)
            detachFromGuild();
        else
            flush();


        this.guild = guild;

        guild.getAudioManager().setSendingHandler(sender);
        guild.getAudioManager().setReceivingHandler(recorder);
    }

    public void detachFromGuild() {
        flush();
        if(guild == null)
            return;

        guild.getAudioManager().setSendingHandler(null);
        guild.getAudioManager().setReceivingHandler(null);
    }

    public void flush() {
        recorder.handoutAudio();
        System.gc();
    }

    public void flushToOStream(OutputStream dest, Integer time) throws IOException {

        PCMtoMP3Encoder encoder = new PCMtoMP3Encoder(dest);
        for(byte[] chunk : recorder.handoutAudio(time))
            encoder.feed(chunk);
        encoder.close();
        System.gc();
    }

    public void flushToSender(Integer time) {
        sender.feedPCM(recorder.handoutAudio(time).iterator());
        System.gc();
    }


}
