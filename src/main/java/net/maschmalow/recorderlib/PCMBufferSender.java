package net.maschmalow.recorderlib;

import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;
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
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(audioDataIter.next());
    }

    @Override
    public boolean isOpus() {
        return false;
    }
}
