package reed.c195_project.model;

import java.time.LocalDateTime;

public record Appointment(int appointmentID,
                          String title,
                          String description,
                          String location,
                          String contact,
                          String type,
                          LocalDateTime start,
                          LocalDateTime end,
//                          LocalDate startDate,
//                          LocalTime startTime,
//                          LocalDate endDate,
//                          LocalTime endTime,
                          String customerID,
                          String userID) {
}
