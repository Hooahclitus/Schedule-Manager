package reed.c195_project.util;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import reed.c195_project.AppEntry;
import reed.c195_project.controller.AppointmentController;
import reed.c195_project.controller.CustomerController;
import reed.c195_project.model.Appointment;
import reed.c195_project.model.Customer;

import java.io.IOException;
import java.util.ResourceBundle;

public abstract class SceneManager {
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("rb");

    public static void setupStage(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("login.fxml"), RESOURCE_BUNDLE);
        Scene scene = new Scene(loader.load());
        stage.setTitle(RESOURCE_BUNDLE.getString("title"));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void loadLoginScene(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("login.fxml"), RESOURCE_BUNDLE);
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setTitle(RESOURCE_BUNDLE.getString("title"));
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void loadScheduleScene(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(AppEntry.class.getResource("schedule.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setTitle("Schedule Manager");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void loadAddAppointmentScene(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(AppEntry.class.getResource("add_appointment.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void loadUpdateAppointmentScene(ActionEvent actionEvent, Appointment appointment) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("update_appointment.fxml"));
        Parent root = loader.load();

        AppointmentController appointmentController = loader.getController();
        appointmentController.loadAppointmentDataIntoForm(appointment);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void loadAddCustomerScene(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(AppEntry.class.getResource("add_customer.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void loadUpdateCustomerScene(ActionEvent actionEvent, Customer customer) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("update_customer.fxml"));
        Parent root = loader.load();

        CustomerController customerController = loader.getController();
        customerController.loadCustomerDataIntoForm(customer);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void exitApplication() {
        Platform.exit();
    }
}

















