module reed.c195_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.java;


    opens project.schedule_manager to javafx.fxml;
    exports project.schedule_manager;
    exports project.schedule_manager.controller;
    opens project.schedule_manager.controller to javafx.fxml;
}
