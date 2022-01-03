package it.unipi.dii.inginf.dmml.voiceidnotesapp.utils;

import com.opencsv.CSVWriter;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.VoiceFeature;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.config.Config;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CSVManager {

    public static void appendToCSV(ArrayList<VoiceFeature> voiceFeatures, String username) {
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(Config.getInstance().getDatasetPath(), true));

            for (int i = 0; i < voiceFeatures.size(); i++) {
                String[] partialRecord = voiceFeatures.get(i).toString().split(" ");
                String[] record = new String[VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA*3 + 1];
                for (int j = 0; j < partialRecord.length; j++) {
                    record[j] = partialRecord[j];
                }
                record[VoiceFeature.NUMBER_MFCC_DELTA_DELTADELTA*3] = username;
                writer.writeNext(record, false);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
            for (int i = 0; i < voiceFeatures.size(); i++) {
                for (int j = 0; j < voiceFeatures.get(i).getMfcc().length; j++) {
                    csvPrinter.print(voiceFeatures.get(i).getMfcc()[j]);
                }
                for (int j = 0; j < voiceFeatures.get(i).getDelta().length; j++) {
                    csvPrinter.print(voiceFeatures.get(i).getDelta()[j]);
                }
                for (int j = 0; j < voiceFeatures.get(i).getDeltadelta().length; j++) {
                    csvPrinter.print(voiceFeatures.get(i).getDeltadelta()[j]);
                }
                csvPrinter.print(username);
                csvPrinter.println();
            } */