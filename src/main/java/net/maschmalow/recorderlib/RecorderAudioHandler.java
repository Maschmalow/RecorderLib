package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


//this implementation will bufferise PCM audio until AUDIOBUF_MAXSIZE memory is used.
// At this point, it will notify and give the buffer to the application and resume with a empty buffer
class RecorderAudioHandler implements AudioReceiveHandler {

    private static final int QUEUE_MAX_CAPACITY = AudioLib.AUDIOBUF_MAXSIZE*1024*1024 / AudioLib.CHUNK_SIZE; //max size is in MB, chunk size is in B

    private boolean canReceive = true;
    private double volume = 1.0; //default volume 100%
    private int afkTimer = 0;

    private BlockingQueue<byte[]> audioData = new LinkedBlockingQueue<>( QUEUE_MAX_CAPACITY);

    RecorderAudioHandler() {}

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

        if(audioData.remainingCapacity() == 0)
            audioData.poll(); //remove oldest entry when queue is full
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
        return handoutAudio(null);
    }
    Queue<byte[]> handoutAudio(Integer time) {
        Queue<byte[]> ret;
        if(time != null && AudioLib.CHUNK_PER_SECOND * time < audioData.size()) {
            ret = new LinkedBlockingQueue<>( QUEUE_MAX_CAPACITY);
            audioData.drainTo(ret,  AudioLib.CHUNK_PER_SECOND * time);
        }  else {
            ret = audioData;
            audioData = new LinkedBlockingQueue<>( QUEUE_MAX_CAPACITY);
        }

        //afkTimer = 0; // I don't think I should do this
        return ret;
    }

    void setVolume(double volume) {
        this.volume = volume;
    }


    int getCurrentSilenceMs() {
        return afkTimer*(1000/AudioLib.CHUNK_PER_SECOND);
    }
}
