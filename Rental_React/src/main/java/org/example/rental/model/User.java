package org.example.rental.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Administrator.class, name = "administrator"),
        @JsonSubTypes.Type(value = ResourceManager.class, name = "resourceManager"),
        @JsonSubTypes.Type(value = Customer.class, name = "customer")
})
public abstract class User implements UserDetails {

    private UUID id;
    private String login;

    @JsonIgnore
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

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        System.out.println("=== User.setPassword ===");
        System.out.println("Setting password for user: " + this.login);
        System.out.println("Password value: " + (password != null ? "[PRESENT, length: " + password.length() + "]" : "NULL"));
        this.password = password;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // === UserDetails ===
    @Override
    public String getUsername() {
        return this.login;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role;

        if (this instanceof Administrator) {
            role = "ROLE_ADMIN";
        } else if (this instanceof ResourceManager) {
            role = "ROLE_MANAGER";
        } else if (this instanceof Customer) {
            role = "ROLE_CUSTOMER";
        } else {
            // To się nie powinno zdarzyć – ale zabezpieczamy
            role = "ROLE_USER";
            // ewentualnie: throw new IllegalStateException("Unknown user type: " + this.getClass().getName());
        }

        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return this.active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
