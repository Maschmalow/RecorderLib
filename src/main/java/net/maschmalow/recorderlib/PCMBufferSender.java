package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioSendHandler;

import java.util.Iterator;


public class PCMBufferSender implements AudioSendHandler {

    private Iterator<byte[]> audioDataIter;


    public void sendPCM(Iterator<byte[]> audioDataIter) {
        this.audioDataIter = audioDataIter;
    }

    @Override
    public boolean canProvide() {
        boolean canProvide = audioDataIter.hasNext();
        if(!canProvide) {
            audioDataIter = null; //attempt to hint garbage collector that the data is not required anymore
            System.gc(); // I don't feel this is good practice, check if it is better to be removed
        }

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
