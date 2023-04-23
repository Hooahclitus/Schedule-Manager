package reed.c195_project.utils;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import reed.c195_project.model.Appointment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Validate {
    public static boolean userCredentials(TextField userName, TextField password) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE User_Name = ? AND Password = ?";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, userName.getText());
        ps.setString(2, password.getText());
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    private static boolean appointmentTime(LocalDateTime localDateTime) {
        var zonedTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("America/New_York"))
                .toLocalTime();

        var businessStart = LocalTime.of(8, 0);
        var businessEnd = LocalTime.of(22, 0);

        return zonedTime.isAfter(businessStart) && zonedTime.isBefore(businessEnd);
    }

    public static boolean isAppointmentWithinBusinessHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return Validate.appointmentTime(startDateTime) && Validate.appointmentTime(endDateTime);
    }

    public static List<Appointment> areAppointmentsWithin15Minutes(ObservableList<Appointment> appointments) {
        return appointments.stream().filter(appointment -> appointment.start().isAfter(LocalDateTime.now())
                && appointment.start().isBefore(LocalDateTime.now().plusMinutes(15))).toList();
    }

    public static List<Appointment> areAppointmentsOverlapping(ObservableList<Appointment> appointments, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return appointments.stream().filter(e -> e.start().isBefore(endDateTime) && e.end().isAfter(startDateTime)).toList();
    }

    private static boolean areTextFieldsValid(Map<TextField, Integer> textFields) {
        boolean areFieldsEmpty = textFields.keySet().stream()
                .map(TextField::getText)
                .anyMatch(String::isEmpty);

        boolean areFieldsWithinLimit = textFields.entrySet().stream()
                .allMatch(entry -> entry.getKey().getLength() <= entry.getValue());

        return areFieldsEmpty || !areFieldsWithinLimit;
    }

    private static <T> boolean areComboBoxesValid(List<ComboBox<T>> comboBoxes) {
        return comboBoxes.stream()
                .map(ComboBox::getSelectionModel)
                .anyMatch(SelectionModel::isEmpty);
    }

    private static boolean areDatePickersValid(List<DatePicker> datePickers) {
        return datePickers.stream()
                .map(DatePicker::getValue)
                .anyMatch(Objects::isNull);
    }

    private static void changeTextFieldColorIfLimitExceeded(Map<TextField, Integer> fields) {
        fields.forEach((fld, lim) -> fld.setStyle("-fx-text-fill: " + (fld.getLength() > lim ? "red" : "black") + ";"));

    }

    public static void customerInputs(Map<TextField, Integer> fieldsAndLimits, List<ComboBox<Object>> combos,
                                      Button btn) {
        InvalidationListener inputValidation = observable -> {
            changeTextFieldColorIfLimitExceeded(fieldsAndLimits);

            btn.setDisable(areTextFieldsValid(fieldsAndLimits) || areComboBoxesValid(combos));
        };

        fieldsAndLimits.keySet().forEach(e -> e.textProperty().addListener(inputValidation));
        combos.forEach(e -> e.getSelectionModel().selectedItemProperty().addListener(inputValidation));
    }

    public static void appointmentInputs(Map<TextField, Integer> fieldsAndLimits, List<ComboBox<Object>> combos,
                                         List<DatePicker> dates, Button btn) {
        InvalidationListener inputValidation = observable -> {
            changeTextFieldColorIfLimitExceeded(fieldsAndLimits);

            btn.setDisable(areTextFieldsValid(fieldsAndLimits) || areComboBoxesValid(combos) || areDatePickersValid(dates));
        };

        fieldsAndLimits.keySet().forEach(e -> e.textProperty().addListener(inputValidation));
        combos.forEach(e -> e.getSelectionModel().selectedItemProperty().addListener(inputValidation));
        dates.forEach(e -> e.valueProperty().addListener(inputValidation));
    }
}
