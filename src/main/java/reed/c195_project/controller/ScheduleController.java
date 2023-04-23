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
import reed.c195_project.utils.JDBC;
import reed.c195_project.utils.LoadScene;
import reed.c195_project.utils.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tblAppointments.getSelectionModel().selectedItemProperty().addListener(observable ->
                modifyAppointment.setDisable(observable == null));

        tblCustomers.getSelectionModel().selectedItemProperty().addListener(observable ->
                modifyCustomer.setDisable(observable == null));

        customers = JDBC.selectCustomerRecords();
        appointments = JDBC.selectAppointmentRecords();

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
                Map.entry(colAppointmentStart, e -> e.start().format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm"))),
                Map.entry(colAppointmentEnd, e -> e.end().format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm")))
        );

        appointmentData.forEach((col, func) -> col.setCellValueFactory(val -> new SimpleObjectProperty<>(func.apply(val.getValue()))));
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
        tblCustomers.setItems(customers);
    }

    public void setupAppointmentsFilter() {
        Function<LocalDate, Integer> getCurrentWeek =
                date -> date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

        var filteredByMonth = appointments
                .stream()
                .filter(e -> e.start().getMonthValue() == LocalDate.now().getMonthValue())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        var filteredByWeek = appointments
                .stream()
                .filter(e -> getCurrentWeek.apply(e.start().toLocalDate()).equals(getCurrentWeek.apply(LocalDate.now())))
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

    private void upcomingAppointmentsAlert(ObservableList<Appointment> appointments) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        var upcomingAppointments = Validate.areAppointmentsWithin15Minutes(appointments);
        var appointmentStrings = upcomingAppointments.stream()
                .filter(appointment -> appointment.start().isBefore(LocalDateTime.now().plusMinutes(15)))
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
        tblAppointments.setItems(JDBC.selectAppointmentRecords());
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

        JDBC.deleteRecord(sql, recordID);
        tblCustomers.setItems(JDBC.selectCustomerRecords());
    }

    @FXML
    private void logout(ActionEvent actionEvent) throws IOException {
        LoadScene.login(actionEvent);
    }
}

























