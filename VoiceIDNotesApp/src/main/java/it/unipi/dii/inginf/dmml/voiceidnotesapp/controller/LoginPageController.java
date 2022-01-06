package it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.Classifier;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.FeatureExtractor;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.classification.VoiceFeature;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.Note;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.Session;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.User;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class LoginPageController {
    @FXML private Button recordButton;
    @FXML private Label foundUsernameLabel;
    @FXML private PasswordField pinTextField;
    @FXML private TextField usernameTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label sentenceLabel;

    private VoiceFeature lastFeatureExtracted;

    public void initialize(){
        recordButton.setOnMouseClicked(clickEvent -> startRecording(clickEvent));
        loginButton.setOnMouseClicked(clickEvent -> loginHandler(clickEvent));
        registerButton.setOnMouseClicked(clickEvent -> registrationHandler(clickEvent));

        int extracted = (int) (Math.random()*20);
        sentenceLabel.setText(Utils.sentences[extracted]);
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

        if((pin.equals("") && login(username, password, false)) || (!pin.equals("") && login(usernameVoiceDetected, pin, true))) {
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

        lastFeatureExtracted = voiceFeatureExtractor.getVoiceFeature(VoiceRecorder.AUDIO_PATH);

        if(lastFeatureExtracted.getMfcc().length != 0 && lastFeatureExtracted.getDelta().length != 0
                && lastFeatureExtracted.getDeltadelta().length != 0) {

            Classifier voiceClassifier = Classifier.getClassifierInstance(false);
            String recognisedUsername = voiceClassifier.classify(lastFeatureExtracted.toInstance());
            foundUsernameLabel.setText("Hi " + recognisedUsername);

        } else{
            Utils.showAlert("Unavailable service, please try again later.");
        }
    }

    //DA LEVARE
    boolean login(String username, String credential, boolean withPin){
        LevelDBDriver dbInstance = LevelDBDriver.getInstance();
        User loggedUser;
        if ((loggedUser = dbInstance.login(username, credential, withPin)) != null) {
            Session session = Session.getLocalSession();
            List<Note> userNotes = dbInstance.getAllNotesOfUser(loggedUser);
            session.setLoggedUser(loggedUser);
            session.setUserNotes(userNotes);

            if(withPin && loggedUser.getCountAudio() < 100) {
                loggedUser.setCountAudio(loggedUser.getCountAudio() + 1);
                dbInstance.updateCount(loggedUser);
                ArrayList<VoiceFeature> singleList = new ArrayList<>();
                singleList.add(lastFeatureExtracted);
                CSVManager.appendToCSV(singleList, loggedUser.getUsername());
            } else if(withPin && loggedUser.getCountAudio() >= 100) {
                CSVManager.removeFirstInCSV(loggedUser.getUsername());
                ArrayList<VoiceFeature> singleList = new ArrayList<>();
                singleList.add(lastFeatureExtracted);
                CSVManager.appendToCSV(singleList, loggedUser.getUsername());
            }

            return true;
        } else {
            return false;
        }
    }
}