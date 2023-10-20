package project.schedule_manager.model;

import project.schedule_manager.utils.DateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents an appointment in the scheduling system.
 */
public record Appointment(int appointmentID,
                          String title,
                          String description,
                          String location,
                          String contact,
                          String type,
                          LocalDate date,
                          LocalTime start,
                          LocalTime end,
                          String customerID,
                          String userID) {

    /**
     * Returns the start date and time of the appointment as a LocalDateTime object.
     *
     * @return the start date and time of the appointment
     */
    public LocalDateTime startDateTime() {
        return LocalDateTime.of(date, start);
    }

    /**
     * Returns the end date and time of the appointment as a LocalDateTime object.
     *
     * @return the end date and time of the appointment
     */
    public LocalDateTime endDateTime() {
        return LocalDateTime.of(date, end);
    }

    /**
     * Returns a formatted string representation of the start date and time of the appointment.
     *
     * @return a formatted string representation of the start date and time of the appointment
     */
    public String startDateTimeFormatted() {
        return startDateTime().format(DateTime.dateTimeFormat);
    }

    /**
     * Returns a formatted string representation of the end date and time of the appointment.
     *
     * @return a formatted string representation of the end date and time of the appointment
     */
    public String endDateTimeFormatted() {
        return endDateTime().format(DateTime.dateTimeFormat);
    }
}
