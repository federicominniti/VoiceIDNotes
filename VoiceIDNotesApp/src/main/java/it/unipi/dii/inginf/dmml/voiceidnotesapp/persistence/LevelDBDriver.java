package it.unipi.dii.inginf.dmml.voiceidnotesapp.persistence;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.Note;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.model.User;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class LevelDBDriver {
    private static DB database;
    private static LevelDBDriver driver;
    private static final String DEFAULT_COUNT_AUDIO = "10";

    private LevelDBDriver() {
        database = openDB();
    }

    public static LevelDBDriver getInstance() {
        if (driver == null) {
            driver = new LevelDBDriver();
        }
        return driver;
    }

    private static DB openDB() {
        Options options = new Options();
        options.createIfMissing(true);

        DB db = null;
        try {
            db = factory.open(new File("voiceidnotes_db"), options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return db;
    }

    private void put(String key,String value) {
        database.put(bytes(key), bytes(value));
    }

    private String get(String key) {
        byte[] bytes = database.get(bytes(key));
        return (bytes == null ? null : asString(bytes));
    }

    private void delete(String key) {
        database.delete(bytes(key));
    }

    /**
     * Function that add a new user to the database if there isn't already one with the same username
     * @param username the username to be registered
     * @param password password associated to the username
     * @param pin pin of the new user
     */
    public boolean registerUser(String username, String password, String pin) {
        if (checkIfUserExists(username))
            return false;

        String buildKey = "user:";
        buildKey += username + ":";

        put(buildKey + "password", password);
        put(buildKey + "pin", pin);
        put(buildKey + "countaudio", DEFAULT_COUNT_AUDIO);
        return true;
    }

    private boolean checkIfUserExists(String username) {
        if (get("user:"+username+":password") == null)
            return false;
        return true;
    }

    /**
     * Add a new note of a specific user to the database
     * @param note New note to be added
     * @param user the owner of the note
     */
    public void addNote(Note note, User user) {
        String buildKey = "note:";
        buildKey += user.getUsername() + ":";

        String pattern = "yyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        buildKey += simpleDateFormat.format(note.getCreationDate()) + ":";
        put(buildKey + "title", note.getTitle());
        put(buildKey + "text", note.getText());
    }

    /**
     * Deletes a note of a user from the database
     * @param note the note to be deleted
     * @param user the owner of the note
     */
    public void deleteNote(Note note, User user) {
        String buildKey = "note:";
        buildKey += user.getUsername() + ":";

        String pattern = "yyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        buildKey += simpleDateFormat.format(note.getCreationDate()) + ":";
        delete(buildKey + "title");
        delete(buildKey + "text");
    }

    /**
     * Checks if the user exists and that the credential used on login by the user is the same as the one in the database
     * @param username the user trying to login
     * @param credential a pin or password
     * @param withPin true if credential is a pin, false if it's a password
     * @return the user logged in or null if the login failed
     */
    public User login(String username, String credential, boolean withPin) {
        String buildKey = "user:" + username + ":";
        String pin = get(buildKey + "pin");
        String password = get(buildKey + "password");
        if (withPin && pin.equals(credential)) {
            return (new User(username, password, pin));
        } else if (password.equals(credential)) {
            return (new User(username, password, pin));
        } else
            return null;
    }

    /**
     * Take all the notes of a particular user
     * @param user the owner of the notes
     * @return a list of user's notes
     */
    public List<Note> getAllNotesOfUser(User user) {
        String buildKey = "note:" + user.getUsername();
        List<Note> notes = new ArrayList<>();
        try (DBIterator iterator = database.iterator()) {
            String title = "";
            String text = "";
            for (iterator.seek(bytes(buildKey)); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (!key.startsWith(buildKey)) {
                    break;
                }

                String[] tokens = key.split(":");

                if (tokens[3].equals("text"))
                    text = asString(iterator.peekNext().getValue());

                if (tokens[3].equals("title")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
                    title = asString(iterator.peekNext().getValue());
                    String timestamp = tokens[2];
                    Date date = dateFormat.parse(timestamp);
                    Note newNote = new Note(title, text, date);
                    notes.add(newNote);
                    title = "";
                    text = "";
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return notes;
    }

    /** Updates the content of a note edited by the user
    * @param modifiedNote the edited note to be updated on the database
    * @param user the owner of the note
    */
    public void updateNote(Note modifiedNote, User user){
        String buildKey = "note:" + user.getUsername() + ":";
        String pattern = "yyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        buildKey += simpleDateFormat.format(modifiedNote.getCreationDate()) + ":text";
        put(buildKey, modifiedNote.getText());
    }

    /**
     * Updates the pin credential of a user
     * @param user the user changing their pin
     */
    public void changePin(User user){
        String buildKey = "user:" + user.getUsername() + ":pin";
        put(buildKey, user.getPin());
    }

    /**
     * Updates the password credential of a user
     * @param user the user changing their password
     */
    public void changePassword(User user){
        String buildKey = "user:" + user.getUsername() + ":password";
        put(buildKey, user.getPassword());
    }

}
