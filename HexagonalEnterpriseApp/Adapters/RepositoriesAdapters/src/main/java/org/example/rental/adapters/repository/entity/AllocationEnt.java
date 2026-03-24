package org.example.rental.adapters.repository.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class AllocationEnt {

    private UUID id;
    private UUID idempotencyKey;

    private UUID customerId;
    private UUID resourceId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean completed;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(UUID idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
