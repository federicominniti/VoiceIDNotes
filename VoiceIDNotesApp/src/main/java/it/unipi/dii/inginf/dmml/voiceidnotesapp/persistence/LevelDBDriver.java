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

    public void addNote(Note note, User user) {
        String buildKey = "note:";
        buildKey += user.getUsername() + ":";

        String pattern = "yyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        buildKey += simpleDateFormat.format(note.getCreationDate()) + ":";
        put(buildKey + "title", note.getTitle());
        put(buildKey + "text", note.getText());
    }

    public void deleteNote(Note note, User user) {
        String buildKey = "note:";
        buildKey += user.getUsername() + ":";

        String pattern = "yyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        buildKey += simpleDateFormat.format(note.getCreationDate()) + ":";
        delete(buildKey + "title");
        delete(buildKey + "text");
    }

    public boolean login(String username, String credential, boolean withPin) {
        if (withPin) {
            String buildKey = "user:" + username + ":" + "pin";
            String pin = get(buildKey);
            if (pin.equals(credential))
                return true;
            return false;
        } else {
            String buildKey = "user:" + username + ":" + "password";
            String password = get(buildKey);
            if (password.equals(credential))
                return true;
            return false;
        }
    }
/*
    public List<Note> getAllNotesOfUser(User user) {
        String buildKey = "note:" + user.getUsername();
        List<Note> list = new ArrayList<>();
        try (DBIterator iter = database.iterator()) {
            iter.seek(bytes(buildKey));

            while (iter.hasNext()) {
                Map.Entry<byte[], byte[]> current = iter.next();
                String partialKey = asString(current.getKey());
                String title = "";
                String text = "";
                Date d = null;
                if (partialKey.equals("title"))
                    title = asString(current.getValue());
                else if (partialKey.equals("text"))
                    text = asString(current.getValue());
                else if (partialKey.equals("countaudio")) {

                }
                else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                    d = sdf.parse(asString(current.getKey()));
                }

                list.add(new Note(title, text, d));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
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

    public void updateNote(Note modifiedNote, User user){
        String buildKey = "note:" + user.getUsername() + ":";
        String pattern = "yyMMddHHmmss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        buildKey += simpleDateFormat.format(modifiedNote.getCreationDate()) + ":text";
        put(buildKey, modifiedNote.getText());
    }

}
