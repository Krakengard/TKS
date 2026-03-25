package org.example.rental.ports.output;

import org.example.rental.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAll();

    Optional<User> findByLoginExact(String login);

    List<User> findByLoginContaining(String loginPart);

    void delete(UUID id);

    boolean existsByLogin(String login);
}