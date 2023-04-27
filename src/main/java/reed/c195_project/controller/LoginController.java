package reed.c195_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import reed.c195_project.utils.DateTime;
import reed.c195_project.utils.LoadScene;
import reed.c195_project.utils.Validate;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    ResourceBundle resourceBundle;

    @FXML
    private TextField password, username;

    @FXML
    private Label lblTimeZone, lblZoneID, lblUserName, lblPassword;

    @FXML
    private Button login, exit;


    /**
     * Initializes the login form with resource bundle values for various form elements.
     * <p>
     * The lambda expression used in the initialize method is used to iterate through
     * the Map of form elements and corresponding keys in the resource bundle, and
     * set the text of each form element to the localized string value in the
     * resource bundle. This makes the code concise, readable and avoids the need
     * to use a for loop to iterate through the Map.
     *
     * @param url            the URL location of the FXML file
     * @param resourceBundle the resource bundle containing localized string values
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        var loginData = Map.of(
                exit, "exit",
                login, "login",
                lblUserName, "userName",
                lblPassword, "password",
                lblTimeZone, "timeZone");

        loginData.forEach((label, string) -> label.setText(resourceBundle.getString(string)));
        lblZoneID.setText(ZoneId.systemDefault().toString());
    }

    /**
     * Attempts to log the user in and displays an error message if the user's credentials are invalid.
     *
     * @throws SQLException if there is an error communicating with the database.
     */
    @FXML
    private void login(ActionEvent actionEvent) throws SQLException, IOException {
        if (Validate.userCredentials(username, password)) {
            logger("Success");
            LoadScene.schedule(actionEvent, "appointment", true);
        } else {
            logger("Fail");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(resourceBundle.getString("alertHeader"));
            alert.setContentText(resourceBundle.getString("alertContent"));
            alert.show();
        }
    }

    /**
     * Logs the user's login activity to a file.
     *
     * @param status The status of the login activity.
     * @throws IOException if there is an error writing to the log file.
     */
    public void logger(String status) throws IOException {
        String log = String.format("""
                        Username: %s
                        Date/Time: %s (UTC)
                        Login Status: %s
                                                
                        """,
                username.getText(),
                LocalDateTime.now(ZoneOffset.UTC).format(DateTime.dateTimeFormat),
                status);

        try (FileWriter writer = new FileWriter("login_activity.txt", true)) {
            writer.write(log);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Closes the current scene and exits the application.
     */
    @FXML
    private void exit() {
        LoadScene.exit();
    }
}






















