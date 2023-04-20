package reed.c195_project;

import javafx.application.Application;
import javafx.stage.Stage;
import reed.c195_project.util.JDBC;
import reed.c195_project.util.SceneManager;

import java.io.IOException;
import java.util.Locale;

public class AppEntry extends Application {

    public static void main(String[] args) {
//        Locale.setDefault(new Locale("fr", "FR"));

        JDBC.openConnection();
        launch();
        JDBC.closeConnection();
    }

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setupStage(stage);
    }
}
