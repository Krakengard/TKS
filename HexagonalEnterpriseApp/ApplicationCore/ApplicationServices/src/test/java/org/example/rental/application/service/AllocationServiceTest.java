package org.example.rental.application.service;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ConflictException;
import org.example.rental.domain.model.Allocation;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.Resource;
import org.example.rental.port.out.AllocationRepository;
import org.example.rental.port.out.ResourceRepository;
import org.example.rental.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceAdvancedTest {

    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private AllocationService allocationService;

    private UUID customerId;
    private UUID resourceId;
    private Customer activeCustomer;
    private Resource testResource;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        resourceId = UUID.randomUUID();

        activeCustomer = new Customer();
        activeCustomer.setId(customerId);
        activeCustomer.setActive(true);

        testResource = new Resource();
        testResource.setId(resourceId);
    }

    @Test
    void shouldThrowExceptionIfStartTimeIsTooFarInThePast() {
        // Arrange
        when(userRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(testResource));

        LocalDateTime pastStartTime = LocalDateTime.now().minusHours(2); // Więcej niż 1 godzina w przeszłości
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            allocationService.createAllocation(customerId, resourceId, pastStartTime, endTime, null);
        });

        assertEquals("Start time cannot be more than 1 hour in the past", exception.getMessage() /* */);
    }

    @Test
    void shouldThrowExceptionIfResourceIsAlreadyAllocatedInGivenTime() {
        // Arrange
        when(userRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(testResource));

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);

        // Symulujemy, że repozytorium znajduje konflikt (zwraca niepustą listę)
        when(allocationRepository.findConflictingAllocations(resourceId, startTime, endTime))
                .thenReturn(List.of(new Allocation()));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            allocationService.createAllocation(customerId, resourceId, startTime, endTime, null);
        });

        assertEquals("Resource is not available for the requested time period", exception.getMessage() /* */);
    }

    @Test
    void shouldThrowExceptionOnIdempotencyKeyConflict() {
        // Arrange
        UUID idempotencyKey = UUID.randomUUID();
        when(userRepository.findById(customerId)).thenReturn(Optional.of(activeCustomer));
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(testResource));

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(2);

        Allocation existingAllocation = new Allocation();
        existingAllocation.setId(UUID.randomUUID());

        // Symulujemy, że ten klucz idempotencji został już użyty
        when(allocationRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existingAllocation));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            allocationService.createAllocation(customerId, resourceId, startTime, endTime, idempotencyKey);
        });

        assertTrue(exception.getMessage().contains("Allocation with this idempotency key already exists") /* */);
    }
}