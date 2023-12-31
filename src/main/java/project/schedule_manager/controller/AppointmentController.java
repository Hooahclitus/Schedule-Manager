package project.schedule_manager.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import project.schedule_manager.model.Appointment;
import project.schedule_manager.utils.DateTime;
import project.schedule_manager.utils.JDBC;
import project.schedule_manager.utils.LoadScene;
import project.schedule_manager.utils.Validate;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.rangeClosed;
import static javafx.collections.FXCollections.observableList;

public class AppointmentController implements Initializable {
    private List<Appointment> appointments;

    @FXML
    private DatePicker date;

    @FXML
    private TextField appointmentID, title, description, location, type;

    @FXML
    private ComboBox<Object> contacts, customerID, userID, startHour, startMinute, endHour, endMinute;

    @FXML
    private Button submit;


    /**
     * Initializes the controller class. Sets up the options for the start and end time
     * ComboBoxes, sets the options for the contacts, customerID, and userID ComboBoxes,
     * and validates the appointment input fields before allowing the user to submit.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: The use of lambda expressions in this method allows for concise and readable code.
     * By using a lambda expression in forEach() method, we are able to apply the same
     * operation (setting the items of the ComboBox) to both startMinute and endMinute,
     * and both startHour and endHour in just one line of code. Similarly, the use of
     * lambda expressions and method references in the map() method allows us to map each
     * hour to a LocalDateTime object, filter out appointment times that are not valid,
     * and map the remaining LocalDateTime objects to just their hour values in a single
     * stream operation.
     *
     * @param url            the URL of the FXML document
     * @param resourceBundle the ResourceBundle used to localize the FXML document
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stream.of(startMinute, endMinute).forEach(e -> e.setItems(observableList(rangeClosed(0, 59).boxed().collect(toList()))));
        Stream.of(startHour, endHour).forEach(e -> e.setItems(observableList(rangeClosed(0, 23)
                .mapToObj(i -> LocalDateTime.of(LocalDate.now(), LocalTime.of(i, 0)))
                .filter(Validate::appointmentTime)
                .map(LocalDateTime::getHour)
                .collect(toList()))));

        contacts.setItems(JDBC.selectContacts());
        customerID.setItems(JDBC.selectCustomerID());
        userID.setItems(JDBC.selectUserID());

        var fieldsAndLimits = Map.of(title, 50, description, 50, location, 50, type, 50);
        var combos = List.of(contacts, customerID, userID, startHour, startMinute, endHour, endMinute);

        Validate.appointmentInputs(fieldsAndLimits, combos, date, submit);
    }

    /**
     * Displays an alert to inform the user that the requested appointment time conflicts with other appointments.
     * <p>
     * <b>LAMBDA JUSTIFICATION</b>: This method uses a lambda expression to format the conflicting appointments as a string for display in the alert
     * dialog. This improves code readability and reduces the amount of boilerplate code required as compared to using a traditional
     * loop or iterator. Additionally, the use of the Stream API with Collectors.joining() allows for concise and efficient
     * concatenation of the appointment details into a single string.
     *
     * @param appointments a list of appointments to check for conflicts
     */
    private void conflictingAppointmentsAlert(List<Appointment> appointments) {
        var conflictingAppointments = appointments.stream()
                .map(appointment -> String.format("Appointment ID: %d\n\tDate: %s - Time: %s - %s",
                        appointment.appointmentID(),
                        appointment.date().format(DateTime.dateFormat),
                        appointment.start().format(DateTime.timeFormat),
                        appointment.end().format(DateTime.timeFormat)))
                .collect(Collectors.joining("\n\n"));

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Scheduling Conflict");
        alert.setHeaderText("Unable to Schedule Appointment");
        alert.setContentText(String.format("""
                The appointment time conflicts with the following:
                %s
                                
                Please select a different time slot.""", conflictingAppointments));
        alert.showAndWait();
    }

