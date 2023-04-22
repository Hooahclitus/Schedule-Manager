package reed.c195_project.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import reed.c195_project.model.Customer;
import reed.c195_project.utils.JDBC;
import reed.c195_project.utils.LoadScene;
import reed.c195_project.utils.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
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
        country.setItems(JDBC.selectFieldData("SELECT Country FROM countries ORDER BY Country"));
        country.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                switch ((String) newValue) {
                    case "U.S" -> populateDivisionComboBox(1);
                    case "UK" -> populateDivisionComboBox(2);
                    case "Canada" -> populateDivisionComboBox(3);
                }
            }
        });

        var fieldsAndLimits = Map.of(name, 50, address, 100, postalCode, 50, phoneNumber, 50);
        var combos = List.of(country, division);

        Validate.customerInputs(fieldsAndLimits, combos, submit);
    }

    private void populateDivisionComboBox(int countryId) {
        String sql = "SELECT Division FROM first_level_divisions WHERE Country_ID = %d ORDER BY Division";
        String query = String.format(sql, countryId);
        division.setItems(JDBC.selectFieldData(query));
        division.setDisable(false);
    }

    public void configureCustomerForm(Customer... customer) {
        switch (customer.length) {
            case 0 -> {
                submit.setText("Add");
                customerID.setText("Auto-Generated");
            }
            case 1 -> {
                submit.setText("Update");
                populateFormWithCustomerData(customer[0]);
            }
        }
    }

    public void populateFormWithCustomerData(Customer customer) {
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
    private void insertOrUpdateCustomer(ActionEvent actionEvent) throws SQLException, IOException {
        final var INSERT_CUSTOMER_SQL = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, " +
                "Division_ID) VALUES (?, ?, ?, ?, (SELECT Division_ID FROM first_level_divisions WHERE Division = ?))";

        final var UPDATE_CUSTOMER_SQL = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone " +
                "= ?, Division_ID = (SELECT Division_ID FROM first_level_divisions WHERE Division = ?) WHERE " +
                "Customer_ID = ?";

        var sql = submit.getText().equals("Update") ? UPDATE_CUSTOMER_SQL : INSERT_CUSTOMER_SQL;

        Map<Integer, Object> customerData = new HashMap<>();
        customerData.put(1, name.getText());
        customerData.put(2, address.getText());
        customerData.put(3, postalCode.getText());
        customerData.put(4, phoneNumber.getText());
        customerData.put(5, division.getSelectionModel().getSelectedItem());

        if (submit.getText().equals("Update")) {
            customerData.put(6, customerID.getText());
        }

        JDBC.updateTable(sql, customerData);
        LoadScene.schedule(actionEvent);
    }

    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        LoadScene.schedule(actionEvent);
    }
}
