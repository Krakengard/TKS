package org.example.rental.application.service;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.exception.ConflictException;
import org.example.rental.domain.model.Resource;
import org.example.rental.port.out.AllocationRepository;
import org.example.rental.port.out.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceAdvancedTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private AllocationRepository allocationRepository;

    @InjectMocks
    private ResourceService resourceService;

    @Test
    void shouldThrowExceptionWhenCreatingResourceWithNegativePrice() {
        Resource invalidResource = new Resource("Projektor", "Opis", "Sprzęt", -10.0);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            resourceService.createResource(invalidResource);
        });

        assertEquals("Price per hour cannot be negative", exception.getMessage() /* */);
    }

    @Test
    void shouldThrowExceptionWhenDeletingResourceWithActiveAllocations() {
        UUID resourceId = UUID.randomUUID();
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(new Resource()));
        when(allocationRepository.hasActiveAllocationsForResource(resourceId)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            resourceService.deleteResource(resourceId);
        });

        assertEquals("Cannot delete resource with active allocations", exception.getMessage() /* */);
        verify(resourceRepository, never()).delete(resourceId);
    }
}