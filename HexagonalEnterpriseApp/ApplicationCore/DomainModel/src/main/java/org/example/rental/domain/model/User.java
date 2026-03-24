package org.example.rental.domain.model;


import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class User  {

    private UUID id;
    private String login;

    private String password;

    private boolean active;
    private String name;
    private String email;

    public User() {
        this.id = UUID.randomUUID();
        this.active = true;
    }

    public User(String login, String name, String email) {
        this();
        this.login = login;
        this.name = name;
        this.email = email;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }



   /* @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof User)) return false;

        User user = (User) o;

        return this.id != null && this.id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
