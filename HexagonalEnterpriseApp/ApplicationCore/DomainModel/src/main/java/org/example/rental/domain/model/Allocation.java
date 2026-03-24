package org.example.rental.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Allocation {
    private UUID id;
    private UUID idempotencyKey;
    private Customer customer;
    private Resource resource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;

    public Allocation() {
        this.id = UUID.randomUUID();
        this.completed = false;
        this.idempotencyKey = UUID.randomUUID();
    }

    public Allocation(Customer customer, Resource resource, LocalDateTime startTime, LocalDateTime endTime) {
        this();
        this.customer = customer;
        this.resource = resource;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(UUID idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Allocation that = (Allocation) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}