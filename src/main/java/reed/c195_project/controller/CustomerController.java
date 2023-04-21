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
    private TextField customerID, name, address, postalCode, phoneNumber;

    @FXML
    private ComboBox<Object> country, division;

    @FXML
    private Button submit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        country.setItems(SQL.selectColumnData("SELECT Country FROM countries ORDER BY Country"));
        country.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch ((String) newValue) {
                    case "U.S" -> setDivisionItems(1);
                    case "UK" -> setDivisionItems(2);
                    case "Canada" -> setDivisionItems(3);
                }
            }
        });

        var fieldsAndLimits = Map.of(name, 50, address, 100, postalCode, 50, phoneNumber, 50);
        var combos = List.of(country, division);

        Validate.customerInputs(fieldsAndLimits, combos, submit);
    }

    private void setDivisionItems(int countryId) {
        String sql = "SELECT Division FROM first_level_divisions WHERE Country_ID = %d ORDER BY Divison";
        String query = String.format(sql, countryId);
        division.setItems(SQL.selectColumnData(query));
        division.setDisable(false);
    }

    @FXML
    private void insertCustomer(ActionEvent actionEvent) throws SQLException, IOException {
        Map<Integer, Object> customerData = Map.of(
                1, name.getText(),
                2, address.getText(),
                3, postalCode.getText(),
                4, phoneNumber.getText(),
                5, division.getSelectionModel().getSelectedItem()
        );

        String sql = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Division_ID) " +
                "VALUES (?, ?, ?, ?, (SELECT Division_ID FROM first_level_divisions WHERE Division = ?))";

        SQL.updateTableData(sql, customerData);
        SceneManager.loadScheduleScene(actionEvent);
    }

    @FXML
    private void updateCustomer(ActionEvent actionEvent) throws SQLException, IOException {
        Map<Integer, Object> customerData = Map.of(
                1, name.getText(),
                2, address.getText(),
                3, postalCode.getText(),
                4, phoneNumber.getText(),
                5, division.getSelectionModel().getSelectedItem(),
                6, customerID.getText()
        );

        String sql = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, " +
                "Division_ID = (SELECT Division_ID FROM first_level_divisions WHERE Division = ?) " +
                "WHERE Customer_ID = ?";

        SQL.updateTableData(sql, customerData);
        SceneManager.loadScheduleScene(actionEvent);
    }

    public void loadCustomerDataIntoForm(Customer customer) {
        Map<TextField, String> fields = Map.of(
                customerID, String.valueOf(customer.customerID()),
                name, customer.name(),
                address, customer.address(),
                postalCode, customer.postalCode(),
                phoneNumber, customer.phoneNumber()
        );

        Map<ComboBox<Object>, String> combos = Map.of(
                country, customer.country(),
                division, customer.division()
        );

        fields.forEach(TextInputControl::setText);
        combos.forEach(ComboBoxBase::setValue);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        SceneManager.loadScheduleScene(actionEvent);
    }
}































