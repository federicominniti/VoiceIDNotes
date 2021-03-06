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
     * Initializes the controller and the needed arrays to avoid showing duplicate random sentences
     */
    public void initialize(){
        countRecordings = 0;
        alreadyExtractedNums = new ArrayList<>();
        extractedFeatures = new ArrayList<>();
        recordButton.setOnMouseClicked(clickEvent -> startRecording(clickEvent));
        cancelButton.setOnMouseClicked(clickEvent -> backToLogin(clickEvent));
        registerButton.setOnMouseClicked(clickEvent -> register(clickEvent));
    }

    private void backToLogin(MouseEvent clickEvent) {
        Utils.changeScene(Utils.LOGIN_PAGE, clickEvent);
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
                    VoiceFeature voicefeature = getRecordingFeatures();
                    if (voicefeature == null) {
                        Utils.changeScene(Utils.LOGIN_PAGE, clickEvent);
                        return;
                    }
                    extractedFeatures.add(getRecordingFeatures());
                    if (countRecordings == 10) {
                        recordButton.setDisable(true);
                        countLabel.setTextFill(Color.GREEN);
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
     * Handle the user's registration with LevelDB local database and appends the voice features to the CSV dataset
     * after performing the SMOTE oversampling
     */
    private void register(MouseEvent clickEvent) {
        if (!Utils.validatePassword(passwordField.getText()) || !Utils.validatePIN(pinField.getText()) ||
            !Utils.validateUsername(usernameTextField.getText())) {
            Utils.showAlert("Password must contain at least 8 characters, 1 upper-case letter and 1 number.\n" +
                            "Pin must be exactly 4 digits \n" +
                            "Username must be an alphanumeric string of at least 5 characters");
            return;
        }
        if (!passwordField.getText().equals(repeatPasswordField.getText())){
            Utils.showAlert("Error! Check password fields");
            return;
        }

        LevelDBDriver dbInstance = LevelDBDriver.getInstance();
        if (dbInstance.registerUser(usernameTextField.getText(), passwordField.getText(), pinField.getText())) {
            CSVManager.appendToCSV(extractedFeatures, usernameTextField.getText(), Utils.REGISTERED_DATASET_PATH);
            try {
                Classifier.oversampleNewVoices();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Utils.changeScene(Utils.LOGIN_PAGE, clickEvent);
        } else {
            Utils.showAlert("Error: username already present");
        }

    }

    /**
     * Function to get the voice features of each audio recorded by the user
     * @return a VoiceFeature instance
     */
    private VoiceFeature getRecordingFeatures() {
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
