package it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.Note;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;


public class MyNotesController {
    @FXML private TextField newTitleTextField;
    @FXML private TextArea newContentTextArea;
    @FXML private Button saveButton;
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private VBox notesVBox;

    private static final int MAX_TITLE_LENGTH = 15;

    public void initialize(){
        saveButton.setOnMouseClicked(clickEvent -> saveNewNote(clickEvent));
        searchButton.setOnMouseClicked(clickEvent -> searchNote(clickEvent));
        newTitleTextField.addEventFilter(KeyEvent.KEY_TYPED, maxLength());

        //ROBA DB
        //ArrayList<Note> searchedNotes = searchByDatesInterval(LocalDate.parse("1-1-1980"), LocalDate.now());
    }

    private EventHandler<KeyEvent> maxLength() {
        return new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                TextField tx = (TextField) keyEvent.getSource();
                if (tx.getText().length() >= MAX_TITLE_LENGTH || keyEvent.getCharacter().equals(" ")) {
                    keyEvent.consume();
                }
            }
        };
    }

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
                    notesVBox.getChildren().remove(container);
                    //deleteNote();
                }
            }
        };
    }

    private void saveNewNote(MouseEvent clickEvent){
        String title = newTitleTextField.getText();
        String content = newContentTextArea.getText();
        //AGGIUNTA AL DB
        if(!title.equals("") && !content.equals("")) {
            //PROVA AD AGGIUNGERE
                Note newNote = new Note(title, content, LocalDate.now());
                ArrayList<Note> arrayList = new ArrayList<>();
                arrayList.add(newNote);
                createSearchedNotesGUI(arrayList);
            //SE AGGIUNTA NON VA A BUON FINE
                //Utils.showAlert("Problem with connecting with db.. Try again");
            //ELSE
                newTitleTextField.setText("");
                newContentTextArea.setText("");
        } else{
            Utils.showAlert("Empty note");
        }
    }

    private void searchNote(MouseEvent clickEvent){
        String searchedTitle = searchTextField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if(searchedTitle.equals("") && (startDate == null || endDate == null)){
            Utils.showAlert("Please insert search parameters!");
            return;
        }

        ArrayList<Note> searchedNotes;

        if(!searchedTitle.equals("") && startDate != null && endDate != null){
            //searchedNotes = searchByTitleInDatesInterval(searchedTitle, startDate, endDate);
        } else if(!searchedTitle.equals("")){
            //searchedNotes = searchByTitle(title);
        } else if(startDate != null && endDate != null){
            //searchedNotes = searchByDatesInterval(startDate, endDate);
        }
        //createSearchedNotesGUI(searchedNotes);
    }

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

    private void createSearchedNotesGUI(ArrayList<Note> searchedNotes){
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

            Label creationDate = new Label("Creation date: " + searchedNotes.get(i).getCreationDate().toString());
            //creationDate.setLayoutX(205);
            creationDate.setLayoutY(contentTextArea.getLayoutY() + contentTextArea.getPrefHeight() + 8);
            creationDate.setTextFill(Paint.valueOf("#5b5959"));
            creationDate.setFont(Font.font(12));
            creationDate.setPrefWidth(344);
            creationDate.setAlignment(Pos.CENTER_RIGHT);
            noteVBox.getChildren().add(creationDate);


            /*anchorPane.getChildren().add(titleLabel);
            anchorPane.getChildren().add(contentTextArea);
            anchorPane.getChildren().add(creationDate);
            anchorPane.getChildren().add(trashBinContainer);*/

            //notesVBox.getChildren().add(anchorPane);
            noteVBox.setPadding(new Insets(0, 0, 20, 0));
            notesVBox.getChildren().add(noteVBox);
        }
    }
}
