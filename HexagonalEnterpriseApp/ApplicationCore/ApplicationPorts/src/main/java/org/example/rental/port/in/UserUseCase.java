package org.example.rental.port.in;

import org.example.rental.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserUseCase {

    User createUser(User user);

    Optional<User> getUserById(UUID id);

    List<User> getAllUsers();

    User updateUser(UUID id, User userDetails);

    void changePassword(UUID userId, String currentPassword, String newPassword);

    void activateUser(UUID id);

    void deactivateUser(UUID id);

    Optional<User> getUserByLogin(String login);

    List<User> getUsersByLoginContaining(String loginPart);
}
