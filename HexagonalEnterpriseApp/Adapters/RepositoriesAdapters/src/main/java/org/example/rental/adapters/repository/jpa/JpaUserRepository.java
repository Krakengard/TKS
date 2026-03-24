package org.example.rental.adapters.repository.jpa;

import org.example.rental.adapters.repository.entity.UserEnt;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class JpaUserRepository {

    private final Map<UUID, UserEnt> users = new ConcurrentHashMap<>();
    private final Map<String, UserEnt> usersByLogin = new ConcurrentHashMap<>();


    public UserEnt save(UserEnt user) {

        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            throw new IllegalArgumentException("Login cannot be null or empty");
        }

        if (usersByLogin.containsKey(user.getLogin())) {

            UserEnt existing = usersByLogin.get(user.getLogin());

            if (!existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException("User with login '" + user.getLogin() + "' already exists");
            }
        }

        users.put(user.getId(), user);
        usersByLogin.put(user.getLogin(), user);

        return user;
    }


    public Optional<UserEnt> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }


    public List<UserEnt> findAll() {
        return new ArrayList<>(users.values());
    }


    public Optional<UserEnt> findByLoginExact(String login) {
        return Optional.ofNullable(usersByLogin.get(login));
    }


    public List<UserEnt> findByLoginContaining(String loginPart) {

        return usersByLogin.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains(loginPart.toLowerCase()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }


    public void delete(UUID id) {

        UserEnt user = users.get(id);

        if (user != null) {
            usersByLogin.remove(user.getLogin());
            users.remove(id);
        }
    }


    public boolean existsByLogin(String login) {
        return usersByLogin.containsKey(login);
    }
}