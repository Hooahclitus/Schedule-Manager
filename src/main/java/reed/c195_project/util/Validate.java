package reed.c195_project.util;

import javafx.beans.InvalidationListener;
import javafx.scene.control.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Validate {
    public static boolean user(TextField userName, TextField password) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE User_Name = ? AND Password = ?";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, userName.getText());
        ps.setString(2, password.getText());
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public static void customerInputs(Map<TextField, Integer> fields, List<ComboBox<String>> comboStrings, Button btn) {
        InvalidationListener inputValidation = observable -> {
            boolean areFieldsEmpty = fields.keySet().stream()
                    .map(TextField::getText)
                    .anyMatch(String::isEmpty);

            boolean areFieldsWithinLimit = fields.entrySet().stream()
                    .allMatch(entry -> entry.getKey().getLength() <= entry.getValue());

            boolean areComboBoxesEmpty = comboStrings.stream()
                    .map(ComboBox::getSelectionModel)
                    .noneMatch(SelectionModel::isEmpty);

            fields.forEach((fld, lim) -> {
                switch (fld.getLength() > lim ? "red" : "black") {
                    case "red" -> fld.setStyle("-fx-text-fill: red;");
                    case "black" -> fld.setStyle("-fx-text-fill: black;");
                }
            });

            btn.setDisable(areFieldsEmpty || !areFieldsWithinLimit || !areComboBoxesEmpty);
        };
        fields.keySet().forEach(field -> field.textProperty().addListener(inputValidation));
        comboStrings.forEach(comboBox -> comboBox.getSelectionModel().selectedItemProperty().addListener(inputValidation));
    }

    public static void appointmentInputs(Map<TextField, Integer> fields, List<ComboBox<String>> comboStrings,
                                         List<DatePicker> datePickers, List<ComboBox<Integer>> comboTimes, Button btn) {
        InvalidationListener inputValidation = observable -> {
            boolean areFieldsEmpty = fields.keySet().stream()
                    .map(TextField::getText)
                    .anyMatch(String::isEmpty);

            boolean areFieldsWithinLimit = fields.entrySet().stream()
                    .allMatch(entry -> entry.getKey().getLength() <= entry.getValue());

            boolean areComboBoxesEmpty = comboStrings.stream()
                    .map(ComboBox::getSelectionModel)
                    .noneMatch(SelectionModel::isEmpty);

            boolean areDatePickersEmpty = datePickers.stream()
                    .map(DatePicker::getValue)
                    .noneMatch(Objects::isNull);

            boolean areComboTimesEmpty = comboTimes.stream()
                    .map(ComboBox::getSelectionModel)
                    .noneMatch(SelectionModel::isEmpty);

            boolean isStartDateBeforeCurrentDate = datePickers.stream()
                    .findFirst()
                    .map(DatePicker::getValue)
                    .map(startDate -> startDate.isBefore(LocalDate.now()))
                    .orElse(false);

            boolean isStartDateAfterEndDate = datePickers.stream()
                    .filter(dp -> dp.getId().equals("datePickerEnd"))
                    .findFirst()
                    .map(DatePicker::getValue)
                    .flatMap(endDate -> datePickers.stream()
                            .findFirst()
                            .map(DatePicker::getValue)
                            .map(endDate::isBefore))
                    .orElse(false);

            fields.forEach((fld, lim) -> {
                switch (fld.getLength() > lim ? "red" : "black") {
                    case "red" -> fld.setStyle("-fx-text-fill: red;");
                    case "black" -> fld.setStyle("-fx-text-fill: black;");
                }
            });

            if (isStartDateBeforeCurrentDate) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Date Selection");
                alert.setHeaderText("Start Date is Before Current Date");
                alert.setContentText("Please select a later Start Date.");
                alert.show();
            }

            if (isStartDateAfterEndDate) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Date Selection");
                alert.setHeaderText("End Date is Before Start Date");
                alert.setContentText("Please select a later End Date or an earlier Start Date.");
                alert.show();
            }

            btn.setDisable(areFieldsEmpty || !areFieldsWithinLimit || !areComboBoxesEmpty || !areDatePickersEmpty || !areComboTimesEmpty
                    || isStartDateBeforeCurrentDate || isStartDateAfterEndDate);
        };

        fields.keySet().forEach(field -> field.textProperty().addListener(inputValidation));
        datePickers.forEach(datePicker -> datePicker.valueProperty().addListener(inputValidation));
        comboTimes.forEach(comboTime -> comboTime.getSelectionModel().selectedItemProperty().addListener(inputValidation));
        comboStrings.forEach(comboBox -> comboBox.getSelectionModel().selectedItemProperty().addListener(inputValidation));
    }
}
