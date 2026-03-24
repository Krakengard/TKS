package org.example.rental.application.service;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ResourceNotFoundException;
import org.example.rental.domain.model.*;
import org.example.rental.port.in.UserUseCase;
import org.example.rental.port.out.PasswordEncoderPort;
import org.example.rental.port.out.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserUseCase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoderPort passwordEncoder;

    @Override
    public User createUser(User user) {
        validateUser(user);
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(UUID id, User userDetails) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!existing.getClass().equals(userDetails.getClass())) {
            throw new BadRequestException("Cannot change user type from "
                    + existing.getClass().getSimpleName() + " to "
                    + userDetails.getClass().getSimpleName());
        }

        existing.setLogin(userDetails.getLogin());
        existing.setName(userDetails.getName());
        existing.setEmail(userDetails.getEmail());
        existing.setActive(userDetails.isActive());

        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (existing instanceof Administrator a && userDetails instanceof Administrator ud) {
            a.setDepartment(ud.getDepartment());
        } else if (existing instanceof ResourceManager a && userDetails instanceof ResourceManager ud) {
            a.setManagedResourceType(ud.getManagedResourceType());
        } else if (existing instanceof Customer a && userDetails instanceof Customer ud) {
            a.setPhoneNumber(ud.getPhoneNumber());
            a.setAddress(ud.getAddress());
        }

        validateUser(existing);
        return userRepository.save(existing);
    }

    @Override
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Obecne hasło jest nieprawidłowe");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public Optional<User> getUserByLogin(String login) {
        return userRepository.findByLoginExact(login);
    }

    @Override
    public List<User> getUsersByLoginContaining(String loginPart) {
        return userRepository.findByLoginContaining(loginPart);
    }

    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            throw new BadRequestException("Login is required");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new BadRequestException("Name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
    }
}
