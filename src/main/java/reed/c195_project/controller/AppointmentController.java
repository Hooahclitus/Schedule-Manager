package reed.c195_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import reed.c195_project.model.Appointment;
import reed.c195_project.util.JDBC;
import reed.c195_project.util.LoadScene;
import reed.c195_project.util.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static javafx.collections.FXCollections.observableList;

public class AppointmentController implements Initializable {
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

    private LocalDateTime toLocalDateTime(DatePicker date, ComboBox<Object> hour, ComboBox<Object> minute) {
        return LocalDateTime.of(date.getValue(),
                LocalTime.of((Integer) hour.getSelectionModel().getSelectedItem(),
                        (Integer) minute.getSelectionModel().getSelectedItem()));
    }

    private void insertAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        var appointmentData = Map.of(
                1, title.getText(),
                2, description.getText(),
                3, location.getText(),
                4, type.getText(),
                5, Timestamp.valueOf(toLocalDateTime(startDate, startHour, startMinute)),
                6, Timestamp.valueOf(toLocalDateTime(endDate, endHour, endMinute)),
                7, customerID.getSelectionModel().getSelectedItem(),
                8, userID.getSelectionModel().getSelectedItem(),
                9, contacts.getSelectionModel().getSelectedItem()
        );

        var sql = "INSERT INTO appointments (Title, Description, Location, Type, Start, End, Customer_ID, User_ID, " +
                "Contact_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT Contact_ID FROM contacts WHERE Contact_Name = ?))";

        JDBC.updateTable(sql, appointmentData);
        LoadScene.schedule(actionEvent);
    }

    private void updateAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        var appointmentData = Map.of(
                1, title.getText(),
                2, description.getText(),
                3, location.getText(),
                4, type.getText(),
                5, Timestamp.valueOf(toLocalDateTime(startDate, startHour, startMinute)),
                6, Timestamp.valueOf(toLocalDateTime(endDate, endHour, endMinute)),
                7, customerID.getSelectionModel().getSelectedItem(),
                8, userID.getSelectionModel().getSelectedItem(),
                9, contacts.getSelectionModel().getSelectedItem(),
                10, appointmentID.getText()
        );

        var sql = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = ?, Start = ?, End = ?, " +
                "Customer_ID = ?, User_ID = ?, " +
                "Contact_ID = (SELECT Contact_ID FROM contacts WHERE Contact_Name = ?) WHERE Appointment_ID = ?";

        JDBC.updateTable(sql, appointmentData);
        LoadScene.schedule(actionEvent);
    }

    @FXML
    private void submitAppointmentData(ActionEvent actionEvent) throws SQLException, IOException {
        if (submit.getText().equals("Update")) {
            updateAppointment(actionEvent);
        } else {
            insertAppointment(actionEvent);
        }
    }

    public void setupAppointmentForm(Appointment... appointment) {
        switch (appointment.length) {
            case 0 -> {
                submit.setText("Add");
                appointmentID.setText("Auto-Generated");
            }
            case 1 -> {
                submit.setText("Update");
                loadAppointmentData(appointment[0]);
            }
        }
    }

    public void loadAppointmentData(Appointment appointment) {
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
                startHour, appointment.startTime().getHour(),
                startMinute, appointment.startTime().getMinute(),
                endHour, appointment.endTime().getHour(),
                endMinute, appointment.endTime().getMinute()
        );

        var dates = Map.of(
                startDate, appointment.startDate(),
                endDate, appointment.endDate()
        );

        fields.forEach(TextInputControl::setText);
        combos.forEach(ComboBoxBase::setValue);
        dates.forEach(ComboBoxBase::setValue);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        LoadScene.schedule(actionEvent);
    }
}

















