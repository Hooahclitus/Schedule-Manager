package reed.c195_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import reed.c195_project.util.SceneManager;
import reed.c195_project.util.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The LoginController class manages the login form for the application.
 * It initializes the form with resource bundle values for various form elements
 * and attempts to log the user in, displaying an error message if the user's credentials are invalid.
 * <p>
 * Author: Charles Reed
 * <p>
 * Date: 2023-04-13
 */
public class LoginController implements Initializable {
    ResourceBundle resourceBundle;

    @FXML
    private TextField fldPassword, fldUserName;

    @FXML
    private Label lblTimeZone, lblZoneID, lblUserName, lblPassword;

    @FXML
    private Button btnLogin, btnExit;

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

        var loginDataMap = Map.of(
                btnExit, "exit",
                btnLogin, "login",
                lblUserName, "userName",
                lblPassword, "password",
                lblTimeZone, "timeZone");

        loginDataMap.forEach((label, string) -> label.setText(resourceBundle.getString(string)));
        lblZoneID.setText(ZoneId.systemDefault().toString());
    }

    /**
     * Attempts to log the user in and displays an error message if the user's credentials are invalid.
     *
     * @throws SQLException if there is an error communicating with the database.
     */
    @FXML
    private void login(ActionEvent actionEvent) throws SQLException, IOException {
        if (Validate.user(fldUserName, fldPassword)) {
            SceneManager.loadScheduleScene(actionEvent);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(resourceBundle.getString("alertHeader"));
            alert.setContentText(resourceBundle.getString("alertContent"));
            alert.show();
        }
    }

    @FXML
    private void exit() {
        SceneManager.exitApplication();
    }
}
