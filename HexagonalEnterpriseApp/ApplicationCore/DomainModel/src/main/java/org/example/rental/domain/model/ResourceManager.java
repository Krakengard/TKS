package org.example.rental.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;


public class ResourceManager extends User {
    private String managedResourceType;

    public ResourceManager() {
        super();
    }

    @JsonCreator
    public ResourceManager( String login,
                           String name,
                           String email,
                           String managedResourceType) {
        super(login, name, email);
        this.managedResourceType = managedResourceType;
    }



    // NIE nadpisuj setPassword() ani getPassword() tutaj!

    public String getManagedResourceType() { return managedResourceType; }
    public void setManagedResourceType(String managedResourceType) { this.managedResourceType = managedResourceType; }
}