package it.unipi.dii.inginf.dmml.voiceidnotesapp.classification;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.config.Config;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class FeatureExtractor {
    private final String voiceExtractionServerIP;
    private final int voiceExtractionServerPort;
    private DataOutputStream dataOutputStream;
    private BufferedReader bufferedReader;
    private final int USER_AUDIO_CHUNK_SIZE = 8 * 1024;

    public FeatureExtractor() throws IOException {
        this.voiceExtractionServerIP = Config.getInstance().getVoiceExtractorServerIP();
        this.voiceExtractionServerPort = Config.getInstance().getVoiceExtractorServerPort();
        //this.voiceExtractionServerIP = "127.0.0.1";
        //this.voiceExtractionServerPort = 5001;

    }

    public VoiceFeature getVoiceFeature(String audioPath){
        VoiceFeature voiceFeature = new VoiceFeature();
        try(Socket voiceExtractionServerSocket = new Socket(voiceExtractionServerIP, voiceExtractionServerPort)){
            dataOutputStream = new DataOutputStream(voiceExtractionServerSocket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(voiceExtractionServerSocket.getInputStream()));
            sendUserAudio(audioPath);

            List<String> featuresValues = Arrays.asList(bufferedReader.readLine().split(" "));

            System.out.println(featuresValues);
            double[] mfcc = new double[VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA];
            double[] delta = new double[VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA];
            double[] deltadelta = new double[VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA];
            for (int i = 0; i < VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA; i++){
                mfcc[i] = Double.parseDouble(featuresValues.get(i));
                delta[i] = Double.parseDouble(featuresValues.get(i + VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA));
                deltadelta[i] = Double.parseDouble(featuresValues.get(i + VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA));
            }
            voiceFeature.setMfcc(mfcc);
            voiceFeature.setDelta(delta);
            voiceFeature.setDeltadelta(deltadelta);

            dataOutputStream.close();
            bufferedReader.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return voiceFeature;
    }

    private void sendUserAudio(String path) throws Exception{
        File userAudio = new File(path);
        FileInputStream fileInputStream = new FileInputStream(userAudio);
        dataOutputStream.writeUTF("extract");
        dataOutputStream.flush();
        Thread.sleep(500);
        dataOutputStream.writeUTF(String.valueOf(userAudio.length()));
        dataOutputStream.flush();
        Thread.sleep(500);
        int chunkBytes;
        byte[] buffer = new byte[USER_AUDIO_CHUNK_SIZE];
        while ((chunkBytes = fileInputStream.read(buffer)) != -1){
            dataOutputStream.write(buffer, 0, chunkBytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
}