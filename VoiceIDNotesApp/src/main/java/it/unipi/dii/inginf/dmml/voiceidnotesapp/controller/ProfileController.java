package it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.Session;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.persistence.LevelDBDriver;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class ProfileController {
    @FXML private Label usernameLabel;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private PasswordField pinField;
    @FXML private PasswordField repeatPinField;
    @FXML private Button modifyPasswordButton;
    @FXML private Button modifyPinButton;
    @FXML private Button cancelButton;
    @FXML private Label resultLabel;


    public void initialize(){
        modifyPasswordButton.setOnMouseClicked(clickEvent -> modifyPassword(clickEvent));
        modifyPinButton.setOnMouseClicked(clickEvent -> modifyPin(clickEvent));
        cancelButton.setOnMouseClicked(clickEvent -> returnToNotes(clickEvent));

        usernameLabel.setText("Hi, " + Session.getLocalSession().getLoggedUser().getUsername());

        //ROBA DB
        //ArrayList<Note> searchedNotes = searchByDatesInterval(LocalDate.parse("1-1-1980"), LocalDate.now());
    }

    private void returnToNotes(MouseEvent clickEvent) {
        Utils.changeScene("/fxml/MyNotes.fxml", clickEvent);
    }

    private void modifyPin(MouseEvent clickEvent){
        if(pinField.getText().equals("") || repeatPinField.getText().equals("") ||
                (!pinField.getText().equals("") && !repeatPinField.getText().equals("") && !pinField.getText().equals(repeatPinField.getText()))){

            Utils.showAlert("Pin must not be empty and must be equal in the two fields!");
            resultLabel.setText("");
        }else {
            Session.getLocalSession().getLoggedUser().setPin(pinField.getText());
            LevelDBDriver.getInstance().changePin(Session.getLocalSession().getLoggedUser());
            pinField.setText("");
            repeatPinField.setText("");
            resultLabel.setText("Pin correctly changed");
        }
    }

    private void modifyPassword(MouseEvent clickEvent){
        if(passwordField.getText().equals("") || repeatPasswordField.getText().equals("") ||
                (!passwordField.getText().equals("") && !repeatPasswordField.getText().equals("") &&
                        !passwordField.getText().equals(repeatPasswordField.getText()))){

            Utils.showAlert("Password must not be empty and must be equal in the two fields!");
            resultLabel.setText("");
        }else {
            Session.getLocalSession().getLoggedUser().setPassword(passwordField.getText());
            LevelDBDriver.getInstance().changePassword(Session.getLocalSession().getLoggedUser());
            passwordField.setText("");
            repeatPasswordField.setText("");
            resultLabel.setText("Password correctly changed");
        }
    }
}
