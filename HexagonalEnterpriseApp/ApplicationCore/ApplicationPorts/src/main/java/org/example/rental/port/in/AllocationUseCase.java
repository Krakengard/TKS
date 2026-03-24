package org.example.rental.port.in;

import org.example.rental.domain.model.Allocation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface AllocationUseCase {

    Allocation createAllocation(UUID customerId,
                                UUID resourceId,
                                LocalDateTime startTime,
                                LocalDateTime endTime,
                                UUID idempotencyKey);

    void completeAllocation(UUID allocationId);

    void deleteAllocation(UUID allocationId);

    List<Allocation> getAllAllocations();

    Optional<Allocation> getAllocationById(UUID id);

    List<Allocation> getPastAllocationsByCustomer(UUID customerId);

    List<Allocation> getCurrentAllocationsByCustomer(UUID customerId);

    List<Allocation> getPastAllocationsByResource(UUID resourceId);

    List<Allocation> getCurrentAllocationsByResource(UUID resourceId);
}
