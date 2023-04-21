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

public abstract class LoadScene {
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("rb");

    public static <T> void login(T actionEventOrStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("login.fxml"), RESOURCE_BUNDLE);
        Scene scene = new Scene(loader.load());
        Stage stage = null;

        if (actionEventOrStage instanceof ActionEvent actionEvent) {
            stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        } else if (actionEventOrStage instanceof Stage s) {
            stage = s;
        }

        stage.setTitle(RESOURCE_BUNDLE.getString("title"));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void schedule(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(AppEntry.class.getResource("schedule.fxml"));
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setTitle("Schedule Manager");
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void appointment(ActionEvent actionEvent, Appointment... appointment) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("appointment.fxml"));
        Parent root = loader.load();

        AppointmentController appointmentController = loader.getController();
        appointmentController.setupAppointmentForm(appointment);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void customer(ActionEvent actionEvent, Customer... customer) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("customer.fxml"));
        Parent root = loader.load();

        CustomerController customerController = loader.getController();
        customerController.setupCustomerForm(customer);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    public static void exit() {
        Platform.exit();
    }
}

















