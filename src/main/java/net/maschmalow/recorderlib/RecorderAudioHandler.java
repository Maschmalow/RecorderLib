package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static net.maschmalow.recorderlib.AudioLib.CHUNK_PER_SECOND;

//this implementation will bufferise PCM audio until AUDIOBUF_MAXSIZE memory is used.
// At this point, it will notify and give the buffer to the application and resume with a empty buffer
public class RecorderAudioHandler implements AudioReceiveHandler {

    private final AudioLib parent;
    private boolean canReceive = true;
    private double volume = 1.0; //default volume 100%
    private int afkTimer = 0;

    private Queue<byte[]> audioData = new LinkedBlockingQueue<>( AudioLib.AUDIOBUF_MAXSIZE*1024*1024 / AudioLib.CHUNK_SIZE);//max size is in MB, chunk size is in B

    public RecorderAudioHandler(AudioLib parent) {
        this.parent = parent;
    }

    @Override
    public boolean canReceiveCombined() {
        return canReceive;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        if (combinedAudio.getUsers().size() == 0) {
            afkTimer++;
        } else {
            afkTimer = 0;
        }

        audioData.add(combinedAudio.getAudioData(volume));
    }


    @Override
    public void handleUserAudio(UserAudio userAudio) {
        throw new UnsupportedOperationException("UserAudio handling is not supported");
    }

    void stop() {
        canReceive = false;
        handoutAudio();
    }

    void start() {
        canReceive = true;
    }


    Queue<byte[]> handoutAudio() {
        Queue<byte[]> ret = audioData;
        audioData = null;
        afkTimer = 0;
        return ret;
    }

    void setVolume(double volume) {
        this.volume = volume;
    }


    public int getCurrentSilenceMs() {
        return afkTimer*(1000/CHUNK_PER_SECOND);
    }
}
