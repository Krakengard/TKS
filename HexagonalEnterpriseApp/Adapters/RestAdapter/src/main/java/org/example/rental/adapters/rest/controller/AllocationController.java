package org.example.rental.adapters.rest.controller;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ConflictException;
import org.example.rental.domain.exception.ResourceNotFoundException;
import org.example.rental.domain.exception.UnauthorizedException;
import org.example.rental.domain.model.Allocation;
import org.example.rental.domain.model.User;
import org.example.rental.port.in.AllocationUseCase;
import org.example.rental.port.in.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    @Autowired
    private AllocationUseCase allocationUseCase;

    @Autowired
    private UserUseCase userUseCase;

    @GetMapping
    public List<Allocation> getAllAllocations() {
        return allocationUseCase.getAllAllocations();
    }

    @GetMapping("/{id}")
    public Allocation getAllocationById(@PathVariable UUID id) {
        return allocationUseCase.getAllocationById(id)
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

        User requestedCustomer = userUseCase.getUserById(customerId)
                .orElseThrow(() -> new BadRequestException("Customer not found with id: " + customerId));

        if (!requestedCustomer.getLogin().equals(loggedInUsername)) {
            boolean isAdminOrManager = authentication.getAuthorities().stream()
                    .anyMatch(auth ->
                            auth.getAuthority().equals("ROLE_ADMIN") ||
                                    auth.getAuthority().equals("ROLE_MANAGER"));

            if (!isAdminOrManager) {
                throw new UnauthorizedException(
                        "You can only create allocations for yourself. " +
                                "Logged in as: " + loggedInUsername + ", " +
                                "Trying to create for: " + requestedCustomer.getLogin());
            }
        }

        try {
            // Sprawdzanie idempotency key i logika biznesowa — w serwisie, nie tutaj
            return allocationUseCase.createAllocation(
                    customerId, resourceId, startTime, endTime, idempotencyKey);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public void completeAllocation(@PathVariable UUID id) {
        try {
            allocationUseCase.completeAllocation(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Allocation not found with id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public void deleteAllocation(@PathVariable UUID id) {
        try {
            allocationUseCase.deleteAllocation(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Allocation not found with id: " + id);
        } catch (IllegalStateException e) {
            throw new ConflictException(e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/past")
    public List<Allocation> getPastAllocationsByCustomer(@PathVariable UUID customerId) {
        return allocationUseCase.getPastAllocationsByCustomer(customerId);
    }

    @GetMapping("/customer/{customerId}/current")
    public List<Allocation> getCurrentAllocationsByCustomer(@PathVariable UUID customerId) {
        return allocationUseCase.getCurrentAllocationsByCustomer(customerId);
    }

    @GetMapping("/resource/{resourceId}/past")
    public List<Allocation> getPastAllocationsByResource(@PathVariable UUID resourceId) {
        return allocationUseCase.getPastAllocationsByResource(resourceId);
    }

    @GetMapping("/resource/{resourceId}/current")
    public List<Allocation> getCurrentAllocationsByResource(@PathVariable UUID resourceId) {
        return allocationUseCase.getCurrentAllocationsByResource(resourceId);
    }
}
