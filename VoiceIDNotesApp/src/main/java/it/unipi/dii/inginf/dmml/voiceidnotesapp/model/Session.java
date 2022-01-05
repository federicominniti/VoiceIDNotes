package it.unipi.dii.inginf.dmml.voiceidnotesapp.model;

import java.util.ArrayList;
import java.util.List;

public class Session {
    private static Session localSession = null;
    private User loggedUser;
    private List<Note> userNotes;

    public static void setLocalSession(Session localSession) {
        Session.localSession = localSession;
    }

    public static Session getLocalSession() {
        if(localSession == null)
            synchronized (Session.class){
                localSession = new Session();
            }
        return localSession;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public List<Note> getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(List<Note> userNotes) {
        this.userNotes = userNotes;
    }

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

}
