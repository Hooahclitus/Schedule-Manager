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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Map;
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
    private Button modifyAppointment, modifyCustomer;

    @FXML
    private TextArea txtArea;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tblAppointments.getSelectionModel().selectedItemProperty().addListener(observable ->
                modifyAppointment.setDisable(observable == null));

        tblCustomers.getSelectionModel().selectedItemProperty().addListener(observable ->
                modifyCustomer.setDisable(observable == null));

        setupCustomersTable();
        setupAppointmentsTable();
        setupAppointmentsFilter();

        upcomingAppointmentsAlert(appointments);
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

    @FXML
    private void countAppointmentByMonth() {
        String result = appointments.stream()
                .map(appointment -> appointment.date().getMonth())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s: %d", entry.getKey().toString().charAt(0)
                        + entry.getKey().toString().substring(1).toLowerCase(), entry.getValue()))
                .collect(Collectors.joining("\n"));

        txtArea.setText(result);
    }

    @FXML
    private void countAppointmentByType() {
        String result = appointments.stream()
                .map(Appointment::type)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s: %s", entry.getKey().charAt(0)
                        + entry.getKey().substring(1).toLowerCase(), entry.getValue()))
                .collect(Collectors.joining("\n"));

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
                        .map(appointment -> String.format("Appointment ID: %s, Title: %s, Type: %s, Description: %s, " +
                                        "Start Date/Time: %s, End Date/Time: %s, Customer ID: %s",
                                appointment.appointmentID(), appointment.title(), appointment.type(),
                                appointment.description(), appointment.start(), appointment.end(),
                                appointment.customerID()))
                        .collect(Collectors.joining("\n"))))
                .collect(Collectors.joining("\n\n"));

        txtArea.setText(result);
    }

    private void upcomingAppointmentsAlert(ObservableList<Appointment> appointments) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        var upcomingAppointments = Validate.areAppointmentsWithin15Minutes(appointments);
        var appointmentStrings = upcomingAppointments.stream()
                .filter(appointment -> appointment.start().isBefore(LocalTime.now().plusMinutes(15)))
                .map(appointment -> String.format("Appointment ID: %d, Date: %s, Time: %s - %s",
                        appointment.appointmentID(),
                        appointment.start().format(dateFormat),
                        appointment.start().format(timeFormat),
                        appointment.end().format(timeFormat)))
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
        var sql = "DELETE FROM appointments WHERE Appointment_ID = ?";
        var recordID = tblAppointments.getSelectionModel().getSelectedItem().appointmentID();

        JDBC.deleteRecord(sql, recordID);
        appointments = JDBC.selectAppointmentRecords();
        tblAppointments.setItems(appointments);
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
        var sql = "DELETE FROM customers WHERE Customer_ID = ?";
        var recordID = tblCustomers.getSelectionModel().getSelectedItem().customerID();

        if (appointments.stream()
                .map(Appointment::customerID)
                .toList()
                .contains(String.valueOf(tblCustomers.getSelectionModel().getSelectedItem().customerID()))) {

            Alert hasAppointmentsAlert = new Alert(Alert.AlertType.WARNING);
            hasAppointmentsAlert.setTitle("Customer has appointments");
            hasAppointmentsAlert.setHeaderText("Cannot delete customer");
            hasAppointmentsAlert.setContentText("There are appointments scheduled for this customer. Please delete " +
                    "the appointments first.");
        } else {
            JDBC.deleteRecord(sql, recordID);
            customers = JDBC.selectCustomerRecords();
            tblCustomers.setItems(customers);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Customer deleted");
            successAlert.setHeaderText(null);
            successAlert.setContentText("The selected customer was successfully deleted.");
        }
    }

    @FXML
    private void logout(ActionEvent actionEvent) throws IOException {
        LoadScene.login(actionEvent);
    }
}

























