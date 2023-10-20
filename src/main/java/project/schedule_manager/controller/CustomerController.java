package project.schedule_manager.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import project.schedule_manager.model.Customer;
import project.schedule_manager.utils.JDBC;
import project.schedule_manager.utils.LoadScene;
import project.schedule_manager.utils.Validate;

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


    /**
     * Initializes the customer form with default values and sets up listeners on the country combo box.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: The listener uses a lambda expression to check for a new selection in the country combo box.
     * If a new selection is made, the division combo box is populated with the appropriate divisions for the selected country.
     * This reduces the amount of boilerplate code needed for handling the combo box selection events.
     * <p>
     * The method also validates the customer inputs to ensure they are not empty and within the specified limits.
     *
     * @param url the URL of the FXML document
     * @param resourceBundle the ResourceBundle used to localize the FXML document
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        country.setItems(JDBC.selectCountry());
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

    /**
     * Populates the division combo box with the divisions of a given country ID.
     *
     * @param countryId the ID of the country for which to retrieve the divisions
     */
    private void populateDivisionComboBox(int countryId) {
        division.setItems(JDBC.selectDivision(countryId));
        division.setDisable(false);
    }

    /**
     * Configures the customer form based on the number of customers passed as arguments.
     * If no customers are passed, sets the form for adding a new customer, with an auto-generated customer ID.
     * If one customer is passed, sets the form for updating an existing customer, and populates the form with the customer's data.
     *
     * @param customer A variable number of Customer objects, representing the customer(s) to be configured on the form.
     *                 The length of the array determines the configuration of the form.
     */
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

    /**
     * Populates the form with the given customer data.
     *
     * @param customer the customer whose data will be used to populate the form
     */
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

    /**
     * Inserts or updates customer data into the database.
     *
     * @param actionEvent the event triggered by the user's action
     * @throws SQLException if there is an error accessing the database
     * @throws IOException if there is an error loading the scene
     */
    @FXML
    private void insertOrUpdateCustomer(ActionEvent actionEvent) throws SQLException, IOException {
        Map<Integer, Object> customerData = new HashMap<>();
        customerData.put(1, name.getText());
        customerData.put(2, address.getText());
        customerData.put(3, postalCode.getText());
        customerData.put(4, phoneNumber.getText());
        customerData.put(5, division.getValue());

        if (submit.getText().equals("Update")) {
            customerData.put(6, customerID.getText());
        }

        JDBC.updateCustomersTable(submit, customerData);
        LoadScene.schedule(actionEvent, "customer", false);
    }

    /**
     * Cancels the current operation and returns to the customer scene.
     *
     * @param actionEvent The event that triggered the cancellation.
     * @throws IOException If an error occurs while loading the customer scene.
     */
    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        LoadScene.schedule(actionEvent, "customer", false);
    }
}
