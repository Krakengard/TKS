package org.example.rental.port.out;

import org.example.rental.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAll();

    Optional<User> findByLoginExact(String login);

    List<User> findByLoginContaining(String loginPart);
}
