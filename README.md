# RecorderLib
A basic library to record voice with JDA.
Code is "as-is" and  lacks documentation. See [RecoderBot](https://github.com/Maschmalow/RecorderBot) for an example usage.

## Usage:

        
```java
    //connect to a channel
    AudioLib audioLib =  new AudioLib(guild));
    guild.getAudioManager().openAudioConnection(channel);
    
    //save the last 'time' recorded seconds to an mp3 file 
    audioLib.flushToOStream(Files.newOutputStream(filePath), time);
    
    //play the last 'time' recorded seconds in the connected channel
    audioLib.flushToSender(time);
    
    //leave channel
    guild.getAudioManager().closeAudioConnection();
    audiolib.detachFromGuild();
```



