package org.example.rental.model;

import java.util.UUID;

public class Resource {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private double pricePerHour;

    public Resource() {
        this.id = UUID.randomUUID();
    }

    public Resource(String name, String description, String type, double pricePerHour) {
        this();
        this.name = name;
        this.description = description;
        this.type = type;
        this.pricePerHour = pricePerHour;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) { this.pricePerHour = pricePerHour; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return id.equals(resource.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}