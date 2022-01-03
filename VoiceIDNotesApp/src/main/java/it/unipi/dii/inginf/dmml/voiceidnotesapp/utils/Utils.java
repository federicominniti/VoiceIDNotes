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

    public static void switchImage(Button recordButton, String imagePath){
        imagePath = Utils.class.getResource(imagePath).toString();
        Image recordingImages = new Image(imagePath);
        ImageView regordingImagesView = new ImageView(recordingImages);
        regordingImagesView.setFitHeight(72.0);
        regordingImagesView.setFitWidth(52.0);
        regordingImagesView.setPreserveRatio(true);
        recordButton.setGraphic(regordingImagesView);
    }

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

    public static Instances loadDataset(String datasetPath) throws Exception {
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(datasetPath);
        Instances dataset = dataSource.getDataSet();
        dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }

}