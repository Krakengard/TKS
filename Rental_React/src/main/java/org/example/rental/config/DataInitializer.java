package org.example.rental.config;

import org.example.rental.model.*;
import org.example.rental.repository.UserRepository;
import org.example.rental.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INITIALIZING DATA ===");

        // Sprawdź czy już istnieją użytkownicy
        if (userRepository.findAll().isEmpty()) {
            System.out.println("Creating initial users...");

            // Administrator with password
            Administrator admin = new Administrator("admin", "System Administrator", "admin@rental.com", "IT");
            String adminPassword = "admin123";
            String encodedAdminPassword = passwordEncoder.encode(adminPassword);
            admin.setPassword(encodedAdminPassword);
            userRepository.save(admin);
            System.out.println("Created admin: " + admin.getLogin() + ", password: " + adminPassword + ", hash: " + encodedAdminPassword.substring(0, 20) + "...");

            // Resource Manager with password
            ResourceManager manager = new ResourceManager("manager", "Resource Manager", "manager@rental.com", "Equipment");
            String managerPassword = "manager123";
            String encodedManagerPassword = passwordEncoder.encode(managerPassword);
            manager.setPassword(encodedManagerPassword);
            userRepository.save(manager);
            System.out.println("Created manager: " + manager.getLogin() + ", password: " + managerPassword + ", hash: " + encodedManagerPassword.substring(0, 20) + "...");

            // Customers with passwords
            Customer customer1 = new Customer("john_doe", "John Doe", "john@example.com", "+123456789", "123 Main St");
            String customer1Password = "password123";
            String encodedCustomer1Password = passwordEncoder.encode(customer1Password);
            customer1.setPassword(encodedCustomer1Password);
            userRepository.save(customer1);
            System.out.println("Created customer1: " + customer1.getLogin() + ", password: " + customer1Password + ", hash: " + encodedCustomer1Password.substring(0, 20) + "...");

            Customer customer2 = new Customer("jane_smith", "Jane Smith", "jane@example.com", "+987654321", "456 Oak Ave");
            String customer2Password = "password123";
            String encodedCustomer2Password = passwordEncoder.encode(customer2Password);
            customer2.setPassword(encodedCustomer2Password);
            userRepository.save(customer2);
            System.out.println("Created customer2: " + customer2.getLogin() + ", password: " + customer2Password + ", hash: " + encodedCustomer2Password.substring(0, 20) + "...");
        } else {
            System.out.println("Users already exist, skipping creation...");
        }

        // Initialize sample resources
        if (resourceRepository.findAll().isEmpty()) {
            System.out.println("Creating initial resources...");

            Resource projector = new Resource("Epson Projector", "High-quality video projector", "AV Equipment", 50.0);
            resourceRepository.save(projector);
            System.out.println("Created resource: " + projector.getName());

            Resource laptop = new Resource("Dell Laptop", "Business laptop with 16GB RAM", "Computer", 30.0);
            resourceRepository.save(laptop);
            System.out.println("Created resource: " + laptop.getName());

            Resource meetingRoom = new Resource("Conference Room A", "Large meeting room with capacity for 20 people", "Room", 100.0);
            resourceRepository.save(meetingRoom);
            System.out.println("Created resource: " + meetingRoom.getName());
        } else {
            System.out.println("Resources already exist, skipping creation...");
        }

        System.out.println("=== DATA INITIALIZATION COMPLETE ===");
    }
}