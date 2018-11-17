package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioSendHandler;

import java.util.Iterator;


public class PCMBufferSender implements AudioSendHandler {

    private Iterator<byte[]> audioDataIter;


    void feedPCM(Iterator<byte[]> audioDataIter) {
        this.audioDataIter = audioDataIter;
    }

    @Override
    public boolean canProvide() {
        if(audioDataIter == null) return false;

        boolean canProvide = audioDataIter.hasNext();
        if(!canProvide)
            audioDataIter = null; //clear all reference so that gc can do its job

        return canProvide;
    }

    @Override
    public byte[] provide20MsAudio() {
        return audioDataIter.next();
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
