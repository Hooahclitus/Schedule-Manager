package reed.c195_project.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import reed.c195_project.model.Appointment;
import reed.c195_project.util.JDBC;
import reed.c195_project.util.SQL;
import reed.c195_project.util.SceneManager;
import reed.c195_project.util.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static javafx.collections.FXCollections.observableList;

public class AppointmentController implements Initializable {
    @FXML
    private DatePicker datePickerStart, datePickerEnd;

    @FXML
    private TextField fldAppointmentID, fldTitle, fldDescription, fldLocation, fldType, fldCustomerID, fldUserID;

    @FXML
    private ComboBox<Object> comboContacts, comboStartHour, comboStartMinute, comboEndHour, comboEndMinute;

    @FXML
    private Button btnAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stream.of(comboStartHour, comboEndHour).forEach(e -> e.setItems(observableList(rangeClosed(0, 24).boxed().collect(toList()))));
        Stream.of(comboStartMinute, comboEndMinute).forEach(e -> e.setItems(observableList(rangeClosed(0, 59).boxed().collect(toList()))));

        comboContacts.setItems(SQL.selectColumnData("SELECT Contact_Name FROM contacts", "Contact_Name"));

        var fieldsMap = Map.of(
                fldTitle, 50,
                fldDescription, 50,
                fldLocation, 50,
                fldType, 50
        );

        var combosList = List.of(comboContacts, comboStartHour, comboStartMinute, comboEndHour, comboEndMinute);
        var datePickersList = List.of(datePickerStart, datePickerEnd);

        Validate.appointmentInputs(fieldsMap, combosList, datePickersList, btnAction);
    }

    private String getDateAndTime(DatePicker date, ComboBox<Object> hour, ComboBox<Object> minute) {
        return String.format("%s %s:%s:00",
                date.getValue(),
                hour.getSelectionModel().getSelectedItem(),
                minute.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void insertAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        var appointmentDataMap = Map.of(
                1, fldTitle.getText(),
                2, fldDescription.getText(),
                3, fldLocation.getText(),
                4, fldType.getText(),
                5, getDateAndTime(datePickerStart, comboStartHour, comboStartMinute),
                6, getDateAndTime(datePickerEnd, comboEndHour, comboEndMinute),
                7, Integer.parseInt(fldCustomerID.getText()),
                8, Integer.parseInt(fldUserID.getText()),
                9, comboContacts.getSelectionModel().getSelectedItem()
        );

        var sql = "INSERT INTO appointments (Title, Description, Location, Type, Start, End, Customer_ID, " +
                "User_ID, " +
                "Contact_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT Contact_ID FROM contacts WHERE Contact_Name = ?))";

        SQL.updateTableData(sql, appointmentDataMap);
        SceneManager.loadScheduleScene(actionEvent);
    }

    @FXML
    private void updateAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        var appointmentDataMap = Map.of(
                1, fldTitle.getText(),
                2, fldDescription.getText(),
                3, fldLocation.getText(),
                4, fldType.getText(),
                5, getDateAndTime(datePickerStart, comboStartHour, comboStartMinute),
                6, getDateAndTime(datePickerEnd, comboEndHour, comboEndMinute),
                7, Integer.parseInt(fldCustomerID.getText()),
                8, Integer.parseInt(fldUserID.getText()),
                9, comboContacts.getSelectionModel().getSelectedItem(),
                10, fldAppointmentID.getText()
        );

        var sql = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = ?, " +
                "Start = ?, End = ?, Customer_ID = ?, User_ID = ?, " +
                "Contact_ID = (SELECT Contact_ID FROM contacts WHERE Contact_Name = ?) " +
                "WHERE Appointment_ID = ?";

        SQL.updateTableData(sql, appointmentDataMap);
        SceneManager.loadScheduleScene(actionEvent);
    }

    public void populateAppointmentFields(Appointment appointment) {
        var datePickerMap = Map.of(
                datePickerStart, appointment.startDate(),
                datePickerEnd, appointment.endDate()
        );

        var comboBoxMap = Map.of(
                comboContacts, appointment.contact(),
                comboStartHour, appointment.getHour(),
                comboStartMinute, appointment.getMinute(),
                comboEndHour, appointment.getHour(),
                comboEndMinute, appointment.getMinute()
        );

        var textFieldMap = Map.of(
                fldAppointmentID, String.valueOf(appointment.appointmentID()),
                fldTitle, appointment.title(),
                fldDescription, appointment.description(),
                fldLocation, appointment.location(),
                fldType, appointment.type(),
                fldCustomerID, String.valueOf(appointment.customerID()),
                fldUserID, String.valueOf(appointment.userID())
        );

        datePickerMap.forEach(ComboBoxBase::setValue);
        comboBoxMap.forEach(ComboBoxBase::setValue);
        textFieldMap.forEach(TextInputControl::setText);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        SceneManager.loadScheduleScene(actionEvent);
    }
}

















