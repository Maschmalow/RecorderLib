package net.maschmalow.recorderlib;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


//this implementation will bufferise PCM audio until AUDIOBUF_MAXSIZE memory is used.
// At this point, it will notify and give the buffer to the application and resume with a empty buffer
class RecorderAudioHandler implements AudioReceiveHandler {

    private static final int BUFFER_MAX_CAPACITY = AudioLib.AUDIOBUF_MAXSIZE*1024*1024 / AudioLib.CHUNK_SIZE; //max size is in MB, chunk size is in B

    private double volume = 1.0; //default volume 100%
    private int afkTimer = 0;

    private final BlockingDeque<byte[]> audioData = new LinkedBlockingDeque<>( BUFFER_MAX_CAPACITY);

    RecorderAudioHandler() {}

    @Override
    public boolean canReceiveCombined() {
        return true;
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
            audioData.removeLast(); //remove oldest entry when queue is full
        audioData.addFirst(combinedAudio.getAudioData(volume));
    }


    @Override
    public void handleUserAudio(@NotNull UserAudio userAudio) {
        throw new UnsupportedOperationException("UserAudio handling is not supported");
    }



    List<byte[]> handoutAudio(Integer time) {
        int numberOfElements = (time == null)? audioData.size() : Math.min(audioData.size(), AudioLib.CHUNK_PER_SECOND * time);
        LinkedList<byte[]> ret = new LinkedList<>();

        Iterator<byte[]> it = audioData.iterator();
        for(int i = 0; i < numberOfElements ; i++) {
            ret.addFirst(it.next());
        }

        return ret;

    }

    void setVolume(double volume) {
        this.volume = volume;
    }


    int getCurrentSilenceMs() {
        return afkTimer*(1000/AudioLib.CHUNK_PER_SECOND);
    }
}
