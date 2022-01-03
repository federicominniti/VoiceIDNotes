package it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.Classifier;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.FeatureExtractor;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.VoiceFeature;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.VoiceRecorder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;


public class LoginPageController {
    @FXML private Button recordButton;
    @FXML private Label foundUsernameLabel;
    @FXML private PasswordField pinTextField;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    public void initialize(){
        recordButton.setOnMouseClicked(clickEvent -> startRecording(clickEvent));
        loginButton.setOnMouseClicked(clickEvent -> loginHandler(clickEvent));
        registerButton.setOnMouseClicked(clickEvent -> registrationHandler(clickEvent));
        //ROBA DB
    }

    private void startRecording(MouseEvent clickEvent) {
        disableLoginPageButtons(true);
        Utils.switchImage(recordButton, Utils.START_RECORDING_IMAGE);

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.startRecord();
                Platform.runLater(() -> {
                    Utils.switchImage(recordButton, Utils.END_RECORDING_IMAGE);
                    disableLoginPageButtons(false);
                    try {
                        getVoiceLabel();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        worker.start();
    }

    private void loginHandler(MouseEvent clickEvent){
        String usernameVoiceDetected, pin, username, password;
        usernameVoiceDetected = foundUsernameLabel.getText().substring(3);
        pin = pinTextField.getText();
        username = usernameTextField.getText();
        password = passwordTextField.getText();

        if((pin.equals("") && login(username, password)) || (!pin.equals("") && login(usernameVoiceDetected, pin))) {
            Utils.changeScene("/fxml/MyNotes.fxml", clickEvent);
        }else
        if(pin.equals("")) {
            Utils.showAlert("Error! Your username and/or password are wrong. Try again");
        }else {
            Utils.showAlert("Error! Your voice has not been recognized or the PIN you have inserted is wrong. " +
                    "Please try login with your username and password or restart the voice identification");
        }
    }

    private void registrationHandler(MouseEvent clickEvent){
        Utils.changeScene("/fxml/RegisterPage.fxml", clickEvent);
    }

    private void disableLoginPageButtons(boolean flag) {
        recordButton.setDisable(flag);
        loginButton.setDisable(flag);
        registerButton.setDisable(flag);
    }

    private void getVoiceLabel() throws IOException {
        FeatureExtractor voiceFeatureExtractor = new FeatureExtractor();

        VoiceFeature voiceFeature = voiceFeatureExtractor.getVoiceFeature(VoiceRecorder.AUDIO_PATH);

        if(voiceFeature.getMfcc().length != 0 && voiceFeature.getDelta().length != 0
                && voiceFeature.getDeltadelta().length != 0) {

            Classifier voiceClassifier = Classifier.getClassifierInstance(false);
            String recognisedUsername = voiceClassifier.classify(voiceFeature.toInstance());
            foundUsernameLabel.setText("Hi " + recognisedUsername);

        } else{
            Utils.showAlert("Unavailable service, please try again later.");
        }
    }

    //DA LEVARE
    boolean login(String a, String b){
        return true;
    }
}