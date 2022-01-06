package it.unipi.dii.inginf.dmml.voiceidnotesapp.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.VoiceFeature;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.config.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVManager {

    public static void appendToCSV(ArrayList<VoiceFeature> voiceFeatures, String username) {
        CSVWriter writer = null;
        try {
            File registeredFile = new File(Utils.REGISTERED_DATASET_PATH);
            boolean exists = registeredFile.exists();

            writer = new CSVWriter(new FileWriter(Utils.REGISTERED_DATASET_PATH, true));

            if(!exists){
                String header = "mfcc1,mfcc2,mfcc3,mfcc4,mfcc5,mfcc6,mfcc7,mfcc8,mfcc9,mfcc10,mfcc11,mfcc12,mfcc13," +
                        "delta1,delta2,delta3,delta4,delta5,delta6,delta7,delta8,delta9,delta10,delta11,delta12,delta13," +
                        "delta_delta1,delta_delta2,delta_delta3,delta_delta4,delta_delta5,delta_delta6,delta_delta7," +
                        "delta_delta8,delta_delta9,delta_delta10,delta_delta11,delta_delta12,delta_delta13,username";
                writer.writeNext(header.split(","), false);
            }

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

    public static void removeFirstInCSV(String username){
        try {
            CSVReader reader = new CSVReader(new FileReader(Utils.REGISTERED_DATASET_PATH));
            List<String[]> allElements = reader.readAll();
            int index = 0;
            for (int i = 0; i<allElements.size(); i++){
                if(allElements.get(i)[39].equals(username)) {
                    index = i;
                    break;
                }
            }
            allElements.remove(index);
            FileWriter fileWriter = new FileWriter(Utils.REGISTERED_DATASET_PATH);
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(allElements);
            writer.close();
        } catch (IOException | CsvException e) {
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