package reed.c195_project.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import reed.c195_project.model.Appointment;
import reed.c195_project.model.Customer;
import reed.c195_project.utils.DateTime;
import reed.c195_project.utils.JDBC;
import reed.c195_project.utils.LoadScene;
import reed.c195_project.utils.Validate;

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

    public void selectAppointmentsTab() {
        tabPane.getSelectionModel().select(0);
    }

    public void selectCustomersTab() {
        tabPane.getSelectionModel().select(1);
    }

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
                                appointment.appointmentID(), appointment.title(), appointment.type(),
                                appointment.description(), appointment.start(), appointment.end(),
                                appointment.customerID()))
                        .collect(Collectors.joining("\n"))))
                .collect(Collectors.joining("\n"));

        txtArea.setText(result);
    }

    public void upcomingAppointmentsAlert() {
        var upcomingAppointments = Validate.areAppointmentsWithin15Minutes(appointments);
        var appointmentStrings = upcomingAppointments.stream()
                .filter(appointment -> appointment.start().isBefore(LocalTime.now().plusMinutes(15)))
                .map(appointment -> String.format("Appointment ID: %d\n\tDate: %s - Time: %s - %s\n",
                        appointment.appointmentID(),
                        appointment.start().format(DateTime.dateFormat),
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

    @FXML
    private void addAppointment(ActionEvent actionEvent) throws IOException {
        LoadScene.appointment(actionEvent, appointments);
    }

    @FXML
    private void modifyAppointment(ActionEvent actionEvent) throws IOException {
        LoadScene.appointment(actionEvent, appointments, tblAppointments.getSelectionModel().getSelectedItem());
    }

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
            successAlert.setContentText(String.format("Appointment ID: %d\nAppointment Type: %s\n\nThe selected appointment was successfully cancelled.", appointmentID, appointmentType));
            successAlert.showAndWait();
        }
    }

    @FXML
    private void addCustomer(ActionEvent actionEvent) throws IOException {
        LoadScene.customer(actionEvent);
    }

    @FXML
    private void modifyCustomer(ActionEvent actionEvent) throws IOException {
        LoadScene.customer(actionEvent, tblCustomers.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void deleteCustomer() throws SQLException {
        var customerID = tblCustomers.getSelectionModel().getSelectedItem().customerID();

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Are you sure?");
        confirmationAlert.setContentText("Are you sure you want to delete the selected customer? Note: All associated appointments will also be deleted.");

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
            successAlert.setContentText("The selected customer and all associated appointments were successfully deleted.");
            successAlert.showAndWait();
        }
    }

    @FXML
    private void logout(ActionEvent actionEvent) throws IOException {
        LoadScene.login(actionEvent);
    }
}

























