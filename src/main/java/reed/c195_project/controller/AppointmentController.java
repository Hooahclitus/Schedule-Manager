package reed.c195_project.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import reed.c195_project.model.Appointment;
import reed.c195_project.utils.JDBC;
import reed.c195_project.utils.LoadScene;
import reed.c195_project.utils.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static javafx.collections.FXCollections.observableList;

public class AppointmentController implements Initializable {
    private ObservableList<Appointment> appointments;

    @FXML
    private DatePicker startDate, endDate;

    @FXML
    private TextField appointmentID, title, description, location, type;

    @FXML
    private ComboBox<Object> contacts, customerID, userID, startHour, startMinute, endHour, endMinute;

    @FXML
    private Button submit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stream.of(startHour, endHour).forEach(e -> e.setItems(observableList(rangeClosed(0, 23).boxed().collect(toList()))));
        Stream.of(startMinute, endMinute).forEach(e -> e.setItems(observableList(rangeClosed(0, 59).boxed().collect(toList()))));

        contacts.setItems(JDBC.selectFieldData("SELECT Contact_Name FROM contacts ORDER BY Contact_Name"));
        customerID.setItems(JDBC.selectFieldData("SELECT Customer_ID FROM customers ORDER BY Customer_ID"));
        userID.setItems(JDBC.selectFieldData("SELECT User_ID FROM users ORDER BY User_ID"));

        var fieldsAndLimits = Map.of(title, 50, description, 50, location, 50, type, 50);
        var combos = List.of(contacts, customerID, userID, startHour, startMinute, endHour, endMinute);
        var dates = List.of(startDate, endDate);

        Validate.appointmentInputs(fieldsAndLimits, combos, dates, submit);
    }

    private LocalDateTime convertToLocalDateTime(DatePicker date, ComboBox<Object> hour, ComboBox<Object> minute) {
        return LocalDateTime.of(date.getValue(),
                LocalTime.of((Integer) hour.getSelectionModel().getSelectedItem(),
                        (Integer) minute.getSelectionModel().getSelectedItem()));
    }

    private void businessHoursAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Business Hours Alert");
        alert.setHeaderText("Appointment Time Outside of Business Hours");
        alert.setContentText("The selected appointment time falls outside our business hours, which are from 8:00" +
                " AM to 10:00 PM Eastern Standard Time");
        alert.showAndWait();
    }

    private void conflictingAppointmentsAlert(List<Appointment> appointments) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        List<String> conflictingAppointments = appointments.stream()
                .map(appointment -> String.format("Appointment ID: %d, Date: %s, Time: %s - %s",
                        appointment.appointmentID(),
                        appointment.start().format(dateFormat),
                        appointment.start().format(timeFormat),
                        appointment.end().format(timeFormat)))
                .toList();

        String appointmentDetails = String.join("\n", conflictingAppointments);

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Scheduling Conflict");
        alert.setHeaderText("Unable to Schedule Appointment");
        alert.setContentText("The appointment time conflicts with the following:\n\n"
                + appointmentDetails + "\n\nPlease select a different time slot.");
        alert.showAndWait();
    }

    public void passAppointments(ObservableList<Appointment> appointments) {
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

        var dates = Map.of(
                startDate, appointment.start().toLocalDate(),
                endDate, appointment.end().toLocalDate()
        );

        fields.forEach(TextInputControl::setText);
        combos.forEach(ComboBoxBase::setValue);
        dates.forEach(ComboBoxBase::setValue);
    }

    @FXML
    private void insertOrUpdateAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        final var INSERT_APPOINTMENT_SQL = "INSERT INTO appointments (Title, Description, Location, Type, Start, End," +
                " Customer_ID, User_ID, Contact_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT Contact_ID FROM contacts " +
                "WHERE Contact_Name = ?))";

        final var UPDATE_APPOINTMENT_SQL = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = " +
                "?, Start = ?, End = ?, Customer_ID = ?, User_ID = ?, Contact_ID = (SELECT Contact_ID FROM contacts " +
                "WHERE Contact_Name = ?) WHERE Appointment_ID = ?";

        var sql = submit.getText().equals("Update") ? UPDATE_APPOINTMENT_SQL : INSERT_APPOINTMENT_SQL;

        var startDateTime = convertToLocalDateTime(startDate, startHour, startMinute);
        var endDateTime = convertToLocalDateTime(endDate, endHour, endMinute);

        if (!Validate.isAppointmentWithinBusinessHours(startDateTime, endDateTime)) {
            businessHoursAlert();
            return;
        }

        var conflictingAppointments = Validate.isAppointmentOverlapping(appointments, startDateTime, endDateTime);

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

        JDBC.updateTable(sql, appointmentData);
        LoadScene.schedule(actionEvent);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        LoadScene.schedule(actionEvent);
    }
}
