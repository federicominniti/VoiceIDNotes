package it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.FeatureExtractor;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.VoiceFeature;
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

    private static final String[] sentences = {
            "His family relocated to Indiana when he was a boy. He married Eliza Jane Sumner in 1851",
            "The following year the couple, with Ezra's brother and with their newborn son, set out for the Oregon where land could be claimed and settled on",
            "He became convinced that the Oregon was being forgotten, and he determined to bring it publicity so it could be marked and monuments erected",
            "The Summers lived about four miles from Indianapolis, and like the Martins were family farmers who did not hire help",
            "They encountered Native Americans, who would sometimes demand provisions for passage, but none were given and none of the incidents ended with violence",
            "Despite being a source of food, the bison were a danger as their stampedes could destroy property and kill irreplaceable stock",
            "The fertile soil and temperate climate of the valley proved ideal for hops. Not only did the plants thrive, farmers were able to obtain four or five times the usual yield",
            "Martin strove to improve life in the region, and donated land and money towards town buildings and parks, a theatre and a hotel.",
            "London’s underground subway system is very efficient and easy to use, even though the city is large and bustling.",
            "Although I drank a lot of coffee, I'm getting sleepy, and I don't think I'll make it through the movie.",
            "Even though the nebula could harm our ship, we must push through it: the alien refugees need our help!",
            "I’d like to visit Quebec, but because I don’t speak French, I’ll have to start learning.",
            "Despite his inability to raise funds for mining, Martin was certain there was a way to make money from the gold rush",
            "Meeker returned to the Yukon twice more, in 1899 and 1900. Most of the money earned through groceries was invested in gold mining, and was lost.",
            "Jim doesn’t drink beer because he has a gluten allergy, so he tends to drink wine most weekends.",
            "They sat in a heavy flat-bottomed boat, each holding a long, crooked rod in his hands and eagerly waiting for a bite",
            "As proud as she was of Jonathan, it was Alex who stayed on her mind for a long time after the phone call.",
            "He walked around to the driver's side, ducking his head as he folded his long frame into the car.",
            "I keep telling him that as long as he gives her money, she'll never get out of trouble, but he just says she's the only sister he has and he has the money.",
            "The scents of what looked like pizza night taunted her, and she stood peering through the cracked door at the long dinner table."
    };

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

    private void startRecording(MouseEvent clickEvent) {
        int extracted;
        countRecordings++;
        do {
            extracted = (int) (Math.random()*20);
        } while (alreadyExtractedNums.contains(extracted));

        alreadyExtractedNums.add(extracted);
        sentenceLabel.setText(sentences[extracted]);
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
                        if (countRecordings == 2) {
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

    private void register(MouseEvent clickEvent) {
        //if(!countLabel.getText().equals("10/10")){
        //    Utils.showAlert("Error! Please finish to record your audio");
        //    return;
        //}
        if (!passwordField.getText().equals(repeatPasswordField.getText())){
            Utils.showAlert("Error! Check password fields");
            return;
        }
        CSVManager.appendToCSV(extractedFeatures, usernameTextField.getText());
    }

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
