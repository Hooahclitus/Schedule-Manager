package reed.c195_project.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import reed.c195_project.model.Appointment;
import reed.c195_project.model.Customer;
import reed.c195_project.utils.JDBC;
import reed.c195_project.utils.LoadScene;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
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
    TableView<Customer> tblCustomers;
    @FXML
    private TableView<Appointment> tblAppointments;

    @FXML
    private TableColumn<Customer, Object> colCustomerID, colCustomerName, colCustomerAddress, colCustomerDivision,
            colCustomerCountry, colCustomerPostal, colCustomerPhone;
    @FXML
    private TableColumn<Appointment, Object> colAppointmentID, colAppointmentCustomerID, colAppointmentUserID,
            colAppointmentTitle, colAppointmentDescription, colAppointmentLocation, colAppointmentContact,
            colAppointmentType, colAppointmentStartDate, colAppointmentEndDate, colAppointmentStartTime,
            colAppointmentEndTime;

    @FXML
    private ComboBox<String> comboAppointmentsFilter;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.customers = JDBC.selectCustomerRecords();
        this.appointments = JDBC.selectAppointmentRecords();

        setupCustomersTable();
        setupAppointmentsTable();
        setupAppointmentsFilter();
    }

    private void setupAppointmentsTable() {
        Map<TableColumn<Appointment, Object>, Function<Appointment, Object>> appointmentDataMap = Map.ofEntries(
                Map.entry(colAppointmentID, Appointment::appointmentID),
                Map.entry(colAppointmentCustomerID, Appointment::customerID),
                Map.entry(colAppointmentUserID, Appointment::userID),
                Map.entry(colAppointmentTitle, Appointment::title),
                Map.entry(colAppointmentDescription, Appointment::description),
                Map.entry(colAppointmentLocation, Appointment::location),
                Map.entry(colAppointmentContact, Appointment::contact),
                Map.entry(colAppointmentType, Appointment::type),
                Map.entry(colAppointmentStartDate, Appointment::startDate),
                Map.entry(colAppointmentEndDate, Appointment::endDate),
                Map.entry(colAppointmentStartTime, Appointment::startTime),
                Map.entry(colAppointmentEndTime, Appointment::endTime)
        );
        appointmentDataMap.forEach((col, func) -> col.setCellValueFactory(val -> new SimpleObjectProperty<>(func.apply(val.getValue()))));
        tblAppointments.setItems(appointments);
    }

    private void setupCustomersTable() {
        Map<TableColumn<Customer, Object>, Function<Customer, Object>> customerDataMap = Map.of(
                colCustomerID, Customer::customerID,
                colCustomerName, Customer::name,
                colCustomerAddress, Customer::address,
                colCustomerDivision, Customer::division,
                colCustomerCountry, Customer::country,
                colCustomerPostal, Customer::postalCode,
                colCustomerPhone, Customer::phoneNumber
        );

        customerDataMap.forEach((col, func) -> col.setCellValueFactory(val -> new SimpleObjectProperty<>(func.apply(val.getValue()))));
        tblCustomers.setItems(customers);
    }

    public void setupAppointmentsFilter() {
        Function<LocalDate, Integer> getCurrentWeek =
                date -> date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

        var filteredByMonth = appointments
                .stream()
                .filter(e -> e.startDate().getMonthValue() == LocalDate.now().getMonthValue())
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        var filteredByWeek = appointments
                .stream()
                .filter(e -> getCurrentWeek.apply(e.startDate()).equals(getCurrentWeek.apply(LocalDate.now())))
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
    private void addAppointment(ActionEvent actionEvent) throws IOException {
        LoadScene.appointment(actionEvent);
    }

    @FXML
    private void modifyAppointment(ActionEvent actionEvent) throws IOException {
        LoadScene.appointment(actionEvent, tblAppointments.getSelectionModel().getSelectedItem());
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

























