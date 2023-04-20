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
    public static boolean user(TextField userName, TextField password) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE User_Name = ? AND Password = ?";
        PreparedStatement ps = JDBC.connection.prepareStatement(sql);
        ps.setString(1, userName.getText());
        ps.setString(2, password.getText());
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public static void customerInputs(Map<TextField, Integer> map, List<ComboBox<Object>> list, Button btn) {
        InvalidationListener inputValidation = observable -> {
            boolean areFieldsEmpty = map.keySet().stream()
                    .map(TextField::getText)
                    .anyMatch(String::isEmpty);

            boolean areFieldsWithinLimit = map.entrySet().stream()
                    .allMatch(entry -> entry.getKey().getLength() <= entry.getValue());

            boolean areComboBoxesValid = list.stream()
                    .map(ComboBox::getSelectionModel)
                    .noneMatch(SelectionModel::isEmpty);

            map.forEach((fld, lim) -> {
                switch (fld.getLength() > lim ? "red" : "black") {
                    case "red" -> fld.setStyle("-fx-text-fill: red;");
                    case "black" -> fld.setStyle("-fx-text-fill: black;");
                }
            });

            btn.setDisable(areFieldsEmpty || !areFieldsWithinLimit || !areComboBoxesValid);
        };
        map.keySet().forEach(field -> field.textProperty().addListener(inputValidation));
        list.forEach(comboBox -> comboBox.getSelectionModel().selectedItemProperty().addListener(inputValidation));
    }

    public static void appointmentInputs(Map<TextField, Integer> fields, List<ComboBox<Object>> combos,
                                         List<DatePicker> datePickers, Button btn) {
        InvalidationListener inputValidation = observable -> {
            boolean areFieldsEmpty = fields.keySet().stream()
                    .map(TextField::getText)
                    .anyMatch(String::isEmpty);

            boolean areFieldsWithinLimit = fields.entrySet().stream()
                    .allMatch(entry -> entry.getKey().getLength() <= entry.getValue());

            boolean areComboBoxesValid = combos.stream()
                    .map(ComboBox::getSelectionModel)
                    .noneMatch(SelectionModel::isEmpty);

            boolean areDatePickersValid = datePickers.stream()
                    .map(DatePicker::getValue)
                    .noneMatch(Objects::isNull);

            fields.forEach((fld, lim) -> {
                switch (fld.getLength() > lim ? "red" : "black") {
                    case "red" -> fld.setStyle("-fx-text-fill: red;");
                    case "black" -> fld.setStyle("-fx-text-fill: black;");
                }
            });

            btn.setDisable(areFieldsEmpty || !areFieldsWithinLimit || !areComboBoxesValid || !areDatePickersValid);
        };
        fields.keySet().forEach(field -> field.textProperty().addListener(inputValidation));
        datePickers.forEach(datePicker -> datePicker.valueProperty().addListener(inputValidation));
        combos.forEach(comboBox -> comboBox.getSelectionModel().selectedItemProperty().addListener(inputValidation));
    }
}
