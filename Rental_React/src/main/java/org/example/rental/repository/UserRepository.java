package org.example.rental.repository;

import org.example.rental.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class UserRepository {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final Map<String, User> usersByLogin = new ConcurrentHashMap<>();

    public User save(User user) {
        System.out.println("=== UserRepository.save ===");
        System.out.println("User login: " + user.getLogin());
        System.out.println("User ID: " + user.getId());
        System.out.println("User class: " + user.getClass().getSimpleName());
        System.out.println("User password at save: " + (user.getPassword() != null ? "[PRESENT, length: " + user.getPassword().length() + "]" : "NULL"));

        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be null or empty");
        }

        if (usersByLogin.containsKey(user.getLogin())) {
            User existingUser = usersByLogin.get(user.getLogin());
            if (!existingUser.getId().equals(user.getId())) {
                throw new IllegalArgumentException("User with login '" + user.getLogin() + "' already exists");
            }
        }

        users.put(user.getId(), user);
        usersByLogin.put(user.getLogin(), user);

        // Weryfikacja: sprawdź czy zapisany user ma hasło
        User savedUser = users.get(user.getId());
        System.out.println("Saved user verification - password: " + (savedUser.getPassword() != null ? "[PRESENT]" : "NULL"));

        return savedUser;
    }

    public Optional<User> findById(UUID id) {
        User user = users.get(id);
        if (user != null) {
            System.out.println("=== UserRepository.findById ===");
            System.out.println("Found user: " + user.getLogin());
            System.out.println("Retrieved password: " + (user.getPassword() != null ? "[PRESENT]" : "NULL"));
        }
        return Optional.ofNullable(user);
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> findByLoginExact(String login) {
        User user = usersByLogin.get(login);
        if (user != null) {
            System.out.println("=== UserRepository.findByLoginExact ===");
            System.out.println("Found user: " + user.getLogin());
            System.out.println("Retrieved password: " + (user.getPassword() != null ? "[PRESENT]" : "NULL"));
        }
        return Optional.ofNullable(user);
    }

    public List<User> findByLoginContaining(String loginPart) {
        return usersByLogin.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains(loginPart.toLowerCase()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        User user = users.get(id);
        if (user != null) {
            usersByLogin.remove(user.getLogin());
            users.remove(id);
        }
    }



    public boolean existsByLogin(String login) {
        return usersByLogin.containsKey(login);
    }
}