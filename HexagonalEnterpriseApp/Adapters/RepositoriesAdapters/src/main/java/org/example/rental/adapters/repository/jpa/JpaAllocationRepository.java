package org.example.rental.adapters.repository.jpa;

import org.example.rental.domain.model.Allocation;
import org.example.rental.port.out.AllocationRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class JpaAllocationRepository implements AllocationRepository {

    private final Map<UUID, Allocation> allocations = new ConcurrentHashMap<>();

    @Override
    public Allocation save(Allocation allocation) {
        allocations.put(allocation.getId(), allocation);
        return allocation;
    }

    @Override
    public Optional<Allocation> findById(UUID id) {
        return Optional.ofNullable(allocations.get(id));
    }

    @Override
    public List<Allocation> findAll() {
        return new ArrayList<>(allocations.values());
    }

    @Override
    public void delete(UUID id) {
        allocations.remove(id);
    }

    @Override
    public List<Allocation> findConflictingAllocations(UUID resourceId,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {
        return allocations.values().stream()
                .filter(a -> a.getResource().getId().equals(resourceId))
                .filter(a -> !a.isCompleted())
                .filter(a -> isTimeOverlapping(startTime, endTime, a.getStartTime(), a.getEndTime()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Allocation> findPastAllocationsByCustomerId(UUID customerId) {
        LocalDateTime now = LocalDateTime.now();
        return allocations.values().stream()
                .filter(a -> a.getCustomer().getId().equals(customerId))
                .filter(a -> a.isCompleted() ||
                        (a.getEndTime() != null && a.getEndTime().isBefore(now)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Allocation> findCurrentAllocationsByCustomerId(UUID customerId) {
        LocalDateTime now = LocalDateTime.now();
        return allocations.values().stream()
                .filter(a -> a.getCustomer().getId().equals(customerId))
                .filter(a -> !a.isCompleted())
                .filter(a -> a.getStartTime().isBefore(now))
                .filter(a -> a.getEndTime() == null || a.getEndTime().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public List<Allocation> findByResourceId(UUID resourceId) {
        return allocations.values().stream()
                .filter(a -> a.getResource().getId().equals(resourceId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Allocation> findCurrentAllocationsByResourceId(UUID resourceId) {
        LocalDateTime now = LocalDateTime.now();
        return allocations.values().stream()
                .filter(a -> a.getResource().getId().equals(resourceId))
                .filter(a -> !a.isCompleted())
                .filter(a -> a.getStartTime().isBefore(now))
                .filter(a -> a.getEndTime() == null || a.getEndTime().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasActiveAllocationsForResource(UUID resourceId) {
        return allocations.values().stream()
                .anyMatch(a -> a.getResource().getId().equals(resourceId) && !a.isCompleted());
    }

    @Override
    public Optional<Allocation> findByIdempotencyKey(UUID idempotencyKey) {
        return allocations.values().stream()
                .filter(a -> idempotencyKey.equals(a.getIdempotencyKey()))
                .findFirst();
    }

    private boolean isTimeOverlapping(LocalDateTime start1, LocalDateTime end1,
                                      LocalDateTime start2, LocalDateTime end2) {
        LocalDateTime effectiveEnd1 = end1 != null ? end1 : LocalDateTime.MAX;
        LocalDateTime effectiveEnd2 = end2 != null ? end2 : LocalDateTime.MAX;
        return start1.isBefore(effectiveEnd2) && effectiveEnd1.isAfter(start2);
    }
}
