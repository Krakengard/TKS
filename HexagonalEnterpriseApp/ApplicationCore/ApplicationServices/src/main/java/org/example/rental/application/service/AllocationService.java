package org.example.rental.application.service;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ConflictException;
import org.example.rental.domain.exception.ResourceNotFoundException;
import org.example.rental.domain.model.Allocation;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.Resource;
import org.example.rental.domain.model.User;
import org.example.rental.port.in.AllocationUseCase;
import org.example.rental.port.out.AllocationRepository;
import org.example.rental.port.out.ResourceRepository;
import org.example.rental.port.out.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AllocationService implements AllocationUseCase {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    private final ConcurrentMap<UUID, Object> resourceLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> allocationLocks = new ConcurrentHashMap<>();

    @Override
    public Allocation createAllocation(UUID customerId, UUID resourceId,
                                       LocalDateTime startTime, LocalDateTime endTime,
                                       UUID idempotencyKey) {
        String lockKey = customerId + "_" + resourceId;

        synchronized (getLock(allocationLocks, lockKey)) {
            synchronized (getLock(resourceLocks, resourceId)) {

                User user = userRepository.findById(customerId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Customer not found with id: " + customerId));

                if (!(user instanceof Customer customer)) {
                    throw new BadRequestException("User is not a customer: " + customerId);
                }

                if (!customer.isActive()) {
                    throw new BadRequestException("Customer is not active");
                }

                Resource resource = resourceRepository.findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Resource not found with id: " + resourceId));

                LocalDateTime now = LocalDateTime.now();
                if (startTime.isBefore(now.minusHours(1))) {
                    throw new BadRequestException("Start time cannot be more than 1 hour in the past");
                }
                if (endTime != null && endTime.isBefore(startTime)) {
                    throw new BadRequestException("End time cannot be before start time");
                }

                if (idempotencyKey != null) {
                    Optional<Allocation> existing = allocationRepository.findByIdempotencyKey(idempotencyKey);
                    if (existing.isPresent()) {
                        throw new ConflictException("Allocation with this idempotency key already exists: "
                                + existing.get().getId());
                    }
                }

                if (!allocationRepository.findConflictingAllocations(resourceId, startTime, endTime).isEmpty()) {
                    throw new ConflictException("Resource is not available for the requested time period");
                }

                Allocation allocation = new Allocation(customer, resource, startTime, endTime);
                if (idempotencyKey != null) {
                    allocation.setIdempotencyKey(idempotencyKey);
                }
                return allocationRepository.save(allocation);
            }
        }
    }

    @Override
    public void completeAllocation(UUID allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation not found with id: " + allocationId));
        allocation.setCompleted(true);
        allocationRepository.save(allocation);
    }

    @Override
    public void deleteAllocation(UUID allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation not found with id: " + allocationId));
        if (allocation.isCompleted()) {
            throw new ConflictException("Cannot delete completed allocation");
        }
        allocationRepository.delete(allocationId);
    }

    @Override
    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    @Override
    public Optional<Allocation> getAllocationById(UUID id) {
        return allocationRepository.findById(id);
    }

    @Override
    public List<Allocation> getPastAllocationsByCustomer(UUID customerId) {
        return allocationRepository.findPastAllocationsByCustomerId(customerId);
    }

    @Override
    public List<Allocation> getCurrentAllocationsByCustomer(UUID customerId) {
        return allocationRepository.findCurrentAllocationsByCustomerId(customerId);
    }

    @Override
    public List<Allocation> getPastAllocationsByResource(UUID resourceId) {
        return allocationRepository.findByResourceId(resourceId).stream()
                .filter(Allocation::isCompleted)
                .toList();
    }

    @Override
    public List<Allocation> getCurrentAllocationsByResource(UUID resourceId) {
        return allocationRepository.findCurrentAllocationsByResourceId(resourceId);
    }

    private <K> Object getLock(ConcurrentMap<K, Object> map, K key) {
        return map.computeIfAbsent(key, k -> new Object());
    }
}
