package reed.c195_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import reed.c195_project.model.Appointment;
import reed.c195_project.utils.DateTime;
import reed.c195_project.utils.JDBC;
import reed.c195_project.utils.LoadScene;
import reed.c195_project.utils.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static javafx.collections.FXCollections.observableList;

public class AppointmentController implements Initializable {
    private List<Appointment> appointments;

    @FXML
    private DatePicker date;

    @FXML
    private TextField appointmentID, title, description, location, type;

    @FXML
    private ComboBox<Object> contacts, customerID, userID, startHour, startMinute, endHour, endMinute;

    @FXML
    private Button submit;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stream.of(startMinute, endMinute).forEach(e -> e.setItems(observableList(rangeClosed(0, 59).boxed().collect(toList()))));
        Stream.of(startHour, endHour).forEach(e -> e.setItems(observableList(rangeClosed(0, 23)
                .mapToObj(i -> LocalDateTime.of(LocalDate.now(), LocalTime.of(i, 0)))
                .filter(Validate::appointmentTime)
                .map(LocalDateTime::getHour)
                .collect(toList()))));

        contacts.setItems(JDBC.selectFieldData("SELECT Contact_Name FROM contacts ORDER BY Contact_Name"));
        customerID.setItems(JDBC.selectFieldData("SELECT Customer_ID FROM customers ORDER BY Customer_ID"));
        userID.setItems(JDBC.selectFieldData("SELECT User_ID FROM users ORDER BY User_ID"));

        var fieldsAndLimits = Map.of(title, 50, description, 50, location, 50, type, 50);
        var combos = List.of(contacts, customerID, userID, startHour, startMinute, endHour, endMinute);

        Validate.appointmentInputs(fieldsAndLimits, combos, date, submit);
    }

    private void conflictingAppointmentsAlert(List<Appointment> appointments) {
        List<String> conflictingAppointments = appointments.stream()
                .map(appointment -> String.format("Appointment ID: %d\n\tDate: %s - Time: %s - %s\n",
                        appointment.appointmentID(),
                        appointment.date().format(DateTime.dateFormat),
                        appointment.start().format(DateTime.timeFormat),
                        appointment.end().format(DateTime.timeFormat)))
                .toList();

        String appointmentDetails = String.join("\n", conflictingAppointments);

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Scheduling Conflict");
        alert.setHeaderText("Unable to Schedule Appointment");
        alert.setContentText("The appointment time conflicts with the following:\n\n"
                + appointmentDetails + "\n\nPlease select a different time slot.");
        alert.showAndWait();
    }

    public void passAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public void configureAppointmentForm(Appointment... appointment) {
        switch (appointment.length) {
            case 0 -> {
                submit.setText("Add");
                appointmentID.setText("Auto-Generated");
            }
            case 1 -> {
                submit.setText("Update");
                populateFormWithAppointmentData(appointment[0]);
            }
        }
    }

    public void populateFormWithAppointmentData(Appointment appointment) {
        var fields = Map.of(
                appointmentID, String.valueOf(appointment.appointmentID()),
                title, appointment.title(),
                description, appointment.description(),
                location, appointment.location(),
                type, appointment.type()
        );

        var combos = Map.of(
                contacts, appointment.contact(),
                customerID, appointment.customerID(),
                userID, appointment.userID(),
                startHour, appointment.start().getHour(),
                startMinute, appointment.start().getMinute(),
                endHour, appointment.end().getHour(),
                endMinute, appointment.end().getMinute()
        );

        fields.forEach(TextInputControl::setText);
        combos.forEach(ComboBoxBase::setValue);
        date.setValue(appointment.date());
    }

    @FXML
    private void insertOrUpdateAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        var startDateTime = DateTime.toLocalDateTime(date, startHour, startMinute);
        var endDateTime = DateTime.toLocalDateTime(date, endHour, endMinute);

        var conflictingAppointments = submit.getText().equals("Update")
                ? Validate.areAppointmentsOverlapping(appointments, contacts, startDateTime, endDateTime, appointmentID)
                : Validate.areAppointmentsOverlapping(appointments, contacts, startDateTime, endDateTime);

        if (!conflictingAppointments.isEmpty()) {
            conflictingAppointmentsAlert(conflictingAppointments);
            return;
        }

        Map<Integer, Object> appointmentData = new HashMap<>();
        appointmentData.put(1, title.getText());
        appointmentData.put(2, description.getText());
        appointmentData.put(3, location.getText());
        appointmentData.put(4, type.getText());
        appointmentData.put(5, Timestamp.valueOf(startDateTime));
        appointmentData.put(6, Timestamp.valueOf(endDateTime));
        appointmentData.put(7, customerID.getValue());
        appointmentData.put(8, userID.getValue());
        appointmentData.put(9, contacts.getValue());

        if (submit.getText().equals("Update")) {
            appointmentData.put(10, appointmentID.getText());
        }

        JDBC.updateAppointmentsTable(submit, appointmentData);
        LoadScene.schedule(actionEvent, false);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        LoadScene.schedule(actionEvent, false);
    }
}
