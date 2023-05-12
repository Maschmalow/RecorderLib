package net.maschmalow.recorderlib;

import de.sciss.jump3r.lowlevel.LameEncoder;
import net.dv8tion.jda.api.audio.AudioReceiveHandler;

import java.io.IOException;
import java.io.OutputStream;

public class PCMtoMP3Encoder  {
    private final LameEncoder encoder;
    private final OutputStream mp3_out;

    public PCMtoMP3Encoder(OutputStream mp3_out) {
        encoder = new LameEncoder(AudioReceiveHandler.OUTPUT_FORMAT, AudioLib.OUT_MP3_BITRATE, LameEncoder.CHANNEL_MODE_AUTO, LameEncoder.QUALITY_HIGHEST, false);
        this.mp3_out = mp3_out;

    }

    public void feed(byte[] pcm_in) throws IOException {

        byte[] buffer = new byte[encoder.getPCMBufferSize()];

        int bytesToTransfer = Math.min(buffer.length, pcm_in.length);
        int bytesWritten;
        int currentPcmPosition = 0;
        while(0 < (bytesWritten = encoder.encodeBuffer(pcm_in, currentPcmPosition, bytesToTransfer, buffer))) {
            currentPcmPosition += bytesToTransfer;
            bytesToTransfer = Math.min(buffer.length, pcm_in.length - currentPcmPosition);

            mp3_out.write(buffer, 0, bytesWritten);
        }

    }

    public void close() throws IOException {
        encoder.close();
        mp3_out.close();
    }


}
