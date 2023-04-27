package reed.c195_project.utils;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import reed.c195_project.AppEntry;
import reed.c195_project.controller.AppointmentController;
import reed.c195_project.controller.CustomerController;
import reed.c195_project.controller.ScheduleController;
import reed.c195_project.model.Appointment;
import reed.c195_project.model.Customer;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * This abstract class provides methods for loading different scenes in the application.
 */
public abstract class LoadScene {
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("rb");

    /**
     * This method loads the login scene of the application.
     *
     * @param actionEventOrStage the ActionEvent object or the Stage object to use for displaying the scene
     * @throws IOException if there is an error loading the FXML file for the scene
     */
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

    /**
     * This method loads the schedule scene of the application.
     *
     * @param actionEvent the ActionEvent object to use for displaying the scene
     * @param tab the tab to select initially in the scene (either "appointment" or "customer")
     * @param isInitialLogin a boolean indicating if this is the initial login of the user
     * @throws IOException if there is an error loading the FXML file for the scene
     */
    public static void schedule(ActionEvent actionEvent, String tab, boolean isInitialLogin) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("schedule.fxml"));
        Parent root = loader.load();

        ScheduleController scheduleController = loader.getController();

        if (isInitialLogin) {
            scheduleController.upcomingAppointmentsAlert();
        }

        if (tab.equals("appointment")) {
            scheduleController.selectAppointmentsTab();
        } else if (tab.equals("customer")) {
            scheduleController.selectCustomersTab();
        }

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    /**
     * This method loads the appointment scene of the application.
     *
     * @param actionEvent the ActionEvent object to use for displaying the scene
     * @param appointments the list of Appointment objects to display in the scene
     * @param appointment the Appointment objects to pre-populate the form with (optional)
     * @throws IOException if there is an error loading the FXML file for the scene
     */
    public static void appointment(ActionEvent actionEvent, ObservableList<Appointment> appointments,
                                   Appointment... appointment) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("appointment.fxml"));
        Parent root = loader.load();

        AppointmentController appointmentController = loader.getController();
        appointmentController.configureAppointmentForm(appointment);
        appointmentController.passAppointments(appointments.stream().toList());

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    /**
     * This method loads the customer scene of the application.
     *
     * @param actionEvent the ActionEvent object to use for displaying the scene
     * @param customer the Customer object to pre-populate the form with (optional)
     * @throws IOException if there is an error loading the FXML file for the scene
     */
    public static void customer(ActionEvent actionEvent, Customer... customer) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppEntry.class.getResource("customer.fxml"));
        Parent root = loader.load();

        CustomerController customerController = loader.getController();
        customerController.configureCustomerForm(customer);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
    }

    /**
     * This method exits the application.
     */
    public static void exit() {
        Platform.exit();
    }
}

















