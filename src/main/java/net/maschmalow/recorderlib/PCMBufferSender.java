package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioSendHandler;

import java.util.Arrays;
import java.util.Iterator;

import static net.maschmalow.recorderlib.AudioLib.CHUNK_SIZE;


public class PCMBufferSender implements AudioSendHandler {


    //temporary workaround because discord is bugged (https://github.com/DV8FromTheWorld/JDA/issues/789)
    //This flag should be set to false if the bug doesn't happen for whatever reason
    //All the active code (including this flag) should be remove if the bug is fixed for good
    private static final boolean AUDIO_BUGFIX_FLAG = true;
    private static byte[] silence = new byte[CHUNK_SIZE];
    static {
        Arrays.fill(silence,(byte)0);
    }

    private Iterator<byte[]> audioDataIter;


    void feedPCM(Iterator<byte[]> audioDataIter) {
        this.audioDataIter = audioDataIter;
    }

    @Override
    public boolean canProvide() {
        if(AUDIO_BUGFIX_FLAG)
            return true;
        else {
            if(audioDataIter == null) return false;

            boolean canProvide = audioDataIter.hasNext();
            if(!canProvide)
                audioDataIter = null; //clear all reference so that gc can do its job

            return canProvide;
        }
    }

    @Override
    public byte[] provide20MsAudio() {

        if(AUDIO_BUGFIX_FLAG) {
            if(audioDataIter != null)
                if(audioDataIter.hasNext())
                    return audioDataIter.next();
                else
                    audioDataIter = null;


            return silence;
        } else {
            return audioDataIter.next();
        }

    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
