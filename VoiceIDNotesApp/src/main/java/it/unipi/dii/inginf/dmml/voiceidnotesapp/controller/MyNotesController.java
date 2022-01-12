package it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.Note;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.Session;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.persistence.LevelDBDriver;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import weka.knowledgeflow.CallbackNotifierDelegate;

import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


public class MyNotesController {
    @FXML private TextField newTitleTextField;
    @FXML private TextArea newContentTextArea;
    @FXML private Button saveButton;
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private VBox notesVBox;
    @FXML private Button profileButton;

    private static final int MAX_TITLE_LENGTH = 25;

    /**
     * Initializes the controller and loads all the user's notes
     */
    public void initialize(){
        saveButton.setOnMouseClicked(clickEvent -> saveNewNote(clickEvent));
        searchButton.setOnMouseClicked(clickEvent -> searchNote(clickEvent));
        newTitleTextField.addEventFilter(KeyEvent.KEY_TYPED, maxLength());
        profileButton.setOnMouseClicked(clickEvent -> myProfile(clickEvent));

        createSearchedNotesGUI(Session.getLocalSession().getUserNotes());
    }

    private void myProfile(MouseEvent clickEvent) {
        Utils.changeScene(Utils.PROFILE_PAGE, clickEvent);
    }

    /**
     * Stops the user when they try to enter too long titles
     */
    private EventHandler<KeyEvent> maxLength() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                TextField tx = (TextField) keyEvent.getSource();
                if (tx.getText().length() >= MAX_TITLE_LENGTH) {
                    keyEvent.consume();
                }
            }
        };
    }

    /**
     * Deletes a note both from the GUI and calls the delete on the database
     * @param toBeDeleted the note to be deleted
     * @param container the GUI container in which the note is shown
     */
    private EventHandler<MouseEvent> deleteNoteHandler(Note toBeDeleted, VBox container) {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirm delete");
                alert.setHeaderText("Are you sure want to delete this note? There is no turning back");
                Image warningImg = new Image(MyNotesController.class.getResource("/img/warning.png").toString());
                ImageView warningImgView = new ImageView(warningImg);
                warningImgView.setFitHeight(70);
                warningImgView.setFitWidth(70);
                alert.setGraphic(warningImgView);
                Optional <ButtonType> option = alert.showAndWait();

                if (option.get() == ButtonType.OK) {
                    LevelDBDriver dbInstance = LevelDBDriver.getInstance();
                    dbInstance.deleteNote(toBeDeleted, Session.getLocalSession().getLoggedUser());
                    Session.getLocalSession().getUserNotes().remove(toBeDeleted);
                    notesVBox.getChildren().remove(container);
                    //deleteNote();
                }
            }
        };
    }

    /**
     * Handles the addition of a new note to the database and shows it into the GUI
     */
    private void saveNewNote(MouseEvent clickEvent){
        String title = newTitleTextField.getText();
        String content = newContentTextArea.getText();
        if(!title.equals("") && !content.equals("")) {
            Note newNote = new Note(title, content, new Date());
            LevelDBDriver driver = LevelDBDriver.getInstance();
            driver.addNote(newNote, Session.getLocalSession().getLoggedUser());
            Session.getLocalSession().getUserNotes().add(newNote);
            notesVBox.getChildren().clear();
            createSearchedNotesGUI(Session.getLocalSession().getUserNotes());
            newTitleTextField.setText("");
            newContentTextArea.setText("");
        } else{
            Utils.showAlert("Empty note");
        }
    }

    /**
     * Filters notes by title or by creation date
     */
    private void searchNote(MouseEvent clickEvent){
        String searchedTitle = searchTextField.getText();
        LocalDate startDateLocal = startDatePicker.getValue();
        LocalDate endDateLocal = endDatePicker.getValue();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        Date startDate = null;
        Date endDate = null;
        if(startDateLocal != null && endDateLocal != null) {
            startDate = Date.from(startDateLocal.atStartOfDay(defaultZoneId).toInstant());
            endDate = Date.from(endDateLocal.atStartOfDay(defaultZoneId).toInstant());
        }

        if((startDateLocal == null && endDateLocal != null) || (startDateLocal != null && endDateLocal == null)){
            Utils.showAlert("Please insert search parameters!");
            return;
        }

        List<Note> searchedNotes;

        List<Note> userNotes;
        userNotes = Session.getLocalSession().getUserNotes();
        searchedNotes = new ArrayList<>();
        for (int i = 0; i < userNotes.size(); i++) {
            if (!searchedTitle.equals("") && startDate != null && endDate != null) {
                if (userNotes.get(i).getTitle().contains(searchedTitle)
                        && startDate.before(userNotes.get(i).getCreationDate())
                        && endDate.after(userNotes.get(i).getCreationDate()))

                    searchedNotes.add(userNotes.get(i));
            } else if (!searchedTitle.equals("")) {
                if (userNotes.get(i).getTitle().contains(searchedTitle))
                    searchedNotes.add(userNotes.get(i));
            } else if (startDate != null && endDate != null) {
                if (startDate.before(userNotes.get(i).getCreationDate())
                        && endDate.after(userNotes.get(i).getCreationDate()))

                    searchedNotes.add(userNotes.get(i));
            }
        }

        if(searchedTitle.equals("") && startDateLocal == null && endDateLocal == null) {
            searchedNotes = Session.getLocalSession().getUserNotes();
        }

        notesVBox.getChildren().clear();
        createSearchedNotesGUI(searchedNotes);
        searchTextField.setText("");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    /**
     * Calculates the size of the TextArea based on the content length of the note
     * @param searchedNote the note to be displayed
     * @return the number of rows needed
     */
    private int getNumbersOfTextRow(Note searchedNote){
        String[] splittedContent= searchedNote.getText().split("\n");
        int numRow = 0;
        for(int j = 0; j<splittedContent.length; j++){
            if(splittedContent[j].length() > 45)
                numRow += splittedContent[j].length()/45;
            else numRow++;
        }
        return numRow;
    }

    /**
     * Updates the GUI containing the notes to be displayed to the user
     * @param searchedNotes a list of notes to be shown
     */
    private void createSearchedNotesGUI(List<Note> searchedNotes){
        for(int i = 0; i<searchedNotes.size(); i++){

            VBox noteVBox = new VBox();
            BorderPane headerNote = new BorderPane();
            headerNote.setPrefWidth(344);

            Label titleLabel = new Label(searchedNotes.get(i).getTitle());
            titleLabel.setPrefWidth(311);
            titleLabel.setMaxWidth(300);
            titleLabel.setWrapText(true);
            titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));

            Image trashBin = new Image(String.valueOf(MyNotesController.class.getResource("/img/trash-bin.png")));
            ImageView trashBinContainer = new ImageView(trashBin);
            trashBinContainer.setFitWidth(26);
            trashBinContainer.setFitHeight(24);
            trashBinContainer.setPickOnBounds(true);
            trashBinContainer.setPreserveRatio(true);
            trashBinContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, deleteNoteHandler(searchedNotes.get(i), noteVBox));

            headerNote.setRight(trashBinContainer);
            headerNote.setLeft(titleLabel);
            noteVBox.getChildren().add(headerNote);

            TextArea contentTextArea = new TextArea(searchedNotes.get(i).getText());
            contentTextArea.setPrefWidth(344);
            contentTextArea.setWrapText(true);
            contentTextArea.setStyle("-fx-vbar-policy: never");


            int numRow = getNumbersOfTextRow(searchedNotes.get(i));
            contentTextArea.setPrefRowCount(numRow);
            contentTextArea.setMinHeight(numRow*17 + 10);
            noteVBox.getChildren().add(contentTextArea);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Label creationDate = new Label("Creation date: " + dateFormat.format(searchedNotes.get(i).getCreationDate()));
            creationDate.setLayoutY(contentTextArea.getLayoutY() + contentTextArea.getPrefHeight() + 8);
            creationDate.setTextFill(Paint.valueOf("#5b5959"));
            creationDate.setFont(Font.font(12));

            BorderPane footer = new BorderPane();
            footer.setPrefWidth(344);
            footer.setLeft(creationDate);
            contentTextArea.addEventHandler(MouseEvent.MOUSE_CLICKED, showUpdateButton(searchedNotes.get(i), footer, contentTextArea));

            noteVBox.getChildren().add(footer);

            noteVBox.setPadding(new Insets(0, 0, 20, 0));
            notesVBox.getChildren().add(noteVBox);
        }
    }


    /**
     * Shows the update button after the edit of a note
     * @param toBeModified the note to be edited
     * @param footer the border pane that will contain the update button
     * @param contentTextArea the edited textarea
     */
    private EventHandler<MouseEvent> showUpdateButton(Note toBeModified, BorderPane footer, TextArea contentTextArea) {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Image update = new Image(String.valueOf(MyNotesController.class.getResource("/img/update.png")));
                ImageView updateContainer = new ImageView(update);
                updateContainer.setFitWidth(26);
                updateContainer.setFitHeight(24);
                updateContainer.setPickOnBounds(true);
                updateContainer.setPreserveRatio(true);
                updateContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, updateNoteHandler(toBeModified, footer, contentTextArea));
                footer.setRight(updateContainer);
            }
        };
    }

    /**
     * Updates the content of a note edited by the user
     * @param toBeModified the edited note
     * @param footer the border pane containing the update button
     * @param contentTextArea the textarea of the edited note
     */
    private EventHandler<MouseEvent> updateNoteHandler(Note toBeModified, BorderPane footer, TextArea contentTextArea) {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                List<Note> notes = Session.getLocalSession().getUserNotes();
                notes.get(notes.indexOf(toBeModified)).setText(contentTextArea.getText());
                toBeModified.setText(contentTextArea.getText());
                LevelDBDriver.getInstance().updateNote(toBeModified, Session.getLocalSession().getLoggedUser());
                footer.setRight(null);
            }
        };
    }

}
