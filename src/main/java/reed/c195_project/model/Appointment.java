package reed.c195_project.model;

import reed.c195_project.utils.DateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public LocalDateTime startDateTime() {
        return LocalDateTime.of(date, start);
    }

    public LocalDateTime endDateTime() {
        return LocalDateTime.of(date, end);
    }

    public String startDateTimeFormatted() {
        return startDateTime().format(DateTime.formatter);
    }

    public String endDateTimeFormatted() {
        return endDateTime().format(DateTime.formatter);
    }
}













