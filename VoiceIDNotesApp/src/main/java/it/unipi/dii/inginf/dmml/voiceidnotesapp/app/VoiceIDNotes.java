package it.unipi.dii.inginf.dmml.voiceidnotesapp.app;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.persistence.LevelDBDriver;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class VoiceIDNotes extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(VoiceIDNotes.class.getResource(Utils.LOGIN_PAGE));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("VoiceIDNotes");
        primaryStage.show();
        primaryStage.setResizable(false);

        //test user
        //LevelDBDriver.getInstance().registerUser("Federico", "Password1", "1234");
    }
}
