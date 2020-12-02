# IPTV-Recorder
Record IPTV from a URL or a M3U file.

Just a fun project to be able to record IPTV from either a .m3u file or from a URL directly. Every setting is stored in config.properties.

After compiling this code for example to IPTV-Recorder.jar the config.properties have to be located in the same directory as the jar since the program will look for it under the same directory. Do not rename config.properties.

Recording can be done either with ffmpeg or without it, I prefer without it and therefor: useFFMPEG=false

Tested under windows and Linux/Ubuntu, build it with JDK 1.8 and run it with JRE 1.8.

Under windows run PowerShell with:

java.exe -jar IPTV-Recorder.jar

Under Linux run it with:

java -jar IPTV-Recorder.jar

Have fun and let me know if it sucks! :D
