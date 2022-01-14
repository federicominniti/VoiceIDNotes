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
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField oldPinField;


    /**
     * Initialize the controller and sets the welcome message for the user
     */
    public void initialize(){
        modifyPasswordButton.setOnMouseClicked(clickEvent -> modifyPassword(clickEvent));
        modifyPinButton.setOnMouseClicked(clickEvent -> modifyPin(clickEvent));
        cancelButton.setOnMouseClicked(clickEvent -> returnToNotes(clickEvent));

        usernameLabel.setText("Hi, " + Session.getLocalSession().getLoggedUser().getUsername());
    }

    private void returnToNotes(MouseEvent clickEvent) {
        Utils.changeScene("/fxml/MyNotes.fxml", clickEvent);
    }

    /**
     * Handles the update of the pin used by the user to login
     */
    private void modifyPin(MouseEvent clickEvent){
        if(!oldPinField.getText().equals(Session.getLocalSession().getLoggedUser().getPin())){
            Utils.showAlert("Old pin is wrong! Please try again");
            return;
        }
        if (!Utils.validatePIN(pinField.getText())) {
            Utils.showAlert("PIN must be exactly 4 digits");
            return;
        }
        if(pinField.getText().equals("") || repeatPinField.getText().equals("") ||
                (!pinField.getText().equals("") && !repeatPinField.getText().equals("") &&
                        !pinField.getText().equals(repeatPinField.getText()))){

            Utils.showAlert("Pin must not be empty and must be equal in the two fields!");
            resultLabel.setText("");
        }else {
            Session.getLocalSession().getLoggedUser().setPin(pinField.getText());
            LevelDBDriver.getInstance().changePin(Session.getLocalSession().getLoggedUser());
            pinField.setText("");
            repeatPinField.setText("");
            oldPinField.setText("");
            resultLabel.setText("Pin correctly changed");
        }
    }

    /**
     * Handles the update of the password used by the user to login
     */
    private void modifyPassword(MouseEvent clickEvent){
        if(!oldPasswordField.getText().equals(Session.getLocalSession().getLoggedUser().getPassword())){
            Utils.showAlert("Old password is wrong! Please try again");
            return;
        }
        if (!Utils.validatePassword(passwordField.getText())) {
            Utils.showAlert("Password must be at least 8 characters, 1 upper-case letter and 1 number");
            return;
        }
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
            oldPasswordField.setText("");
            resultLabel.setText("Password correctly changed");
        }
    }
}
