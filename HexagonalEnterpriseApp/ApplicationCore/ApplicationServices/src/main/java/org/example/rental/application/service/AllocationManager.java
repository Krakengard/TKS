package org.example.rental.application.service;

import org.example.rental.domain.model.Allocation;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.Resource;
import org.example.rental.domain.model.User;
import org.example.rental.repository.AllocationRepository;
import org.example.rental.repository.UserRepository;
import org.example.rental.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AllocationManager {

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    private final ConcurrentMap<UUID, Object> resourceLocks = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Object> allocationLocks = new ConcurrentHashMap<>();

    public Allocation createAllocation(UUID customerId, UUID resourceId, LocalDateTime startTime, LocalDateTime endTime, UUID idempotencyKey) {
        String allocationLockKey = customerId.toString() + "_" + resourceId.toString();

        synchronized (getAllocationLock(allocationLockKey)) {
            synchronized (getResourceLock(resourceId)) {
                User user = userRepository.findById(customerId)
                        .orElseThrow(() -> new IllegalArgumentException("Customer not found with id: " + customerId));

                if (!(user instanceof Customer)) {
                    throw new IllegalArgumentException("User is not a customer: " + customerId);
                }

                Customer customer = (Customer) user;

                if (!customer.isActive()) {
                    throw new IllegalStateException("Customer is not active");
                }

                Resource resource = resourceRepository.findById(resourceId)
                        .orElseThrow(() -> new IllegalArgumentException("Resource not found with id: " + resourceId));

                LocalDateTime now = LocalDateTime.now();

                if (startTime.isBefore(now.minusHours(1))) {
                    throw new IllegalArgumentException("Start time cannot be more than 1 hour in the past");
                }

                if (endTime != null && endTime.isBefore(startTime)) {
                    throw new IllegalArgumentException("End time cannot be before start time");
                }

                if (!isResourceAvailable(resourceId, startTime, endTime)) {
                    throw new IllegalStateException("Resource is not available for the requested time period");
                }

                Allocation allocation = new Allocation(customer, resource, startTime, endTime);

                if (idempotencyKey != null) {
                    allocation.setIdempotencyKey(idempotencyKey);
                }

                return allocationRepository.save(allocation);
            }
        }
    }

    private boolean isResourceAvailable(UUID resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Allocation> conflictingAllocations = allocationRepository.findConflictingAllocations(resourceId, startTime, endTime);
        return conflictingAllocations.isEmpty();
    }

    public void completeAllocation(UUID allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new IllegalArgumentException("Allocation not found with id: " + allocationId));

        allocation.setCompleted(true);
        allocationRepository.save(allocation);
    }

    public void deleteAllocation(UUID allocationId) {
        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new IllegalArgumentException("Allocation not found with id: " + allocationId));

        if (allocation.isCompleted()) {
            throw new IllegalStateException("Cannot delete completed allocation");
        }

        allocationRepository.delete(allocationId);
    }

    public List<Allocation> getPastAllocationsByCustomer(UUID customerId) {
        return allocationRepository.findPastAllocationsByCustomerId(customerId);
    }

    public List<Allocation> getCurrentAllocationsByCustomer(UUID customerId) {
        return allocationRepository.findCurrentAllocationsByCustomerId(customerId);
    }

    public List<Allocation> getPastAllocationsByResource(UUID resourceId) {
        return allocationRepository.findByResourceId(resourceId).stream()
                .filter(Allocation::isCompleted)
                .toList();
    }

    public List<Allocation> getCurrentAllocationsByResource(UUID resourceId) {
        return allocationRepository.findCurrentAllocationsByResourceId(resourceId);
    }

    private Object getResourceLock(UUID resourceId) {
        return resourceLocks.computeIfAbsent(resourceId, k -> new Object());
    }

    private Object getAllocationLock(String lockKey) {
        return allocationLocks.computeIfAbsent(lockKey, k -> new Object());
    }
}