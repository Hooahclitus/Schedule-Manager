package project.schedule_manager;

import javafx.application.Application;
import javafx.stage.Stage;
import project.schedule_manager.utils.JDBC;
import project.schedule_manager.utils.LoadScene;

import java.io.IOException;

/**
 * The entry point of the application, responsible for launching the GUI.
 */
public class AppEntry extends Application {

    /**
     * The main method that launches the application.
     *
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
//        Locale.setDefault(new Locale("fr", "FR"));

        JDBC.openConnection();
        launch();
        JDBC.closeConnection();
    }

    /**
     * The start method of the application, which sets up the initial scene to be displayed.
     *
     * @param stage The primary stage of the application.
     * @throws IOException If there is an error loading the login scene.
     */
    @Override
    public void start(Stage stage) throws IOException {
        LoadScene.login(stage);
    }
}
