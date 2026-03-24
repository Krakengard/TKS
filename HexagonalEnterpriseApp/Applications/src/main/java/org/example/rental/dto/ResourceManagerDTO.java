package org.example.rental.dto;


import java.util.UUID;

public class ResourceManagerDTO {

    private UUID id;
    private String login;
    private String name;
    private String email;
    private String managedResourceType;
    private boolean active;

    public ResourceManagerDTO() {}

    public ResourceManagerDTO(UUID id, String login, String name, String email, String managedResourceType, boolean active) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.email = email;
        this.managedResourceType = managedResourceType;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getManagedResourceType() {
        return managedResourceType;
    }

    public void setManagedResourceType(String managedResourceType) {
        this.managedResourceType = managedResourceType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}