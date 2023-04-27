package reed.c195_project.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import reed.c195_project.model.Appointment;
import reed.c195_project.model.Customer;

import java.sql.*;
import java.util.Map;

/**
 * A class that provides methods to interact with a MySQL database using JDBC.
 */
public abstract class JDBC {
    /**
     * The protocol used to connect to the database.
     */
    private static final String protocol = "jdbc";

    /**
     * The vendor of the database.
     */
    private static final String vendor = ":mysql:";

    /**
     * The location of the database.
     */
    private static final String location = "//localhost/";

    /**
     * The name of the database.
     */
    private static final String databaseName = "client_schedule";

    /**
     * The JDBC URL used to connect to the database.
     */
    private static final String jdbUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER";

    /**
     * The JDBC driver used to connect to the database.
     */
    private static final String driver = "com.mysql.cj.jdbc.Driver";

    /**
     * The username used to connect to the database.
     */
    private static final String userName = "sqlUser";

    /**
     * The password used to connect to the database.
     */
    private static final String password = "Passw0rd!";

    /**
     * The connection to the database.
     */
    public static Connection connection;

    /**
     * Opens a connection to the database.
     */
    public static void openConnection() {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(jdbUrl, userName, password);
            System.out.println("Connection Successful!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Closes the connection to the database.
     */
    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Connection Closed!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of all customers from the database.
     *
     * @return The list of customers.
     */
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

    /**
     * Retrieves a list of all appointments from the database.
     *
     * @return The list of appointments.
     */
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

    /**
     * This method executes a given SQL statement and returns the resulting data as an observable list.
     *
     * @param sql the SQL statement to be executed.
     * @return an observable list containing the resulting data from the executed SQL statement.
     */
    private static ObservableList<Object> selectFieldData(String sql) {
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

    /**
     * This method selects the Contact_Name field from the contacts table and returns it as an observable list.
     *
     * @return an observable list containing the Contact_Name field from the contacts table.
     */
    public static ObservableList<Object> selectContacts() {
        return selectFieldData("SELECT Contact_Name FROM contacts ORDER BY Contact_Name");
    }

    /**
     * This method selects the Customer_ID field from the customers table and returns it as an observable list.
     *
     * @return an observable list containing the Customer_ID field from the customers table.
     */
    public static ObservableList<Object> selectCustomerID() {
        return selectFieldData("SELECT Customer_ID FROM customers ORDER BY Customer_ID");
    }

    /**
     * This method selects the User_ID field from the users table and returns it as an observable list.
     *
     * @return an observable list containing the User_ID field from the users table.
     */
    public static ObservableList<Object> selectUserID() {
        return selectFieldData("SELECT User_ID FROM users ORDER BY User_ID");
    }

    /**
     * This method selects the Country field from the countries table and returns it as an observable list.
     *
     * @return an observable list containing the Country field from the countries table.
     */
    public static ObservableList<Object> selectCountry() {
        return selectFieldData("SELECT Country FROM countries");
    }

    /**
     * This method selects the Division field from the first_level_divisions table for a given country and returns it as an
     * observable list.
     *
     * @param countryID the ID of the country for which the Division field is to be selected.
     * @return an observable list containing the Division field from the first_level_divisions table for the given country.
     */
    public static ObservableList<Object> selectDivision(int countryID) {
        String sql = String.format("SELECT Division FROM first_level_divisions WHERE Country_ID = %d ORDER BY " +
                "Division", countryID);
        return selectFieldData(sql);
    }

    /**
     * This method deletes a record with a given ID from a specified table.
     *
     * @param sql      the SQL statement used to delete the record.
     * @param recordID the ID of the record to be deleted.
     * @throws SQLException if a database access error occurs or the SQL statement does not return a ResultSet.
     */
    public static void deleteRecord(String sql, int recordID) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, recordID);
        preparedStatement.executeUpdate();
    }


    /**
     * This method deletes an appointment record with a given ID from the appointments table.
     *
     * @param appointmentID the ID of the appointment record to be deleted.
     * @throws SQLException if a database access error occurs or the SQL statement does not return a ResultSet.
     */
    public static void deleteAppointment(int appointmentID) throws SQLException {
        deleteRecord("DELETE FROM appointments WHERE Appointment_ID = ?", appointmentID);
    }

    /**
     * This method deletes a customer record with a given ID from the customers table.
     *
     * @param customerID the ID of the customer record to be deleted.
     * @throws SQLException if a database access error occurs or the SQL statement does not return a ResultSet.
     */
    public static void deleteCustomer(int customerID) throws SQLException {
        deleteRecord("DELETE FROM customers WHERE Customer_ID = ?", customerID);
    }

    /**
     * This method updates a specified table with the given form data.
     *
     * @param sql      the SQL statement used to update the table.
     * @param formData a map of the form data to be used in the update statement.
     * @throws SQLException if a database access error occurs or the SQL statement does not return a ResultSet.
     */
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

    /**
     * This method updates the appointments table with the given form data.
     *
     * @param submit   the Button used to submit the form.
     * @param formData a map of the form data to be used in the update statement.
     * @throws SQLException if a database access error occurs or the SQL statement does not return a ResultSet.
     */
    public static void updateAppointmentsTable(Button submit, Map<Integer, ?> formData) throws SQLException {
        final String INSERT_APPOINTMENT_SQL = "INSERT INTO appointments (Title, Description, Location, Type, Start, " +
                "End," +
                " Customer_ID, User_ID, Contact_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, (SELECT Contact_ID FROM contacts " +
                "WHERE Contact_Name = ?))";

        final String UPDATE_APPOINTMENT_SQL = "UPDATE appointments SET Title = ?, Description = ?, Location = ?, Type" +
                " = " +
                "?, Start = ?, End = ?, Customer_ID = ?, User_ID = ?, Contact_ID = (SELECT Contact_ID FROM contacts " +
                "WHERE Contact_Name = ?) WHERE Appointment_ID = ?";

        String sql = submit.getText().equals("Update") ? UPDATE_APPOINTMENT_SQL : INSERT_APPOINTMENT_SQL;

        updateTable(sql, formData);
    }

/**
 * This method updates the customers table with the given form data.
 *
 * @param submit   the Button used to submit the form.
 * @param formData a map of the form data to be used in the update statement
 */
    public static void updateCustomersTable(Button submit, Map<Integer, ?> formData) throws SQLException {
        final String INSERT_CUSTOMER_SQL = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, " +
                "Division_ID) VALUES (?, ?, ?, ?, (SELECT Division_ID FROM first_level_divisions WHERE Division = ?))";

        final String UPDATE_CUSTOMER_SQL = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, " +
                "Phone " +
                "= ?, Division_ID = (SELECT Division_ID FROM first_level_divisions WHERE Division = ?) WHERE " +
                "Customer_ID = ?";

        String sql = submit.getText().equals("Update") ? UPDATE_CUSTOMER_SQL : INSERT_CUSTOMER_SQL;

        updateTable(sql, formData);
    }
}



















