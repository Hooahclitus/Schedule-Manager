package reed.c195_project.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import reed.c195_project.model.Appointment;
import reed.c195_project.model.Customer;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;

public abstract class SQL {
    public static ObservableList<Customer> selectCustomerRecords() {
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        String sql = "SELECT Customer_ID, Customer_Name, Address, Division, Country, Postal_Code, Phone " +
                "FROM customers " +
                "INNER JOIN first_level_divisions fld on customers.Division_ID = fld.Division_ID " +
                "INNER JOIN countries on fld.Country_ID = countries.Country_ID " +
                "ORDER BY Customer_ID";

        try (ResultSet resultSet = JDBC.connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                customers.add(new Customer(
                        resultSet.getInt("Customer_ID"),
                        resultSet.getString("Customer_Name"),
                        resultSet.getString("Address"),
                        resultSet.getString("Country"),
                        resultSet.getString("Division"),
                        resultSet.getString("Postal_Code"),
                        resultSet.getString("Phone")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customers;
    }

    public static ObservableList<Appointment> selectAppointmentRecords() {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();
        String sql = "SELECT Appointment_ID, Title, Description, Location, Contact_Name, Type, Start, End, " +
                "Customer_ID, User_ID " +
                "FROM appointments " +
                "INNER JOIN contacts on appointments.Contact_ID = contacts.Contact_ID " +
                "ORDER BY Appointment_ID";

        try (ResultSet resultSet = JDBC.connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                appointments.add(new Appointment(
                        resultSet.getInt("Appointment_ID"),
                        resultSet.getString("Title"),
                        resultSet.getString("Description"),
                        resultSet.getString("Location"),
                        resultSet.getString("Contact_Name"),
                        resultSet.getString("Type"),
                        resultSet.getTimestamp("Start").toLocalDateTime().toLocalDate(),
                        resultSet.getTimestamp("Start").toLocalDateTime().toLocalTime(),
                        resultSet.getTimestamp("End").toLocalDateTime().toLocalDate(),
                        resultSet.getTimestamp("End").toLocalDateTime().toLocalTime(),
                        resultSet.getString("Customer_ID"),
                        resultSet.getString("User_ID")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return appointments;
    }

    public static ObservableList<Object> selectColumnData(String sql) {
        ObservableList<Object> fieldList = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = JDBC.connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                fieldList.add(resultSet.getString(sql.substring("SELECT".length() + 1, sql.indexOf("FROM") - 1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return fieldList;
    }

    public static void updateTableData(String sql, Map<Integer, ?> maps) throws SQLException {
        PreparedStatement preparedStatement = JDBC.connection.prepareStatement(sql);

        maps.forEach((index, val) -> {
            try {
                preparedStatement.setObject(index, val);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        preparedStatement.executeUpdate();
    }

    public static void deleteRecord(String sql, int recordID) throws SQLException {
        PreparedStatement preparedStatement = JDBC.connection.prepareStatement(sql);
        preparedStatement.setInt(1, recordID);
        preparedStatement.executeUpdate();
    }
}












