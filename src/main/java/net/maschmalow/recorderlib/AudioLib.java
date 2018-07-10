package net.maschmalow.recorderlib;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import tech.gdragon.Utilities;
import tech.gdragon.configuration.ServerSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Date;

public class AudioLib {
    public static final int CHUNK_PER_SECOND = 50; //20ms audio chunks
    public static final int BYTES_PER_SEC = (int) AudioReceiveHandler.OUTPUT_FORMAT.getFrameRate()*AudioReceiveHandler.OUTPUT_FORMAT.getFrameSize();
    public static final int CHUNK_SIZE = BYTES_PER_SEC/CHUNK_PER_SECOND; //in byte

    public static final int OUT_MP3_BITRATE = 128;
    public static final int AUDIOBUF_MAXSIZE = 128; //MB

    private PCMBufferSender sender = new PCMBufferSender();
    private RecorderAudioHandler recorder = new RecorderAudioHandler(this);

    public AudioLib(Guild guild) {
        attachToGuild(guild);
    }



    public static void writeToFile(Guild guild, String filename, TextChannel tc, Integer time) {
        if(tc == null) {
            tc = guild.getDefaultChannel();
        }


        RecorderAudioHandler ah = (RecorderAudioHandler) guild.getAudioManager().getReceiveHandler();
        if(ah == null) {
            Utilities.sendMessage(tc, "I wasn't recording!");
            return;
        }

        if(filename == null)
            filename = "untitled_recording";
        filename = DateFormat.getDateInstance().format(new Date()) + filename + ".mp3";

        File dest = Paths.get(ServerSettings.getRecordingsPath(), filename ).toFile();


        try {
            flushToOStream(null, new FileOutputStream(dest));
        } catch(IOException ex) {
            ex.printStackTrace();
            Utilities.sendMessage(tc, "Error saving file: "+ex.getMessage());
        }

        System.out.format("Saved audio file '%s' from %s on %s of size %f MB\n",
                dest.getName(), guild.getAudioManager().getConnectedChannel().getName(), guild.getName(), (double) dest.length() / 1024 / 1024);

        if(dest.length() / 1024 / 1024 < 8) {
            final TextChannel channel = tc;
            tc.sendFile(dest).queue(message -> {
                        dest.delete();
                        System.out.println("\tDeleting file " + dest.getName() + "...");
                    },
                    (Throwable) -> Utilities.sendMessage(guild.getDefaultChannel(),
                            "I don't have permissions to send files in " + channel.getName() + "!"));

        } else {
            Utilities.sendMessage(tc, ServerSettings.getRecordingsURL() + dest.getName());
        }



    }

    public void attachToGuild(Guild guild) {
        flush();
        guild.getAudioManager().setSendingHandler(sender);
        guild.getAudioManager().setReceivingHandler(recorder);
    }

    public void detachFromGuild(Guild guild) {
        flush();
        guild.getAudioManager().setSendingHandler(null);
        guild.getAudioManager().setReceivingHandler(null);
    }

    public void flush() {
        recorder.handoutAudio();
        System.gc();
    }

    public void flushToOStream( OutputStream dest) throws IOException {

        PCMtoMP3Encoder encoder = new PCMtoMP3Encoder(dest);
        for(byte[] chunk : recorder.handoutAudio())
            encoder.feed(chunk);
        encoder.close();
        System.gc();
    }

    public void flushToSender(PCMBufferSender dest) {
        sender.sendPCM(recorder.handoutAudio().iterator());
    }


}
