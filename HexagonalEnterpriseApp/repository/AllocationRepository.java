package org.example.rental.repository;

import org.example.rental.model.Allocation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AllocationRepository {
    private final Map<UUID, Allocation> allocations = new ConcurrentHashMap<>();

    public Allocation save(Allocation allocation) {
        allocations.put(allocation.getId(), allocation);
        return allocation;
    }

    public Optional<Allocation> findById(UUID id) {
        return Optional.ofNullable(allocations.get(id));
    }

    public List<Allocation> findAll() {
        return new ArrayList<>(allocations.values());
    }

    public List<Allocation> findByCustomerId(UUID customerId) {
        return allocations.values().stream()
                .filter(allocation -> allocation.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());
    }

    public List<Allocation> findByResourceId(UUID resourceId) {
        return allocations.values().stream()
                .filter(allocation -> allocation.getResource().getId().equals(resourceId))
                .collect(Collectors.toList());
    }

    public List<Allocation> findConflictingAllocations(UUID resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        return allocations.values().stream()
                .filter(allocation -> allocation.getResource().getId().equals(resourceId))
                .filter(allocation -> !allocation.isCompleted()) // Tylko nieukończone alokacje
                .filter(allocation -> isTimeOverlapping(
                        startTime, endTime,
                        allocation.getStartTime(), allocation.getEndTime()
                ))
                .collect(Collectors.toList());
    }

    private boolean isTimeOverlapping(LocalDateTime start1, LocalDateTime end1,
                                      LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime effectiveEnd1 = end1 != null ? end1 : LocalDateTime.MAX;
        LocalDateTime effectiveEnd2 = end2 != null ? end2 : LocalDateTime.MAX;

        return start1.isBefore(effectiveEnd2) && effectiveEnd1.isAfter(start2);
    }

    public List<Allocation> findCurrentAllocationsByResourceId(UUID resourceId) {
        LocalDateTime now = LocalDateTime.now();
        return allocations.values().stream()
                .filter(allocation -> allocation.getResource().getId().equals(resourceId))
                .filter(allocation -> !allocation.isCompleted()) // Tylko nieukończone
                .filter(allocation -> allocation.getStartTime().isBefore(now)) // Już się rozpoczęły
                .filter(allocation -> allocation.getEndTime() == null || allocation.getEndTime().isAfter(now)) // Jeszcze trwają
                .collect(Collectors.toList());
    }

    public List<Allocation> findPastAllocationsByCustomerId(UUID customerId) {
        LocalDateTime now = LocalDateTime.now();
        return allocations.values().stream()
                .filter(allocation -> allocation.getCustomer().getId().equals(customerId))
                .filter(allocation -> allocation.isCompleted() ||
                        (allocation.getEndTime() != null && allocation.getEndTime().isBefore(now)))
                .collect(Collectors.toList());
    }

    public List<Allocation> findCurrentAllocationsByCustomerId(UUID customerId) {
        LocalDateTime now = LocalDateTime.now();
        return allocations.values().stream()
                .filter(allocation -> allocation.getCustomer().getId().equals(customerId))
                .filter(allocation -> !allocation.isCompleted()) // Tylko nieukończone
                .filter(allocation -> allocation.getStartTime().isBefore(now)) // Już się rozpoczęły
                .filter(allocation -> allocation.getEndTime() == null || allocation.getEndTime().isAfter(now)) // Jeszcze trwają
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        allocations.remove(id);
    }

    public boolean hasActiveAllocationsForResource(UUID resourceId) {
        return allocations.values().stream()
                .anyMatch(allocation -> allocation.getResource().getId().equals(resourceId) &&
                        !allocation.isCompleted());
    }

    public Optional<Allocation> findByIdempotencyKey(UUID idempotencyKey) {
        return allocations.values().stream()
                .filter(allocation -> idempotencyKey.equals(allocation.getIdempotencyKey()))
                .findFirst();
    }
}