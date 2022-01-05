package it.unipi.dii.inginf.dmml.voiceidnotesapp.model;

import java.time.LocalDate;
import java.util.Date;

public class Note {
    private String title;
    private String text;
    private Date creationDate;

    public Note(String title, String text, Date creationDate) {
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
