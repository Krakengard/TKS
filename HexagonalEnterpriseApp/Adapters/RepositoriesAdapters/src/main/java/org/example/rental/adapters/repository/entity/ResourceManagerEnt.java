package org.example.rental.adapters.repository.entity;

public class ResourceManagerEnt extends UserEnt {

    private String managedResourceType;

    public String getManagedResourceType() { return managedResourceType; }
    public void setManagedResourceType(String managedResourceType) {
        this.managedResourceType = managedResourceType;
    }
}