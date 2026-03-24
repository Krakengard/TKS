package org.example.rental.adapters.repository.jpa;

import org.example.rental.domain.model.User;
import org.example.rental.port.out.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class JpaUserRepository implements UserRepository {

    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final Map<String, User> usersByLogin = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be null or empty");
        }

        if (usersByLogin.containsKey(user.getLogin())) {
            User existing = usersByLogin.get(user.getLogin());
            if (!existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException(
                        "User with login '" + user.getLogin() + "' already exists");
            }
        }

        users.put(user.getId(), user);
        usersByLogin.put(user.getLogin(), user);
        return users.get(user.getId());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findByLoginExact(String login) {
        return Optional.ofNullable(usersByLogin.get(login));
    }

    @Override
    public List<User> findByLoginContaining(String loginPart) {
        return usersByLogin.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains(loginPart.toLowerCase()))
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
}
