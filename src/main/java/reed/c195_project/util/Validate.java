package reed.c195_project.util;

import javafx.beans.InvalidationListener;
import javafx.scene.control.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
