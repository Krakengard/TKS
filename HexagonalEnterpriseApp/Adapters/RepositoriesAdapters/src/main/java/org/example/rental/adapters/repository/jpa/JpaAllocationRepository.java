package org.example.rental.adapters.repository.jpa;

import org.example.rental.adapters.repository.entity.AllocationEnt;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class JpaAllocationRepository {

    private final Map<UUID, AllocationEnt> allocations = new ConcurrentHashMap<>();


    public AllocationEnt save(AllocationEnt allocation) {

        allocations.put(allocation.getId(), allocation);
        return allocation;
    }


    public Optional<AllocationEnt> findById(UUID id) {
        return Optional.ofNullable(allocations.get(id));
    }


    public List<AllocationEnt> findAll() {
        return new ArrayList<>(allocations.values());
    }


    public List<AllocationEnt> findByCustomerId(UUID customerId) {

        return allocations.values().stream()
                .filter(a -> a.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }


    public List<AllocationEnt> findByResourceId(UUID resourceId) {

        return allocations.values().stream()
                .filter(a -> a.getResourceId().equals(resourceId))
                .collect(Collectors.toList());
    }


    public List<AllocationEnt> findConflictingAllocations(UUID resourceId,
                                                          LocalDateTime startTime,
                                                          LocalDateTime endTime) {

        return allocations.values().stream()
                .filter(a -> a.getResourceId().equals(resourceId))
                .filter(a -> !a.isCompleted())
                .filter(a -> isOverlapping(startTime, endTime,
                        a.getStartTime(), a.getEndTime()))
                .collect(Collectors.toList());
    }


    private boolean isOverlapping(LocalDateTime start1,
                                  LocalDateTime end1,
                                  LocalDateTime start2,
                                  LocalDateTime end2) {

        LocalDateTime effectiveEnd1 = end1 != null ? end1 : LocalDateTime.MAX;
        LocalDateTime effectiveEnd2 = end2 != null ? end2 : LocalDateTime.MAX;

        return start1.isBefore(effectiveEnd2) && effectiveEnd1.isAfter(start2);
    }


    public void delete(UUID id) {
        allocations.remove(id);
    }


    public Optional<AllocationEnt> findByIdempotencyKey(UUID key) {

        return allocations.values().stream()
                .filter(a -> key.equals(a.getIdempotencyKey()))
                .findFirst();
    }
}