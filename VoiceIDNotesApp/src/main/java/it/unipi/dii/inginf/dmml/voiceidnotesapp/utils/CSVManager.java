package it.unipi.dii.inginf.dmml.voiceidnotesapp.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.VoiceFeature;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVManager {

    /**
     * Appends a list of VoiceFeature instances to the CSV dataset
     * @param voiceFeatures the voice features to be written to the file
     * @param username the label of each voice feature
     */
    public static void appendToCSV(ArrayList<VoiceFeature> voiceFeatures, String username, String path) {
        CSVWriter writer = null;
        try {
            File registeredFile = new File(path);
            boolean exists = registeredFile.exists();

            writer = new CSVWriter(new FileWriter(path, true));

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

    /**
     * Removes the first tuple of voice features of a certain user from the CSV dataset of registered users
     * @param username of the user
     */
    public static void removeFirstInCSV(String username, String path){
        try {
            CSVReader reader = new CSVReader(new FileReader(path));
            List<String[]> allElements = reader.readAll();
            int index = 0;
            for (int i = 0; i<allElements.size(); i++){
                if(allElements.get(i)[39].equals(username)) {
                    index = i;
                    break;
                }
            }
            allElements.remove(index);
            FileWriter fileWriter = new FileWriter(path);
            CSVWriter writer = new CSVWriter(fileWriter);
            for(int i = 0; i<allElements.size(); i++)
                writer.writeNext(allElements.get(i), false);
            writer.close();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    /**
     * Merges the original dataset obtained from LibriSpeech and the dataset of the voice features of real
     * registered users into a temporary CSV dataset
     */
    public static void mergeCSV(String csv1, String csv2){
        try {
            CSVReader registeredData = new CSVReader(new FileReader(csv1));
            CSVReader data = new CSVReader(new FileReader(csv2));
            List<String[]> allElements = data.readAll();
            List<String[]> registeredList = registeredData.readAll();
            registeredList.remove(0);
            allElements.addAll(registeredList);

            FileWriter fileWriter = new FileWriter(Utils.MERGED_DATASET);
            CSVWriter writer = new CSVWriter(fileWriter);
            for(int i = 0; i<allElements.size(); i++)
                writer.writeNext(allElements.get(i), false);
            writer.close();
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

}