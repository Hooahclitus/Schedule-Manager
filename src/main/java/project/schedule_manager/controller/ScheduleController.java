package project.schedule_manager.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import project.schedule_manager.model.Appointment;
import project.schedule_manager.model.Customer;
import project.schedule_manager.utils.DateTime;
import project.schedule_manager.utils.JDBC;
import project.schedule_manager.utils.LoadScene;
import project.schedule_manager.utils.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScheduleController implements Initializable {
    ObservableList<Customer> customers = FXCollections.observableArrayList();
    ObservableList<Appointment> appointments = FXCollections.observableArrayList();

    @FXML
    private TableView<Customer> tblCustomers;

    @FXML
    private TableView<Appointment> tblAppointments;

    @FXML
    private TableColumn<Customer, Object> colCustomerID, colCustomerName, colCustomerAddress, colCustomerDivision,
            colCustomerCountry, colCustomerPostal, colCustomerPhone;
    @FXML
    private TableColumn<Appointment, Object> colAppointmentID, colAppointmentCustomerID, colAppointmentUserID,
            colAppointmentTitle, colAppointmentDescription, colAppointmentLocation, colAppointmentContact,
            colAppointmentType, colAppointmentStart, colAppointmentEnd;

    @FXML
    private ComboBox<String> comboAppointmentsFilter;

    @FXML
    private Button modifyAppointment, modifyCustomer, deleteAppointment, deleteCustomer;

    @FXML
    private TextArea txtArea;

    @FXML
    private TabPane tabPane;


    /**
     * Initializes the controller class and sets up listeners for the modify and delete buttons in the appointments
     * and customers tabs.The listeners disable the buttons when there is no selection in the corresponding tableviews.
     * Additionally, this method sets up the tableviews for the customers and appointments tabs and configures the
     * filter for the appointments table.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: The use of lambda expressions in the listeners for the selection of the
     * tableviews provides a concise and readable way to set the disabled state of the modify and delete buttons
     * based on the presence or absence of a selection.
     *
     * @param url            the URL of the FXML document.
     * @param resourceBundle the ResourceBundle used to localize the FXML document.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tblAppointments.getSelectionModel().selectedItemProperty().addListener(observable -> {
            modifyAppointment.setDisable(observable == null);
            deleteAppointment.setDisable(observable == null);
        });

        tblCustomers.getSelectionModel().selectedItemProperty().addListener(observable -> {
            modifyCustomer.setDisable(observable == null);
            deleteCustomer.setDisable(observable == null);
        });

        setupCustomersTable();
        setupAppointmentsTable();
        setupAppointmentsFilter();
    }

    /**
     * Sets up the appointments table.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: A lambda expression is used in the forEach method call to set the cell value factory
     * for each appointment table column. This lambda expression allows us to easily map each column to the appropriate
     * appointment data field without having to write a separate function for each mapping.
     */
    private void setupAppointmentsTable() {
        Map<TableColumn<Appointment, Object>, Function<Appointment, Object>> appointmentData = Map.ofEntries(
                Map.entry(colAppointmentID, Appointment::appointmentID),
                Map.entry(colAppointmentCustomerID, Appointment::customerID),
                Map.entry(colAppointmentUserID, Appointment::userID),
                Map.entry(colAppointmentTitle, Appointment::title),
                Map.entry(colAppointmentDescription, Appointment::description),
                Map.entry(colAppointmentLocation, Appointment::location),
                Map.entry(colAppointmentContact, Appointment::contact),
                Map.entry(colAppointmentType, Appointment::type),
                Map.entry(colAppointmentStart, Appointment::startDateTimeFormatted),
                Map.entry(colAppointmentEnd, Appointment::endDateTimeFormatted)
        );

        appointmentData.forEach((col, func) -> col.setCellValueFactory(val -> new SimpleObjectProperty<>(func.apply(val.getValue()))));
        appointments = JDBC.selectAppointmentRecords();
        tblAppointments.setItems(appointments);
    }

    /**
     * Sets up the customers table.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: A lambda expression is used in the forEach method call to set the cell value factory
     * for each customer table column. This lambda expression allows us to easily map each column to the appropriate
     * customer data field without having to write a separate function for each mapping.
     */
    private void setupCustomersTable() {
        Map<TableColumn<Customer, Object>, Function<Customer, Object>> customerData = Map.of(
                colCustomerID, Customer::customerID,
                colCustomerName, Customer::name,
                colCustomerAddress, Customer::address,
                colCustomerDivision, Customer::division,
                colCustomerCountry, Customer::country,
                colCustomerPostal, Customer::postalCode,
                colCustomerPhone, Customer::phoneNumber
        );

        customerData.forEach((col, func) -> col.setCellValueFactory(val -> new SimpleObjectProperty<>(func.apply(val.getValue()))));
        customers = JDBC.selectCustomerRecords();
        tblCustomers.setItems(customers);
    }

    /**
     * Sets up the appointments filter.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: This method uses lambda expressions to simplify filtering of appointments by
     * month and week. A lambda expression is used to calculate the current week number, which is then used to filter
     * appointments by week. Another lambda expression is used as the listener for the selection of the appointments
     * filter combo box, which updates the appointments table based on the selected filter value.
     */
    private void setupAppointmentsFilter() {
        Function<LocalDate, Integer> getCurrentWeek =
                date -> date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

        var filteredByMonth = appointments
                .stream()
                .filter(e -> e.date().getMonthValue() == LocalDate.now().getMonthValue())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        var filteredByWeek = appointments
                .stream()
                .filter(e -> getCurrentWeek.apply(e.date()).equals(getCurrentWeek.apply(LocalDate.now())))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        comboAppointmentsFilter.getItems().addAll("All", "Month", "Week");
        comboAppointmentsFilter
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) ->
                        tblAppointments.setItems(
                                switch (newValue) {
                                    case "Month" -> filteredByMonth;
                                    case "Week" -> filteredByWeek;
                                    default -> appointments;
                                }
                        )
                );
    }

    /**
     * Selects the Appointments tab in the tab pane.
     */
    public void selectAppointmentsTab() {
        tabPane.getSelectionModel().select(0);
    }

    /**
     * Selects the Customers tab in the tab pane.
     */
    public void selectCustomersTab() {
        tabPane.getSelectionModel().select(1);
    }

    /**
     * Displays an alert message if there are any upcoming appointments within 15 minutes of the current time.
     * If there are upcoming appointments, it displays the appointment details in the alert message.
     * If there are no upcoming appointments, it displays a message indicating so.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: Uses a lambda expression in the stream to filter and map the upcomingAppointments
     * list
     * into a list of appointment details strings. This simplifies the code by avoiding the need for a separate
     * loop to process the list.
     */
    public void upcomingAppointmentsAlert() {
        var upcomingAppointments = Validate.areAppointmentsWithin15Minutes(appointments);
        var appointmentStrings = upcomingAppointments.stream()
                .filter(appointment -> appointment.start().isBefore(LocalTime.now().plusMinutes(15)))
                .map(appointment -> String.format("Appointment ID: %d\n\tDate: %s - Time: %s - %s\n",
                        appointment.appointmentID(),
                        appointment.date().format(DateTime.dateFormat),
                        appointment.start().format(DateTime.timeFormat),
                        appointment.end().format(DateTime.timeFormat)))
                .toList();

        String appointmentDetails = String.join("\n", appointmentStrings);

        Alert alert;
        if (!upcomingAppointments.isEmpty()) {
            alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Upcoming Appointments");
            alert.setHeaderText("There are appointments within 15 minutes of the current time.");
            alert.setContentText("The following appointments are scheduled within 15 minutes:\n\n" + appointmentDetails);
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Upcoming Appointments");
            alert.setHeaderText("No appointments within 15 minutes of the current time.");
            alert.setContentText("There are no upcoming appointments within 15 minutes of the current time.");
        }
        alert.showAndWait();
    }

    /**
     * This method counts the number of appointments by type and month, then sorts the results and formats them as a
     * string. The string is then set as the text of a text area.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: Lambda expressions are used in the Collectors.groupingBy and map methods to
     * specify the grouping and mapping
     * functions, respectively. They provide a concise and readable way to express the logic for grouping and mapping
     * appointments by their type and month.
     */
    @FXML
    private void countAppointmentByTypeThenMonth() {
        var result = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::type,
                        Collectors.groupingBy(appointment -> appointment.date().getMonth(), Collectors.counting())))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(type -> String.format("%s:\n%s",
                        type.getKey(),
                        type.getValue()
                                .entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(entry -> String.format("\t%s: %d",
                                        entry.getKey().toString().charAt(0)
                                                + entry.getKey().toString().substring(1).toLowerCase(),
                                        entry.getValue()))
                                .collect(Collectors.joining("\n"))))
                .collect(Collectors.joining("\n\n"));

        txtArea.setText(result);
    }

    /**
     * This method counts the number of appointments by date, then sorts the results and formats them as a string.
     * The string is then set as the text of a text area.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: The lambda expression is used to simplify the process of formatting the results
     * as a string. It is used to replace a traditional for-each loop to iterate over the map entries and concatenate
     * the results as a string.
     */
    @FXML
    private void countAppointmentByDate() {
        var results = appointments.stream()
                .map(Appointment::date)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s: %s",
                        entry.getKey(),
                        entry.getValue()))
                .collect(Collectors.joining("\n"));

        txtArea.setText(results);
    }

    /**
     * This method groups appointments by contact, then formats the results as a string.
     * The string is then set as the text of a text area.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: The lambda expression is used to format the output for each appointment in the
     * resulting list. This allows for a more concise and readable code, as well as easier maintenance and
     * modification of the formatting in the future.
     */
    @FXML
    private void appointmentsByContact() {
        var result = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::contact))
                .entrySet().stream()
                .map(entry -> String.format("%s:\n%s", entry.getKey(), entry.getValue().stream()
                        .map(appointment -> String.format("""
                                        Appointment ID: %s
                                        \tTitle: %s, Type: %s, Description: %s, Start Date/Time: %s, End Date/Time: %s, Customer ID: %s
                                        """,
                                appointment.appointmentID(),
                                appointment.title(),
                                appointment.type(),
                                appointment.description(),
                                appointment.start(),
                                appointment.end(),
                                appointment.customerID()))
                        .collect(Collectors.joining("\n"))))
                .collect(Collectors.joining("\n"));

        txtArea.setText(result);
    }

    /**
     * Launches the 'Add Appointment' screen.
     *
     * @param actionEvent the ActionEvent that triggered this method call
     *
     * @throws IOException if an I/O error occurs while loading the scene
     */
    @FXML
    private void addAppointment(ActionEvent actionEvent) throws IOException {
        LoadScene.appointment(actionEvent, appointments);
    }

    /**
     * Launches the 'Modify Appointment' screen, pre-populated with the selected appointment's data.
     *
     * @param actionEvent the ActionEvent that triggered this method call
     *
     * @throws IOException if an I/O error occurs while loading the scene
     */
    @FXML
    private void modifyAppointment(ActionEvent actionEvent) throws IOException {
        LoadScene.appointment(actionEvent, appointments, tblAppointments.getSelectionModel().getSelectedItem());
    }

    /**
     * Deletes the selected appointment and updates the table view with the remaining appointments.
     * Prompts the user to confirm the deletion before proceeding.
     *
     * @throws SQLException if a database access error occurs while deleting the appointment
     */
    @FXML
    private void deleteAppointment() throws SQLException {
        var appointment = tblAppointments.getSelectionModel().getSelectedItem();
        var appointmentID = appointment.appointmentID();
        var appointmentType = appointment.type();

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Cancellation");
        confirmationAlert.setHeaderText("Are you sure?");
        confirmationAlert.setContentText("Are you sure you want to cancel the selected appointment?");

        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        confirmationAlert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == okButton) {
            JDBC.deleteAppointment(appointmentID);
            appointments = JDBC.selectAppointmentRecords();
            tblAppointments.setItems(appointments);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Cancellation Successful");
            successAlert.setHeaderText("Appointment cancelled");
            successAlert.setContentText(String.format("""
                            Appointment ID: %d
                            Appointment Type: %s

                            The selected appointment was successfully cancelled.
                            """,
                    appointmentID,
                    appointmentType));
            successAlert.showAndWait();
        }
    }

    /**
     * Loads the customer scene for adding a new customer.
     *
     * @param actionEvent the action event triggering the method
     *
     * @throws IOException if an I/O error occurs while loading the scene
     */
    @FXML
    private void addCustomer(ActionEvent actionEvent) throws IOException {
        LoadScene.customer(actionEvent);
    }


    /**
     * Loads the customer scene for modifying an existing customer.
     *
     * @param actionEvent the action event triggering the method
     *
     * @throws IOException if an I/O error occurs while loading the scene
     */
    @FXML
    private void modifyCustomer(ActionEvent actionEvent) throws IOException {
        LoadScene.customer(actionEvent, tblCustomers.getSelectionModel().getSelectedItem());
    }

    /**
     * Deletes the selected customer and all associated appointments, after displaying a confirmation alert to the user.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: In this method, a lambda expression is used to iterate through the appointments
     * list and
     * filter the appointments associated with the selected customer. This filtering is done to delete all associated
     * appointments of the customer being deleted. The lambda expression is passed as an argument to the forEach()
     * method, which helps in concise and readable code.
     *
     * @throws SQLException if there is an error in the SQL query or database connection.
     */
    @FXML
    private void deleteCustomer() throws SQLException {
        var customerID = tblCustomers.getSelectionModel().getSelectedItem().customerID();

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Are you sure?");
        confirmationAlert.setContentText("Are you sure you want to delete the selected customer? Note: All associated" +
                " appointments will also be deleted.");

        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        confirmationAlert.getButtonTypes().setAll(okButton, cancelButton);

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == okButton) {
            appointments.stream()
                    .filter(appointment -> appointment.customerID().contains(String.valueOf(customerID)))
                    .forEach(appointment -> {
                        try {
                            JDBC.deleteAppointment(appointment.appointmentID());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

            appointments = JDBC.selectAppointmentRecords();
            tblAppointments.setItems(appointments);

            JDBC.deleteCustomer(customerID);
            customers = JDBC.selectCustomerRecords();
            tblCustomers.setItems(customers);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Deletion Successful");
            successAlert.setHeaderText("Customer deleted");
            successAlert.setContentText("The selected customer and all associated appointments were successfully " +
                    "deleted.");
            successAlert.showAndWait();
        }
    }

    /**
     * Logs out the current user and returns to the login screen.
     *
     * @param actionEvent the action event triggering the method
     *
     * @throws IOException if an I/O error occurs while loading the login screen
     */
    @FXML
    private void logout(ActionEvent actionEvent) throws IOException {
        LoadScene.login(actionEvent);
    }
}
