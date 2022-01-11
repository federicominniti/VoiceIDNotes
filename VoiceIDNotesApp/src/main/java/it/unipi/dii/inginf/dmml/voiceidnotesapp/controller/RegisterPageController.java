package it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.Classifier;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.FeatureExtractor;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.VoiceFeature;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.persistence.LevelDBDriver;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.CSVManager;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.VoiceRecorder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;

public class RegisterPageController {
    @FXML private Button registerButton;
    @FXML private Button cancelButton;
    @FXML private Button recordButton;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private PasswordField pinField;
    @FXML private Label countLabel;
    @FXML private Label sentenceLabel;
    private ArrayList<Integer> alreadyExtractedNums;
    private ArrayList<VoiceFeature> extractedFeatures;
    private int countRecordings;

    /**
     * Initializes the controller and the arrays needed to show random sentences
     */
    public void initialize(){
        countRecordings = 0;
        alreadyExtractedNums = new ArrayList<>();
        extractedFeatures = new ArrayList<>();
        recordButton.setOnMouseClicked(clickEvent -> startRecording(clickEvent));
        cancelButton.setOnMouseClicked(clickEvent -> backToLogin(clickEvent));
        registerButton.setOnMouseClicked(clickEvent -> register(clickEvent));
        //ROBA DB
    }

    private void backToLogin(MouseEvent clickEvent) {
        Utils.changeScene("/fxml/LoginPage.fxml", clickEvent);
    }

    /**
     * Creates a new thread 'worker' with the purpose of registering the user's voice.
     * The temporary audio file is then saved and sent immediately to the feature extraction server, and the
     * response containing the features is saved in an array of VoiceFeatures.
     * After 10 successfully recorded audios the recording button is disabled.
     */
    private void startRecording(MouseEvent clickEvent) {
        int extracted;
        countRecordings++;
        do {
            extracted = (int) (Math.random()*20);
        } while (alreadyExtractedNums.contains(extracted));

        alreadyExtractedNums.add(extracted);
        sentenceLabel.setText(Utils.sentences[extracted]);
        countLabel.setText(countRecordings + "/10");
        disableRegisterPageButtons(true);
        Utils.switchImage(recordButton, Utils.START_RECORDING_IMAGE);
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.startRecord();
                Platform.runLater(() -> {
                    Utils.switchImage(recordButton, Utils.END_RECORDING_IMAGE);
                    disableRegisterPageButtons(false);
                    try {
                        VoiceFeature voicefeature = getRecordingFeatures();
                        if (voicefeature == null) {
                            Utils.changeScene("/fxml/LoginPage.fxml", clickEvent);
                            return;
                        }
                        extractedFeatures.add(getRecordingFeatures());
                        if (countRecordings == 10) {
                            recordButton.setDisable(true);
                            countLabel.setTextFill(Color.GREEN);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        worker.start();

    }

    private void disableRegisterPageButtons(boolean flag) {
        recordButton.setDisable(flag);
        registerButton.setDisable(flag);
        cancelButton.setDisable(flag);
    }

    /**
     * Registers the user to the LevelDB local database and appends the voice features to the CSV dataset
     * after performing the SMOTE oversampling
     */
    private void register(MouseEvent clickEvent) {
        //if(!countLabel.getText().equals("10/10")){
        //    Utils.showAlert("Error! Please finish to record your audio");
        //    return;
        //}
        if (!passwordField.getText().equals(repeatPasswordField.getText())){
            Utils.showAlert("Error! Check password fields");
            return;
        }
        LevelDBDriver dbInstance = LevelDBDriver.getInstance();
        if (dbInstance.registerUser(usernameTextField.getText(), passwordField.getText(), pinField.getText())) {
            CSVManager.appendToCSV(extractedFeatures, usernameTextField.getText());
            try {
                Classifier.oversampleNewVoices();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Utils.changeScene("/fxml/LoginPage.fxml", clickEvent);
        } else {
            Utils.showAlert("Error: username already present");
        }

    }

    /**
     * Function to get the voice features of each audio recorded by the user
     * @return a VoiceFeature instance
     */
    private VoiceFeature getRecordingFeatures() throws IOException {
        FeatureExtractor voiceFeatureExtractor = new FeatureExtractor();

        VoiceFeature voiceFeature = voiceFeatureExtractor.getVoiceFeature(VoiceRecorder.AUDIO_PATH);

        if(!(voiceFeature.getMfcc().length != 0 && voiceFeature.getDelta().length != 0
                && voiceFeature.getDeltadelta().length != 0)) {

            Utils.showAlert("Unavailable service, please try again later.");
            return null;
        } else {
            return voiceFeature;
        }
    }



}
