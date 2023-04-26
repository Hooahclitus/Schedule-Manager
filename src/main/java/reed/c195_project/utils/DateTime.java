package reed.c195_project.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTime {
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // todo think of better name
    public static LocalDateTime toLocalDateTime(DatePicker date, ComboBox<Object> hour, ComboBox<Object> minute) {
        return LocalDateTime.of(date.getValue(),
                LocalTime.of((Integer) hour.getSelectionModel().getSelectedItem(),
                        (Integer) minute.getSelectionModel().getSelectedItem()));
    }

    public static LocalDateTime toLocalDateTime(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }
}