    /**
     * Shows an alert informing the user that the end time of an appointment is before or on the start time.
     */
    private void conflictingAppointmentTimeAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Time Selection");
        alert.setHeaderText("End Time is Before or on Start Time");
        alert.setContentText("Please select an End Time that is after the Start Time.");
        alert.show();
    }

    /**
     * Shows an alert informing the user that the start date of an appointment is before the current date.
     */
    private void conflictingStartDateTimeAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Date Selection");
        alert.setHeaderText("Start Date is Before Current Date");
        alert.setContentText("Please select a later Start Date.");
        alert.show();
    }

    /**
     * Sets the appointments list for verification of appointment data within the appointment GUI.
     *
     * @param appointments The list of appointments to be set for this object.
     */
    public void passAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    /**
     * Configures the appointment form based on the number of appointments passed as arguments.
     * If no appointments are passed, the form is set up for adding a new appointment, with an auto-generated appointment ID.
     * If one appointment is passed, the form is set up for updating an existing appointment, and the form is populated with the appointment's data.
     *
     * @param appointment A variable number of Appointment objects representing the appointment(s) to be configured on the form.
     *                     The length of the array determines the configuration of the form.
     */

    public void configureAppointmentForm(Appointment... appointment) {
        switch (appointment.length) {
            case 0 -> {
                submit.setText("Add");
                appointmentID.setText("Auto-Generated");
            }
            case 1 -> {
                submit.setText("Update");
                populateFormWithAppointmentData(appointment[0]);
            }
        }
    }

    /**
     * Populates a form with data from the selected appointment object.
     *
     * @param appointment The appointment object to extract data from.
     */
    public void populateFormWithAppointmentData(Appointment appointment) {
        var fields = Map.of(
                appointmentID, String.valueOf(appointment.appointmentID()),
                title, appointment.title(),
                description, appointment.description(),
                location, appointment.location(),
                type, appointment.type()
        );

        var combos = Map.of(
                contacts, appointment.contact(),
                customerID, appointment.customerID(),
                userID, appointment.userID(),
                startHour, appointment.start().getHour(),
                startMinute, appointment.start().getMinute(),
                endHour, appointment.end().getHour(),
                endMinute, appointment.end().getMinute()
        );

        fields.forEach(TextInputControl::setText);
        combos.forEach(ComboBoxBase::setValue);
        date.setValue(appointment.date());
    }

    /**
     * Inserts or updates an appointment into the database. If the submit button text is "Update", the appointment is
     * updated,
     * otherwise it is inserted. Validates if the appointments are overlapping or not, and provides an alert if the
     * appointments
     * are overlapping.
     *
     * @param actionEvent The event that triggered this method.
     *
     * @throws SQLException If there is an error accessing the database.
     * @throws IOException  If there is an error reading input.
     */
    @FXML
    private void insertOrUpdateAppointment(ActionEvent actionEvent) throws SQLException, IOException {
        var startDateTime = DateTime.toLocalDateTime(date, startHour, startMinute);
        var endDateTime = DateTime.toLocalDateTime(date, endHour, endMinute);

        var conflictingAppointments = submit.getText().equals("Update")
                ? Validate.areAppointmentsOverlapping(appointments, customerID, startDateTime, endDateTime, appointmentID)
                : Validate.areAppointmentsOverlapping(appointments, customerID, startDateTime, endDateTime);

        if (!conflictingAppointments.isEmpty()) {
            conflictingAppointmentsAlert(conflictingAppointments);
            return;
        }

        if (Validate.isEndTimeBeforeStartTime(startDateTime, endDateTime)) {
            conflictingAppointmentTimeAlert();
            return;
        }

        if (Validate.isAppointmentDateTimeBeforeCurrentDateTime(startDateTime)) {
            conflictingStartDateTimeAlert();
            return;
        }

        Map<Integer, Object> appointmentData = new HashMap<>();
        appointmentData.put(1, title.getText());
        appointmentData.put(2, description.getText());
        appointmentData.put(3, location.getText());
        appointmentData.put(4, type.getText());
        appointmentData.put(5, Timestamp.valueOf(startDateTime));
        appointmentData.put(6, Timestamp.valueOf(endDateTime));
        appointmentData.put(7, customerID.getValue());
        appointmentData.put(8, userID.getValue());
        appointmentData.put(9, contacts.getValue());

        if (submit.getText().equals("Update")) {
            appointmentData.put(10, appointmentID.getText());
        }

        JDBC.updateAppointmentsTable(submit, appointmentData);
        LoadScene.schedule(actionEvent, "appointment", false);
    }

    /**
     * This method cancels the current action and loads the appointment scene.
     *
     * @param actionEvent The event that triggered this method.
     *
     * @throws IOException If there is an error while loading the appointment scene.
     */
    @FXML
    private void cancel(ActionEvent actionEvent) throws IOException {
        LoadScene.schedule(actionEvent, "appointment", false);
    }
}
