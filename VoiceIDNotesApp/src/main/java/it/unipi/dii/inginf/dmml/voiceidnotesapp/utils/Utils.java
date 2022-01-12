package it.unipi.dii.inginf.dmml.voiceidnotesapp.utils;

//import com.sun.javafx.scene.layout.region.Margins;

import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.IOException;

public class Utils {
    public final static String START_RECORDING_IMAGE = "/img/voice-recording.png";
    public final static String END_RECORDING_IMAGE = "/img/microphone-default.png";
    public final static String REGISTERED_DATASET_PATH = "registeredUser.csv";
    public final static String MERGED_DATASET = "temp/mergedDataset.csv";
    public static final String[] sentences = {
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

    /**
     * Utility function that uses the VoiceRecorder class to start the recording of an audio
     */
    public static void startRecord() {

        VoiceRecorder recorder = new VoiceRecorder();
        // creates a new thread that waits for a specified
        // of time before stopping
        Thread stopper = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(VoiceRecorder.RECORD_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                recorder.finish();
            }
        });

        stopper.start();

        // start recording
        recorder.start();
    }

    /**
     * Utility function that shows an error alert to the user
     * @param message the message to be displayed
     */
    public static void showAlert(String message){
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setContentText(message);
        errorAlert.setHeaderText("Please click 'OK' and try again");
        errorAlert.setTitle("Error...");
        Image errorImage = new Image(Utils.class.getResource("/img/error-icon.png").toString());
        ImageView errorImageView = new ImageView(errorImage);
        errorImageView.setFitHeight(70);
        errorImageView.setFitWidth(70);
        errorAlert.setGraphic(errorImageView);
        errorAlert.show();
    }

    /**
     * Utility function that switches the image contained in the recording button
     * @param recordButton the button clicked to record
     * @param imagePath the path to the image to be set into the button
     */
    public static void switchImage(Button recordButton, String imagePath){
        imagePath = Utils.class.getResource(imagePath).toString();
        Image recordingImages = new Image(imagePath);
        ImageView regordingImagesView = new ImageView(recordingImages);
        regordingImagesView.setFitHeight(72.0);
        regordingImagesView.setFitWidth(52.0);
        regordingImagesView.setPreserveRatio(true);
        recordButton.setGraphic(regordingImagesView);
    }

    /**
     * Utility function to change the current scene
     * @param fxmlScene the path to the fxml of the next scene to be loaded
     */
    public static Object changeScene(String fxmlScene, Event clickEvent){
        try {
            Scene scene;
            FXMLLoader fxmlLoader;
            System.out.println(Utils.class.getResource(fxmlScene));
            fxmlLoader = new FXMLLoader(Utils.class.getResource(fxmlScene));

            Scene oldScene = ((Node)clickEvent.getSource()).getScene();
            Stage stage = (Stage) oldScene.getWindow();
            scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.show();
            return fxmlLoader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads the dataset of both registered users voice features and LibriSpeech voice features
     * @param datasetPath the path of the dataset to be loaded
     * @return the Instances of the dataset
     */
    public static Instances loadDataset(String datasetPath) throws Exception {
        CSVManager.mergeCSV();
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(datasetPath);
        Instances dataset = dataSource.getDataSet();
        dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }

    /**
     * Utility function to check that the password has at least 8 characters, 1 upper-case letter and 1 number
     * @param password the password to be checked
     */
    public static boolean validatePassword(String password) {
        if (password.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$"))
            return true;
        return false;
    }

    /**
     * Utility function to check that the PIN is made of four numeric digits
     * @param pin the pin to be checked
     */
    public static boolean validatePIN(String pin) {
        if (pin.matches("^[0-9]{4}$"))
            return true;
        return false;
    }

    /**
     * Utility function to check that the username is alphanumeric and longer than 5 characters
     * @param username the username to be checked
     */
    public static boolean validateUsername(String username) {
        if (username.length() >= 5)
            return true;
        return false;
    }

}