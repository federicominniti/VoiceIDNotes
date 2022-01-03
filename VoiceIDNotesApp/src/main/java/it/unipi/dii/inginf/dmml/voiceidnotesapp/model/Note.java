package it.unipi.dii.inginf.dmml.voiceidnotesapp.model;

import java.time.LocalDate;

public class Note {
    private String title;
    private String text;
    private LocalDate creationDate;

    public Note(String title, String text, LocalDate creationDate) {
        this.title = title;
        this.text = text;
        this.creationDate = creationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
}
