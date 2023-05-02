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
     *
     * @return true if the username and password match an entry in the database, false otherwise
     *
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
     *
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
     * Returns a List of Appointment objects that are scheduled within the next 15 minutes, based on the current time.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: A lambda expression is used in the filter() method to check if the start date and
     * time of each appointment is after the current date and time and before 15 minutes from now. Using a lambda
     * expression in this case improves code readability and conciseness, as it allows us to define the filtering
     * condition in a clear and concise way, without having to create a separate method for it.
     *
     * @param appointments the list of appointments to filter
     *
     * @return a List of Appointment objects that are scheduled within the next 15 minutes, based on the current time
     */
    public static List<Appointment> areAppointmentsWithin15Minutes(ObservableList<Appointment> appointments) {
        return appointments.stream().filter(appointment -> {
            var dateTime = DateTime.toLocalDateTime(appointment.date(), appointment.start());

            return dateTime.isAfter(LocalDateTime.now()) && dateTime.isBefore(LocalDateTime.now().plusMinutes(15));
        }).toList();
    }

    /**
     * Finds appointments that overlap with a given time period and customerID, including the appointment
     * with a given ID (if provided).
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: A lambda expression is used to filter the stream of appointments based on whether
     * they overlap with the given start and end times. It allows for a more concise and readable code, and reduces
     * the amount of boilerplate code required.
     *
     * @param appointments  the list of appointments to check
     * @param customerID    the ComboBox containing the customerID associated with the appointments
     * @param start         the start time of the time period to check for overlapping appointments
     * @param end           the end time of the time period to check for overlapping appointments
     * @param appointmentID the optional TextField containing the ID of the appointment to include from the results
     *
     * @return a List of Appointment objects that overlap with the given time period and contact, including the
     * appointment with the given ID (if provided)
     */
    public static List<Appointment> areAppointmentsOverlapping(List<Appointment> appointments,
                                                               ComboBox<Object> customerID,
                                                               LocalDateTime start, LocalDateTime end,
                                                               TextField... appointmentID) {

        var appointmentStream = appointments.stream()
                .filter(appointment -> appointment.customerID().equals(customerID.getValue())
                        && appointment.date().isEqual(start.toLocalDate())
                        && appointment.start().isBefore(end.toLocalTime())
                        && appointment.end().isAfter(start.toLocalTime()));


        return appointmentID.length == 0
                ? appointmentStream.toList()
                : appointmentStream.filter(e -> e.appointmentID() != Integer.parseInt(appointmentID[0].getText())).toList();
    }

    /**
     * Checks if the text fields in the given Map are valid.
     * <p>
     * <b>LAMBDA JUSTIFICATION:</b> Lambdas are used in this method to simplify the code and make it more concise.
     * The first lambda expression is used to check if any of the text fields are empty, while the second lambda
     * expression is used to check if all the text fields are within their specified length limit. Using lambdas
     * allows us to avoid writing explicit loops and conditionals, and makes the code more readable and maintainable.
     *
     * @param textFields a Map of TextFields and their corresponding length limits
     *
     * @return true if the text fields are valid, false otherwise
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
     *
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
     *
     * @return true if the DatePicker has a null value, false otherwise
     */
    private static boolean isDatePickerValid(DatePicker datePicker) {
        return datePicker.getValue() == null;
    }


    /**
     * Checks if the given date and time is before the current date and time.
     *
     * @param dateTime the date and time to be checked
     *
     * @return true if the given date and time is before the current date and time, otherwise false
     */
    public static boolean isAppointmentDateTimeBeforeCurrentDateTime(LocalDateTime dateTime) {
        return dateTime.isBefore(LocalDateTime.now(ZoneId.systemDefault()));
    }

    /**
     * Checks if the end time is before the start time.
     *
     * @param startDateTime the start date and time
     * @param endDateTime   the end date and time
     *
     * @return true if the end date and time is before the start date and time, otherwise false
     */
    public static boolean isEndTimeBeforeStartTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return (endDateTime.toLocalTime().isBefore(startDateTime.toLocalTime()));
    }

    /**
     * Changes the text field color to red if the limit has been exceeded.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: The lambda expression is used within the forEach method to iterate over the
     * Map<TextField, Integer> fields and apply a conditional statement to set the text field color to red if the
     * length of the text in the field exceeds the limit. This lambda expression allows for concise and readable code
     * by avoiding the need to write a separate loop to iterate over the Map and apply the conditional statement.
     *
     * @param fields a Map of TextFields and their respective character limits
     */
    private static void changeTextFieldColorIfLimitExceeded(Map<TextField, Integer> fields) {
        fields.forEach((fld, lim) -> fld.setStyle("-fx-text-fill: " + (fld.getLength() > lim ? "red" : "black") + ";"));

    }

    /**
     * This method takes in a Map of TextFields and their corresponding input limits, a List of ComboBoxes, and a
     * Button. It then adds an InvalidationListener to each TextField and ComboBox to validate user input and disable
     * the Button if any of the inputs are invalid.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: Lambdas are used in this method to create InvalidationListeners for each
     * TextField and ComboBox. This reduces code redundancy and promotes code readability by avoiding the need to
     * create separate InvalidationListener implementations for each TextField and ComboBox.
     *
     * @param fieldsAndLimits a Map of TextFields and their corresponding input limits
     * @param combos          a List of ComboBoxes
     * @param btn             a Button to be disabled if any of the inputs are invalid
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
     * @param combos          A List of ComboBoxes containing appointment data.
     * @param date            A DatePicker object containing the appointment date.
     * @param btn             The button that will be disabled if the input validation fails.
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
