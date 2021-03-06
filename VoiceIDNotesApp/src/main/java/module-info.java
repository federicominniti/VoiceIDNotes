module it.unipi.dii.inginf.dmml.voiceidnotesapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jshell;
    requires xstream;
    requires java.xml;
    requires java.desktop;
    requires com.opencsv;
    requires leveldb;
    requires leveldb.api;
    requires weka.smote;

    opens it.unipi.dii.inginf.dmml.voiceidnotesapp.config to xstream;
    opens it.unipi.dii.inginf.dmml.voiceidnotesapp.app to javafx.fxml;
    opens it.unipi.dii.inginf.dmml.voiceidnotesapp.controller to javafx.fxml;
    exports it.unipi.dii.inginf.dmml.voiceidnotesapp.config;
    exports it.unipi.dii.inginf.dmml.voiceidnotesapp.app;
    exports it.unipi.dii.inginf.dmml.voiceidnotesapp.controller;
}