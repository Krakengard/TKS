package org.example.rental.adapters.repository.adapter;

import org.example.rental.adapters.repository.entity.AllocationEnt;
import org.example.rental.ports.output.AllocationRepositoryPort;
import org.example.rental.domain.model.*;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AllocationRepositoryAdapter implements AllocationRepositoryPort {

    private final Map<UUID, AllocationEnt> allocations = new ConcurrentHashMap<>();


    @Override
    public Allocation save(Allocation allocation) {

        AllocationEnt entity = toEntity(allocation);

        allocations.put(entity.getId(), entity);

        return toDomain(entity);
    }


    @Override
    public Optional<Allocation> findById(UUID id) {

        AllocationEnt entity = allocations.get(id);

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(entity));
    }


    @Override
    public List<Allocation> findAll() {

        List<Allocation> result = new ArrayList<>();

        for (AllocationEnt ent : allocations.values()) {
            result.add(toDomain(ent));
        }

        return result;
    }


    @Override
    public void delete(UUID id) {
        allocations.remove(id);
    }


    @Override
    public List<Allocation> findConflictingAllocations(UUID resourceId,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {

        List<Allocation> result = new ArrayList<>();

        for (AllocationEnt ent : allocations.values()) {

            if (!ent.getResourceId().equals(resourceId)) {
                continue;
            }

            if (ent.isCompleted()) {
                continue;
            }

            if (isOverlapping(startTime, endTime, ent.getStartTime(), ent.getEndTime())) {
                result.add(toDomain(ent));
            }
        }

        return result;
    }


    private boolean isOverlapping(LocalDateTime start1,
                                  LocalDateTime end1,
                                  LocalDateTime start2,
                                  LocalDateTime end2) {

        LocalDateTime effectiveEnd1 = end1 != null ? end1 : LocalDateTime.MAX;
        LocalDateTime effectiveEnd2 = end2 != null ? end2 : LocalDateTime.MAX;

        return start1.isBefore(effectiveEnd2) && effectiveEnd1.isAfter(start2);
    }


    private AllocationEnt toEntity(Allocation allocation) {

        AllocationEnt entity = new AllocationEnt();

        entity.setId(allocation.getId());
        entity.setIdempotencyKey(allocation.getIdempotencyKey());

        entity.setCustomerId(allocation.getCustomer().getId());
        entity.setResourceId(allocation.getResource().getId());

        entity.setStartTime(allocation.getStartTime());
        entity.setEndTime(allocation.getEndTime());
        entity.setCompleted(allocation.isCompleted());

        return entity;
    }


    private Allocation toDomain(AllocationEnt entity) {

        Allocation allocation = new Allocation();

        allocation.setId(entity.getId());
        allocation.setIdempotencyKey(entity.getIdempotencyKey());
        allocation.setStartTime(entity.getStartTime());
        allocation.setEndTime(entity.getEndTime());
        allocation.setCompleted(entity.isCompleted());

        return allocation;
    }
}