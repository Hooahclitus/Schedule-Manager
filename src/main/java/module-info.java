module reed.c195_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;


    opens reed.c195_project to javafx.fxml;
    exports reed.c195_project;
    exports reed.c195_project.controller;
    opens reed.c195_project.controller to javafx.fxml;
}
