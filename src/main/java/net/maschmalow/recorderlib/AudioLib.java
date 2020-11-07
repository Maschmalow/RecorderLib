package net.maschmalow.recorderlib;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.io.OutputStream;

public class AudioLib {
    public static final int CHUNK_PER_SECOND = 50; //20ms audio chunks
    public static final int BYTES_PER_SEC = (int) AudioReceiveHandler.OUTPUT_FORMAT.getFrameRate() * AudioReceiveHandler.OUTPUT_FORMAT.getFrameSize();
    public static final int CHUNK_SIZE = BYTES_PER_SEC / CHUNK_PER_SECOND; //in byte

    public static final int OUT_MP3_BITRATE = 128; //kbps ofc
    public static final int AUDIOBUF_MAXSIZE = 128; //MB

    private PCMBufferSender sender;
    private RecorderAudioHandler recorder;
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

        this.guild = guild;
        sender = new PCMBufferSender();
        recorder = new RecorderAudioHandler();

        guild.getAudioManager().setSendingHandler(sender);
        guild.getAudioManager().setReceivingHandler(recorder);
    }

    public void detachFromGuild() {
        if(guild == null)
            return;

        sender = null;
        recorder = null;

        guild.getAudioManager().setSendingHandler(null);
        guild.getAudioManager().setReceivingHandler(null);
    }


    public void flushToOStream(OutputStream dest, Integer time) throws IOException {

        PCMtoMP3Encoder encoder = new PCMtoMP3Encoder(dest);
        for(byte[] chunk : recorder.handoutAudio(time))
            encoder.feed(chunk);
        encoder.close();
    }

    public void flushToSender(Integer time) {
        sender.feedPCM(recorder.handoutAudio(time).iterator());
    }


}
