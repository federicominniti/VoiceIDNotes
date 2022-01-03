package it.unipi.dii.inginf.dmml.voiceidnotesapp.utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class VoiceRecorder {
    public static final String AUDIO_PATH = "./temp/VoiceAudio.wav";
    // Record duration, in milliseconds
    public static final long RECORD_TIME = 6000;

    // path where we save the wav file
    private File wavFile = new File(AUDIO_PATH);

    // format of audio file
    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    // the line from which audio data is captured
    private TargetDataLine line;

    /**
     * Defines an audio format
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    /**
     * Captures the sound and record into a WAV file
     */
    public void start() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports the data line
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();   // start capturing

            System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);

            System.out.println("Start recording...");

            // start recording
            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Closes the target data line to finish capturing and recording
     */
    public void finish() {
        line.stop();
        line.close();
        System.out.println("Finished");
    }
}