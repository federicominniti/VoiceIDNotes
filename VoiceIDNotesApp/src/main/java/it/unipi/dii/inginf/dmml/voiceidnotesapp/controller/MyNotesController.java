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
        //ROBA DB
        //ArrayList<Note> searchedNotes = searchByDatesInterval(LocalDate.parse("1-1-1980"), LocalDate.now());
    }

    private void myProfile(MouseEvent clickEvent) {
        Utils.changeScene("/fxml/Profile.fxml", clickEvent);
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
     * Deletes a note both from the GUI and the database
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
     * Saves a new note to the database and shows it into the GUI
     */
    private void saveNewNote(MouseEvent clickEvent){
        String title = newTitleTextField.getText();
        String content = newContentTextArea.getText();
        //AGGIUNTA AL DB
        if(!title.equals("") && !content.equals("")) {
            //PROVA AD AGGIUNGERE
            Note newNote = new Note(title, content, new Date());
            LevelDBDriver driver = LevelDBDriver.getInstance();
            driver.addNote(newNote, Session.getLocalSession().getLoggedUser());
            Session.getLocalSession().getUserNotes().add(newNote);
            notesVBox.getChildren().clear();
            createSearchedNotesGUI(Session.getLocalSession().getUserNotes());
            //SE AGGIUNTA NON VA A BUON FINE
            //Utils.showAlert("Problem with connecting with db.. Try again");
            //ELSE
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
     * Adapts the height of the TextArea of the single note based on the number of rows of the note's content
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
     * Updates the VBox containing the notes to be displayed to the user
     * @param searchedNotes a list of notes to be shown
     */
    private void createSearchedNotesGUI(List<Note> searchedNotes){
        for(int i = 0; i<searchedNotes.size(); i++){
            //AnchorPane anchorPane = new AnchorPane();
            //anchorPane.setPrefWidth(344);

            VBox noteVBox = new VBox();
            //HBox titleHBox = new HBox();
            BorderPane headerNote = new BorderPane();
            headerNote.setPrefWidth(344);

            Label titleLabel = new Label(searchedNotes.get(i).getTitle());
            //titleLabel.setLayoutX(2);
            //titleLabel.setLayoutY(1);
            titleLabel.setPrefWidth(311);
            titleLabel.setMaxWidth(300);
            titleLabel.setWrapText(true);
            titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            //titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
            //titleHBox.getChildren().add(titleLabel);

            Image trashBin = new Image(String.valueOf(MyNotesController.class.getResource("/img/trash-bin.png")));
            ImageView trashBinContainer = new ImageView(trashBin);
            trashBinContainer.setFitWidth(26);
            trashBinContainer.setFitHeight(24);
            //trashBinContainer.setLayoutX(324);
            //trashBinContainer.setLayoutY(8);
            trashBinContainer.setPickOnBounds(true);
            trashBinContainer.setPreserveRatio(true);
            trashBinContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, deleteNoteHandler(searchedNotes.get(i), noteVBox));

            //titleHBox.getChildren().add(trashBinContainer);
            headerNote.setRight(trashBinContainer);
            headerNote.setLeft(titleLabel);
            //noteVBox.getChildren().add(titleHBox);
            noteVBox.getChildren().add(headerNote);

            TextArea contentTextArea = new TextArea(searchedNotes.get(i).getText());
            //contentTextArea.setLayoutY(32);
            contentTextArea.setPrefWidth(344);
            contentTextArea.setWrapText(true);
            contentTextArea.setStyle("-fx-vbar-policy: never");


            int numRow = getNumbersOfTextRow(searchedNotes.get(i));
            contentTextArea.setPrefRowCount(numRow);
            contentTextArea.setMinHeight(numRow*17 + 10);
            //contentTextArea.setPrefHeight(8.5 + (numRow * 17));
            //anchorPane.setPrefHeight(60 + 8);
            noteVBox.getChildren().add(contentTextArea);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Label creationDate = new Label("Creation date: " + dateFormat.format(searchedNotes.get(i).getCreationDate()));
            //creationDate.setLayoutX(205);
            creationDate.setLayoutY(contentTextArea.getLayoutY() + contentTextArea.getPrefHeight() + 8);
            creationDate.setTextFill(Paint.valueOf("#5b5959"));
            creationDate.setFont(Font.font(12));
            //creationDate.setPrefWidth(344);

            /*Image update = new Image(String.valueOf(MyNotesController.class.getResource("/img/update.png")));
            ImageView updateContainer = new ImageView(update);
            updateContainer.setFitWidth(26);
            updateContainer.setFitHeight(24);
            //trashBinContainer.setLayoutX(324);
            //trashBinContainer.setLayoutY(8);
            updateContainer.setPickOnBounds(true);
            updateContainer.setPreserveRatio(true);
            updateContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, updateNoteHandler(searchedNotes.get(i)), noteVBox);
            */
            BorderPane footer = new BorderPane();
            footer.setPrefWidth(344);
            footer.setLeft(creationDate);
            contentTextArea.addEventHandler(MouseEvent.MOUSE_CLICKED, showUpdateButton(searchedNotes.get(i), footer, contentTextArea));

            //footer.setRight(updateContainer);
            //creationDate.setAlignment(Pos.CENTER_RIGHT);
            //noteVBox.getChildren().add(creationDate);
            noteVBox.getChildren().add(footer);


            /*anchorPane.getChildren().add(titleLabel);
            anchorPane.getChildren().add(contentTextArea);
            anchorPane.getChildren().add(creationDate);
            anchorPane.getChildren().add(trashBinContainer);*/

            //notesVBox.getChildren().add(anchorPane);
            noteVBox.setPadding(new Insets(0, 0, 20, 0));
            notesVBox.getChildren().add(noteVBox);
        }
    }


    /**
     * Shows the update button after the edit of a saved note
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
                //trashBinContainer.setLayoutX(324);
                //trashBinContainer.setLayoutY(8);
                updateContainer.setPickOnBounds(true);
                updateContainer.setPreserveRatio(true);
                updateContainer.addEventHandler(MouseEvent.MOUSE_CLICKED, updateNoteHandler(toBeModified, footer, contentTextArea));
                footer.setRight(updateContainer);
            }
        };
    }

    /**
     * Updates the content of a saved note edited by the user
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
