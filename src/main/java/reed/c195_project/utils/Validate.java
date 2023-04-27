package reed.c195_project.utils;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import reed.c195_project.model.Appointment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * A class containing static methods for validating user inputs.
 */
public abstract class Validate {
    /**
     * Validates user credentials by checking the given username and password against the database.
     *
     * @param userName the TextField containing the user's username
     * @param password the TextField containing the user's password
     * @return true if the username and password match an entry in the database, false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean userCredentials(TextField userName, TextField password) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE User_Name = ? AND Password = ?";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, userName.getText());
        ps.setString(2, password.getText());
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    /**
     * Validates an appointment time by checking if it falls within business hours.
     *
     * @param localDateTime the LocalDateTime object representing the appointment time
     * @return true if the appointment time falls within business hours, false otherwise
     */
    public static boolean appointmentTime(LocalDateTime localDateTime) {
        var zonedTime = localDateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("America/New_York"))
                .toLocalTime();

        var businessStart = LocalTime.of(8, 0);
        var businessEnd = LocalTime.of(22, 0);

        return zonedTime.isAfter(businessStart.minusMinutes(1)) && zonedTime.isBefore(businessEnd.plusMinutes(1));
    }

    /**
     * Finds appointments that start within the next 15 minutes.
     *
     * @param appointments the list of appointments to check
     * @return a List of Appointment objects that start within the next 15 minutes
     */
    public static List<Appointment> areAppointmentsWithin15Minutes(ObservableList<Appointment> appointments) {
        return appointments.stream().filter(appointment -> appointment.start().isAfter(LocalTime.now())
                && appointment.start().isBefore(LocalTime.now().plusMinutes(15))).toList();
    }

    /**
     * Finds appointments that overlap with a given time period and contact, including the appointment
     * with a given ID (if provided).
     *
     * @param appointments the list of appointments to check
     * @param contacts the ComboBox containing the contact associated with the appointments
     * @param start the start time of the time period to check for overlapping appointments
     * @param end the end time of the time period to check for overlapping appointments
     * @param appointmentID the optional TextField containing the ID of the appointment to include from the results
     * @return a List of Appointment objects that overlap with the given time period and contact, including the appointment with the given ID (if provided)
     */
    public static List<Appointment> areAppointmentsOverlapping(List<Appointment> appointments,
                                                               ComboBox<Object> contacts,
                                                               LocalDateTime start, LocalDateTime end,
                                                               TextField... appointmentID) {
        var appointmentStream = appointments.stream()
                .filter(e -> e.contact().equals(contacts.getValue()))
                .filter(e -> e.start().isBefore(end.toLocalTime()) && e.end().isAfter(start.toLocalTime()));

        return appointmentID.length == 0
                ? appointmentStream.toList()
                : appointmentStream.filter(e -> e.appointmentID() != Integer.parseInt(appointmentID[0].getText())).toList();
    }


    /**
     * Checks if a Map of TextFields and their character limits contains any empty or excessively long TextFields.
     *
     * @param textFields the Map of TextFields and their character limits to check
     * @return true if any TextField is empty or exceeds its character limit, false otherwise
     */
    private static boolean areTextFieldsValid(Map<TextField, Integer> textFields) {
        boolean areFieldsEmpty = textFields.keySet().stream()
                .map(TextField::getText)
                .anyMatch(String::isEmpty);

        boolean areFieldsWithinLimit = textFields.entrySet().stream()
                .allMatch(entry -> entry.getKey().getLength() <= entry.getValue());

        return areFieldsEmpty || !areFieldsWithinLimit;
    }

    /**
     * Checks if a List of ComboBoxes has any empty selection models.
     *
     * @param comboBoxes the List of ComboBoxes to check
     * @return true if any ComboBox has an empty selection model, false otherwise
     */
    private static <T> boolean areComboBoxesValid(List<ComboBox<T>> comboBoxes) {
        return comboBoxes.stream()
                .map(ComboBox::getSelectionModel)
                .anyMatch(SelectionModel::isEmpty);
    }

    /**
     * Checks if a DatePicker has a valid value (i.e., not null).
     *
     * @param datePicker the DatePicker to check
     * @return true if the DatePicker has a null value, false otherwise
     */
    private static boolean isDatePickerValid(DatePicker datePicker) {
        return datePicker.getValue() == null;
    }

    /**
     * Changes the text color of any TextField in a Map that exceeds its character limit to red.
     *
     * @param fields the Map of TextFields and their character limits to check and update
     */
    private static void changeTextFieldColorIfLimitExceeded(Map<TextField, Integer> fields) {
        fields.forEach((fld, lim) -> fld.setStyle("-fx-text-fill: " + (fld.getLength() > lim ? "red" : "black") + ";"));

    }

    /**
     * Sets up input validation for customer data fields and combo boxes.
     *
     * @param fieldsAndLimits A Map containing a TextField object and its limit as an Integer.
     * @param combos A List of ComboBoxes containing customer data.
     * @param btn The button that will be disabled if the input validation fails.
     */
    public static void customerInputs(Map<TextField, Integer> fieldsAndLimits, List<ComboBox<Object>> combos,
                                      Button btn) {
        InvalidationListener inputValidation = observable -> {
            changeTextFieldColorIfLimitExceeded(fieldsAndLimits);

            btn.setDisable(areTextFieldsValid(fieldsAndLimits) || areComboBoxesValid(combos));
        };

        fieldsAndLimits.keySet().forEach(e -> e.textProperty().addListener(inputValidation));
        combos.forEach(e -> e.getSelectionModel().selectedItemProperty().addListener(inputValidation));
    }

    /**
     * Sets up input validation for appointment data fields, combo boxes, and date picker.
     *
     * @param fieldsAndLimits A Map containing a TextField object and its limit as an Integer.
     * @param combos A List of ComboBoxes containing appointment data.
     * @param date A DatePicker object containing the appointment date.
     * @param btn The button that will be disabled if the input validation fails.
     */
    public static void appointmentInputs(Map<TextField, Integer> fieldsAndLimits, List<ComboBox<Object>> combos,
                                         DatePicker date, Button btn) {
        InvalidationListener inputValidation = observable -> {
            changeTextFieldColorIfLimitExceeded(fieldsAndLimits);

            btn.setDisable(areTextFieldsValid(fieldsAndLimits) || areComboBoxesValid(combos) || isDatePickerValid(date));
        };

        fieldsAndLimits.keySet().forEach(e -> e.textProperty().addListener(inputValidation));
        combos.forEach(e -> e.getSelectionModel().selectedItemProperty().addListener(inputValidation));
        date.valueProperty().addListener(inputValidation);
    }
}
