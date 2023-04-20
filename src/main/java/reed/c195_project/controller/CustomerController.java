package reed.c195_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import reed.c195_project.model.Customer;
import reed.c195_project.util.SQL;
import reed.c195_project.util.SceneManager;
import reed.c195_project.util.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CustomerController implements Initializable {
    @FXML
    private TextField fldCustomerID, fldName, fldAddress, fldPostalCode, fldPhoneNumber;

    @FXML
    private ComboBox<Object> comboCountry, comboDivision;

    @FXML
    private Button btnAction;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var fieldsAndLimits = Map.of(
                fldName, 50,
                fldAddress, 100,
                fldPostalCode, 50,
                fldPhoneNumber, 50
        );

        var comboBoxes = List.of(comboCountry, comboDivision);

        comboCountry.setItems(SQL.selectColumnData("SELECT Country FROM countries", "Country"));
        comboCountry.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch ((String) newValue) {
                    case "U.S" -> setDivisionItems(1);
                    case "UK" -> setDivisionItems(2);
                    case "Canada" -> setDivisionItems(3);
                }
            }
        });

        Validate.customerInputs(fieldsAndLimits, comboBoxes, btnAction);
    }

    private void setDivisionItems(int countryId) {
        comboDivision.setDisable(false);
        String query = String.format("SELECT Division FROM first_level_divisions WHERE Country_ID = %d", countryId);
        comboDivision.setItems(SQL.selectColumnData(query, "Division"));
    }

    @FXML
    private void insertCustomer(ActionEvent actionEvent) throws SQLException, IOException {
        Map<Integer, Object> customerDataMap = Map.of(
                1, fldName.getText(),
                2, fldAddress.getText(),
                3, fldPostalCode.getText(),
                4, fldPhoneNumber.getText(),
                5, comboDivision.getSelectionModel().getSelectedItem()
        );

        String sql = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Division_ID) " +
                "VALUES (?, ?, ?, ?, (SELECT Division_ID FROM first_level_divisions WHERE Division = ?))";

        SQL.updateTableData(sql, customerDataMap);
        SceneManager.loadScheduleScene(actionEvent);
    }

    @FXML
    private void updateCustomer(ActionEvent actionEvent) throws SQLException, IOException {
        Map<Integer, Object> customerDataMap = Map.of(
                1, fldName.getText(),
                2, fldAddress.getText(),
                3, fldPostalCode.getText(),
                4, fldPhoneNumber.getText(),
                5, comboDivision.getSelectionModel().getSelectedItem(),
                6, fldCustomerID.getText()
        );

        String sql = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, " +
                "Division_ID = (SELECT Division_ID FROM first_level_divisions WHERE Division = ?) " +
                "WHERE Customer_ID = ?";

        SQL.updateTableData(sql, customerDataMap);
        SceneManager.loadScheduleScene(actionEvent);
    }

    public void populateCustomerFields(Customer customer) {
        Map<ComboBox<Object>, String> comboBoxMap = Map.of(
                comboCountry, customer.country(),
                comboDivision, customer.division()
        );

        Map<TextField, String> textFieldMap = Map.of(
                fldCustomerID, String.valueOf(customer.customerID()),
                fldName, customer.name(),
                fldAddress, customer.address(),
                fldPostalCode, customer.postalCode(),
                fldPhoneNumber, customer.phoneNumber()
        );

        comboBoxMap.forEach(ComboBoxBase::setValue);
        textFieldMap.forEach(TextInputControl::setText);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        SceneManager.loadScheduleScene(actionEvent);
    }
}































