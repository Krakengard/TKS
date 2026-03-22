package org.example.rental.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceManager extends User {
    private String managedResourceType;

    public ResourceManager() {
        super();
    }

    @JsonCreator
    public ResourceManager(@JsonProperty("login") String login,
                           @JsonProperty("name") String name,
                           @JsonProperty("email") String email,
                           @JsonProperty("managedResourceType") String managedResourceType) {
        super(login, name, email);
        this.managedResourceType = managedResourceType;
    }



    // NIE nadpisuj setPassword() ani getPassword() tutaj!

    public String getManagedResourceType() { return managedResourceType; }
    public void setManagedResourceType(String managedResourceType) { this.managedResourceType = managedResourceType; }
}