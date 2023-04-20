package reed.c195_project.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record Appointment(int appointmentID,
                          String title,
                          String description,
                          String location,
                          String contact,
                          String type,
                          LocalDate startDate,
                          LocalTime startTime,
                          LocalDate endDate,
                          LocalTime endTime,
                          String customerID,
                          String userID) {

    public int getHour() {
        return this.startTime.getHour();
    }

    public int getMinute() {
        return this.startTime.getMinute();
    }
}

























