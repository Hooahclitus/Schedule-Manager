# Schedule Manager

## Overview
**Schedule Manager** is a personal project developed to practice Java and explore JavaFX and JavaFXML. The application serves as a tool to facilitate the scheduling of appointments with customers.

## Environment & Tools
- **IDE**: IntelliJ Idea 2021.1.3 (Community Edition)
- **JDK**: 17.0.1
- **JavaFX**: JavaFX-SDK-17.0.1
- **MySQL Connector Driver**: mysql-connector-java:8.0.25

## Features

### Authentication
- Users must supply a valid username and password to access the application.
- Unsuccessful login attempts will result in an error message.

### Appointments Management
- View all current appointments.
- Ability to filter appointments by week, month, or view all.
- Add, modify, or delete appointments.
- Note: Appointments are restricted to business hours.

### Customer Management
- View all customers in the database.
- Add, modify, or delete customers.
- Deleting a customer will also remove all associated appointments.

### Reports
- Access detailed reports on appointments and customers.
- Generate a count of appointments by date.

## How to Use
1. **Login**: Start by entering your username and password.
2. **Main Dashboard**: After logging in, you'll be directed to the main dashboard showcasing the appointments table.
3. **Tabs**: Utilize the tabs for different functionalities:
    - **Appointments Tab**: View and manage appointments.
    - **Customers Tab**: View and manage customer information.
    - **Reports Tab**: Obtain insights on appointments and customers.
4. **Logout & Exit**: Use the "Logout" button to navigate back to the login window. To close the application, select "Exit."

## Learning & Development
This project served as a hands-on exercise to delve deeper into Java's capabilities, especially in terms of UI development with JavaFX and JavaFXML. It was a valuable experience in understanding how to structure a project, design a user-friendly interface, and integrate with a database.

## License
This project is licensed under the MIT License. Please see the `LICENSE` file for more details.
