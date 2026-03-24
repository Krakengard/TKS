package org.example.rental.application.service;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ResourceNotFoundException;
import org.example.rental.domain.model.*;
import org.example.rental.ports.output.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepositoryPort userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        System.out.println("=== UserManager.createUser ===");
        System.out.println("User login: " + user.getLogin());
        System.out.println("User class: " + user.getClass().getSimpleName());
        System.out.println("User password raw: " + (user.getPassword() != null ? "[PRESENT]" : "NULL"));

        validateUser(user);

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            System.out.println("Original password length: " + user.getPassword().length());
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            System.out.println("Password encoded to: " + encodedPassword.substring(0, Math.min(20, encodedPassword.length())) + "...");
        } else {
            System.out.println("ERROR: Password is null or empty!");
            throw new IllegalArgumentException("Password is required");
        }

        User savedUser = userRepository.save(user);
        System.out.println("User saved with ID: " + savedUser.getId());

        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        retrievedUser.ifPresent(u -> {
            System.out.println("Retrieved user password from DB: " + (u.getPassword() != null ? "[PRESENT, length: " + u.getPassword().length() + "]" : "NULL"));
        });

        return savedUser;
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByIdSafe(UUID id) {
        Optional<User> user = userRepository.findById(id);
        // NIE czyść hasła - tylko do debugowania
        user.ifPresent(u -> {
            System.out.println("getUserByIdSafe - User: " + u.getLogin() + ", Password: " + (u.getPassword() != null ? "[PRESENT]" : "NULL"));
        });
        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }

    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Obecne hasło jest nieprawidłowe");
        }

        String encoded = passwordEncoder.encode(newPassword);
        user.setPassword(encoded);
        userRepository.save(user);
    }

    public User updateUser(UUID id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (!existingUser.getClass().equals(userDetails.getClass())) {
            throw new IllegalArgumentException("Cannot change user type from " +
                    existingUser.getClass().getSimpleName() + " to " +
                    userDetails.getClass().getSimpleName());
        }

        existingUser.setLogin(userDetails.getLogin());
        existingUser.setName(userDetails.getName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setActive(userDetails.isActive());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (existingUser instanceof Administrator && userDetails instanceof Administrator) {
            ((Administrator) existingUser).setDepartment(((Administrator) userDetails).getDepartment());
        } else if (existingUser instanceof ResourceManager && userDetails instanceof ResourceManager) {
            ((ResourceManager) existingUser).setManagedResourceType(((ResourceManager) userDetails).getManagedResourceType());
        } else if (existingUser instanceof Customer && userDetails instanceof Customer) {
            ((Customer) existingUser).setPhoneNumber(((Customer) userDetails).getPhoneNumber());
            ((Customer) existingUser).setAddress(((Customer) userDetails).getAddress());
        }

        validateUser(existingUser);
        return userRepository.save(existingUser);
    }

    public void activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(true);
        userRepository.save(user);
    }

    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public Optional<User> getUserByLoginExact(String login) {
        System.out.println("=== UserManager.getUserByLoginExact ===");
        System.out.println("Looking for user: " + login);
        Optional<User> user = userRepository.findByLoginExact(login);
        if (user.isPresent()) {
            System.out.println("User found: " + user.get().getLogin());
            System.out.println("User password in DB: " + (user.get().getPassword() != null ? "[PRESENT]" : "NULL"));
        } else {
            System.out.println("User NOT found: " + login);
        }
        return user;
    }

    public Optional<User> getUserByLoginExactSafe(String login) {
        Optional<User> user = userRepository.findByLoginExact(login);
        return user;
    }

    public List<User> getUsersByLoginContaining(String loginPart) {
        List<User> users = userRepository.findByLoginContaining(loginPart);
        return users;
    }

    public List<User> getAllUsersWithPasswords() {
        return userRepository.findAll();
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Login is required");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}