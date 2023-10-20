package project.schedule_manager.model;

/**
 * Represents a customer in the scheduling system
 */
public record Customer(int customerID,
                       String name,
                       String address,
                       String country,
                       String division,
                       String postalCode,
                       String phoneNumber) {
}
