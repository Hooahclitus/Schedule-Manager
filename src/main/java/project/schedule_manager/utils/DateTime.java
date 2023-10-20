package project.schedule_manager.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * A utility class for working with date and time formatting.
 */
public abstract class DateTime {
    /**
     * A date-time formatter for the format "yyyy-MM-dd HH:mm".
     */
    public static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * A date formatter for the format "yyyy-MM-dd".
     */
    public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * A time formatter for the format "HH:mm".
     */
    public static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Converts the given DatePicker, hour, and minute values into a LocalDateTime object.
     *
     * @param date   The DatePicker object representing the date.
     * @param hour   The ComboBox object representing the hour (0-23).
     * @param minute The ComboBox object representing the minute (0-59).
     *
     * @return The LocalDateTime object representing the given date, hour, and minute.
     */
    public static LocalDateTime toLocalDateTime(DatePicker date, ComboBox<Object> hour, ComboBox<Object> minute) {
        return LocalDateTime.of(date.getValue(),
                LocalTime.of((Integer) hour.getValue(),
                        (Integer) minute.getValue()));
    }

    /**
     * Converts a LocalDate object and a LocalTime object to a LocalDateTime object.
     *
     * @param date the LocalDate object to be converted.
     * @param time the LocalTime object to be converted.
     *
     * @return the resulting LocalDateTime object.
     */
    public static LocalDateTime toLocalDateTime(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }
}
