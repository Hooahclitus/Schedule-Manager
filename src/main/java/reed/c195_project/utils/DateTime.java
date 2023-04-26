package reed.c195_project.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTime {
    public static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    // todo think of better name
    public static LocalDateTime toLocalDateTime(DatePicker date, ComboBox<Object> hour, ComboBox<Object> minute) {
        return LocalDateTime.of(date.getValue(),
                LocalTime.of((Integer) hour.getValue(),
                        (Integer) minute.getValue()));
    }
}
