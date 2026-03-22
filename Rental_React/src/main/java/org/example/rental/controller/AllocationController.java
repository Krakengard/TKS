package org.example.rental.controller;

import org.example.rental.exception.BadRequestException;
import org.example.rental.exception.ConflictException;
import org.example.rental.exception.ResourceNotFoundException;
import org.example.rental.exception.UnauthorizedException;
import org.example.rental.model.Allocation;
import org.example.rental.model.User;
import org.example.rental.repository.AllocationRepository;
import org.example.rental.service.AllocationManager;
import org.example.rental.service.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    @Autowired
    private AllocationManager allocationManager;

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private UserManager userManager;  // Dodaj ten autowired

    @GetMapping
    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Allocation getAllocationById(@PathVariable UUID id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found with id: " + id));
    }

    @PostMapping
    public Allocation createAllocation(
            @RequestParam UUID customerId,
            @RequestParam UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey,
            Authentication authentication) {

        String loggedInUsername = authentication.getName();

        User requestedCustomer = userManager.getUserById(customerId)
                .orElseThrow(() -> new BadRequestException("Customer not found with id: " + customerId));

        if (!requestedCustomer.getLogin().equals(loggedInUsername)) {
            boolean isAdminOrManager = authentication.getAuthorities().stream()
                    .anyMatch(auth ->
                            auth.getAuthority().equals("ROLE_ADMIN") ||
                                    auth.getAuthority().equals("ROLE_MANAGER")
                    );

            if (!isAdminOrManager) {
                throw new UnauthorizedException(
                        "You can only create allocations for yourself. " +
                                "Logged in as: " + loggedInUsername + ", " +
                                "Trying to create for: " + requestedCustomer.getLogin()
                );
            }
        }

        if (idempotencyKey != null) {
            Optional<Allocation> existingAllocation = allocationRepository.findByIdempotencyKey(idempotencyKey);
            if (existingAllocation.isPresent()) {
                throw new ConflictException(
                        "Allocation with this idempotency key already exists: " + existingAllocation.get().getId()
                );
            }
        }

        try {
            return allocationManager.createAllocation(customerId, resourceId, startTime, endTime, idempotencyKey);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public void completeAllocation(@PathVariable UUID id) {
        try {
            allocationManager.completeAllocation(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Allocation not found with id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteAllocation(@PathVariable UUID id) {
        try {
            allocationManager.deleteAllocation(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Allocation not found with id: " + id);
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/past")
    public List<Allocation> getPastAllocationsByCustomer(@PathVariable UUID customerId) {
        return allocationManager.getPastAllocationsByCustomer(customerId);
    }

    @GetMapping("/customer/{customerId}/current")
    public List<Allocation> getCurrentAllocationsByCustomer(@PathVariable UUID customerId) {
        return allocationManager.getCurrentAllocationsByCustomer(customerId);
    }

    @GetMapping("/resource/{resourceId}/past")
    public List<Allocation> getPastAllocationsByResource(@PathVariable UUID resourceId) {
        return allocationManager.getPastAllocationsByResource(resourceId);
    }

    @GetMapping("/resource/{resourceId}/current")
    public List<Allocation> getCurrentAllocationsByResource(@PathVariable UUID resourceId) {
        return allocationManager.getCurrentAllocationsByResource(resourceId);
    }
}