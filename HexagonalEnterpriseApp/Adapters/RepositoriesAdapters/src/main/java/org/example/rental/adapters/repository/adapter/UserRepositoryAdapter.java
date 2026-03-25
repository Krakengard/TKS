package org.example.rental.adapters.repository.adapter;

import org.example.rental.adapters.repository.entity.*;
import org.example.rental.ports.output.UserRepositoryPort;

import org.example.rental.domain.model.*;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final Map<UUID, UserEnt> users = new ConcurrentHashMap<>();
    private final Map<String, UserEnt> usersByLogin = new ConcurrentHashMap<>();


    @Override
    public User save(User user) {

        UserEnt entity = toEntity(user);

        users.put(entity.getId(), entity);
        usersByLogin.put(entity.getLogin(), entity);

        return toDomain(entity);
    }


    @Override
    public Optional<User> findById(UUID id) {

        UserEnt entity = users.get(id);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(entity));
    }


    @Override
    public List<User> findAll() {

        List<User> result = new ArrayList<>();

        for (UserEnt ent : users.values()) {
            result.add(toDomain(ent));
        }

        return result;
    }


    @Override
    public Optional<User> findByLoginExact(String login) {

        UserEnt entity = usersByLogin.get(login);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(entity));
    }


    @Override
    public void delete(UUID id) {

        UserEnt entity = users.get(id);

        if (entity != null) {
            users.remove(id);
            usersByLogin.remove(entity.getLogin());
        }
    }


    @Override
    public boolean existsByLogin(String login) {
        return usersByLogin.containsKey(login);
    }


    private UserEnt toEntity(User user) {

        UserEnt entity;

        if (user instanceof Administrator admin) {

            AdministratorEnt ent = new AdministratorEnt();
            ent.setDepartment(admin.getDepartment());
            entity = ent;

        } else if (user instanceof Customer customer) {

            CustomerEnt ent = new CustomerEnt();
            ent.setPhoneNumber(customer.getPhoneNumber());
            ent.setAddress(customer.getAddress());
            entity = ent;

        } else if (user instanceof ResourceManager manager) {

            ResourceManagerEnt ent = new ResourceManagerEnt();
            ent.setManagedResourceType(manager.getManagedResourceType());
            entity = ent;

        } else {
            throw new IllegalArgumentException("Unknown user type");
        }

        entity.setId(user.getId());
        entity.setLogin(user.getLogin());
        entity.setPassword(user.getPassword());
        entity.setActive(user.isActive());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());

        return entity;
    }


    private User toDomain(UserEnt entity) {

        User user;

        if (entity instanceof AdministratorEnt adminEnt) {

            Administrator admin = new Administrator();
            admin.setDepartment(adminEnt.getDepartment());
            user = admin;

        } else if (entity instanceof CustomerEnt custEnt) {

            Customer customer = new Customer();
            customer.setPhoneNumber(custEnt.getPhoneNumber());
            customer.setAddress(custEnt.getAddress());
            user = customer;

        } else if (entity instanceof ResourceManagerEnt managerEnt) {

            ResourceManager manager = new ResourceManager();
            manager.setManagedResourceType(managerEnt.getManagedResourceType());
            user = manager;

        } else {
            throw new IllegalArgumentException("Unknown entity type");
        }

        user.setId(entity.getId());
        user.setLogin(entity.getLogin());
        user.setPassword(entity.getPassword());
        user.setActive(entity.isActive());
        user.setName(entity.getName());
        user.setEmail(entity.getEmail());

        return user;
    }
}