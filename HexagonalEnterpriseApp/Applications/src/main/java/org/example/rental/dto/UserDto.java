package org.example.rental.dto;

// src/main/java/org/example/rental/dto/UserDto.java

import java.util.UUID;

public class UserDto {
    private UUID id;
    private String login;
    private String password; // включаем только если нужен при регистрации или смене
    private String name;
    private String email;
    private boolean active;

    public UserDto() {}

    public UserDto(UUID id, String login, String password, String name, String email, boolean active) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.name = name;
        this.email = email;
        this.active = active;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}