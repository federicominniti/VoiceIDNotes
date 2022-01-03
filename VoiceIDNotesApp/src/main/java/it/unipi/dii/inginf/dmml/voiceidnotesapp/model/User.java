package it.unipi.dii.inginf.dmml.voiceidnotesapp.model;

public class User {
    private String username;
    private String password;
    private String pin;
    private int countAudio;

    public User(String username, String password, String pin, int countAudio) {
        this.username = username;
        this.password = password;
        this.pin = pin;
        this.countAudio = countAudio;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPin() {
        return pin;
    }

    public int getCountAudio() {
        return countAudio;
    }

    public void setCountAudio(int countAudio) {
        this.countAudio = countAudio;
    }
}
