package it.unipi.dii.inginf.dmml.voiceidnotesapp.model;

public class Session {
    private static Session localSession = null;
    private User loggedUser;

    private Session(){};

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

    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }
}
