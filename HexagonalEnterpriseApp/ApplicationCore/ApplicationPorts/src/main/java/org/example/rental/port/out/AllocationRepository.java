package org.example.rental.port.out;

import org.example.rental.domain.model.Allocation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface AllocationRepository {

    Allocation save(Allocation allocation);

    Optional<Allocation> findById(UUID id);

    List<Allocation> findAll();

    void delete(UUID id);

    List<Allocation> findConflictingAllocations(UUID resourceId,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime);

    List<Allocation> findPastAllocationsByCustomerId(UUID customerId);

    List<Allocation> findCurrentAllocationsByCustomerId(UUID customerId);

    List<Allocation> findByResourceId(UUID resourceId);

    List<Allocation> findCurrentAllocationsByResourceId(UUID resourceId);

    boolean hasActiveAllocationsForResource(UUID resourceId);

    Optional<Allocation> findByIdempotencyKey(UUID idempotencyKey);
}
