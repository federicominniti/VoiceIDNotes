package it.unipi.dii.inginf.dmml.voiceidnotesapp.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VoiceIDNotes extends Application {

    private final static String LOGIN_PAGE = "/fxml/LoginPage.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(VoiceIDNotes.class.getResource(LOGIN_PAGE));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("VoiceIDNotes");
        primaryStage.show();
        primaryStage.setResizable(false);
    }
}
