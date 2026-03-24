package org.example.rental.ports.output;

import org.example.rental.domain.model.Allocation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllocationRepositoryPort {

    Allocation save(Allocation allocation);

    Optional<Allocation> findById(UUID id);

    List<Allocation> findAll();

    List<Allocation> findByCustomerId(UUID customerId);

    List<Allocation> findByResourceId(UUID resourceId);

    List<Allocation> findConflictingAllocations(UUID resourceId,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime);

    List<Allocation> findCurrentAllocationsByResourceId(UUID resourceId);

    List<Allocation> findPastAllocationsByCustomerId(UUID customerId);

    List<Allocation> findCurrentAllocationsByCustomerId(UUID customerId);

    void delete(UUID id);

    boolean hasActiveAllocationsForResource(UUID resourceId);

    Optional<Allocation> findByIdempotencyKey(UUID idempotencyKey);
}