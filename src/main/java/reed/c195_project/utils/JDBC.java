package reed.c195_project.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import reed.c195_project.model.Appointment;
import reed.c195_project.model.Customer;

import java.sql.*;
import java.util.Map;

public abstract class JDBC {
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER";
    private static final String driver = "com.mysql.cj.jdbc.Driver";
    private static final String userName = "sqlUser";
    private static final String password = "Passw0rd!";
    public static Connection connection;

    public static void openConnection() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(jdbUrl, userName, password);
            System.out.println("Connection Successful!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Connection Closed!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static ObservableList<Customer> selectCustomerRecords() {
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        String sql = "SELECT Customer_ID, Customer_Name, Address, Division, Country, Postal_Code, Phone " +
                "FROM customers " +
                "INNER JOIN first_level_divisions fld on customers.Division_ID = fld.Division_ID " +
                "INNER JOIN countries on fld.Country_ID = countries.Country_ID " +
                "ORDER BY Customer_ID";

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
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

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
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

    public static ObservableList<Object> selectFieldData(String sql) {
        ObservableList<Object> fieldData = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = connection.createStatement().executeQuery(sql);
            while (resultSet.next()) {
                fieldData.add(resultSet.getString(sql.substring("SELECT".length() + 1, sql.indexOf("FROM") - 1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return fieldData;
    }

    private static void updateTable(String sql, Map<Integer, ?> formData) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        formData.forEach((index, val) -> {
            try {
                preparedStatement.setObject(index, val);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        preparedStatement.executeUpdate();
    }

    public static void deleteRecord(String sql, int recordID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, recordID);
        preparedStatement.executeUpdate();
    }

    public static void updateAppointmentsTable(Button submit, Map<Integer, ?> formData) throws SQLException {
        final var INSERT_APPOINTMENT_SQL = "INSERT INTO appointments (Title, Description, Location, Type, Start, End," +
                " Customer_ID, User_ID, Contact_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT Contact_ID FROM contacts " +
                "WHERE Contact_Name = ?))";

        final var UPDATE_APPOINTMENT_SQL = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type = " +
                "?, Start = ?, End = ?, Customer_ID = ?, User_ID = ?, Contact_ID = (SELECT Contact_ID FROM contacts " +
                "WHERE Contact_Name = ?) WHERE Appointment_ID = ?";

        var sql = submit.getText().equals("Update") ? UPDATE_APPOINTMENT_SQL : INSERT_APPOINTMENT_SQL;

        updateTable(sql, formData);
    }

    public static void updateCustomersTable(Button submit, Map<Integer, ?> formData) throws SQLException {
        final var INSERT_CUSTOMER_SQL = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, " +
                "Division_ID) VALUES (?, ?, ?, ?, (SELECT Division_ID FROM first_level_divisions WHERE Division = ?))";

        final var UPDATE_CUSTOMER_SQL = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone " +
                "= ?, Division_ID = (SELECT Division_ID FROM first_level_divisions WHERE Division = ?) WHERE " +
                "Customer_ID = ?";

        var sql = submit.getText().equals("Update") ? UPDATE_CUSTOMER_SQL : INSERT_CUSTOMER_SQL;

        updateTable(sql, formData);
    }
}



















