package org.example.rental.adapters.repository.jpa;

import org.example.rental.domain.model.Allocation;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JpaAllocationRepositoryTest {

    private JpaAllocationRepository repository;
    private UUID resourceId;
    private Customer mockCustomer;
    private Resource mockResource;

    @BeforeEach
    void setUp() {
        repository = new JpaAllocationRepository();
        resourceId = UUID.randomUUID();

        mockCustomer = new Customer("test_user", "Jan", "jan@test.pl", "123", "Adres");
        mockCustomer.setId(UUID.randomUUID());

        mockResource = new Resource("Projektor", "Epson", "Sprzęt", 100.0);
        mockResource.setId(resourceId);
    }

    @Test
    void shouldFindConflictingAllocations() {
        LocalDateTime start = LocalDateTime.now().plusHours(5);
        LocalDateTime end = start.plusHours(2);

        Allocation existing = new Allocation(mockCustomer, mockResource, start, end);
        existing.setId(UUID.randomUUID());
        repository.save(existing); //

        List<Allocation> conflicts = repository.findConflictingAllocations(
                resourceId, start.plusHours(1), end.plusHours(1)); //

        assertEquals(1, conflicts.size(), "Powinien zostać znaleziony jeden konflikt");
        assertEquals(existing.getId(), conflicts.get(0).getId());
    }

    @Test
    void shouldIgnoreCompletedAllocationsWhenCheckingConflicts() {
        LocalDateTime start = LocalDateTime.now().plusHours(5);
        LocalDateTime end = start.plusHours(2);

        Allocation completed = new Allocation(mockCustomer, mockResource, start, end);
        completed.setId(UUID.randomUUID());
        completed.setCompleted(true); //
        repository.save(completed);

        List<Allocation> conflicts = repository.findConflictingAllocations(resourceId, start, end);

        assertTrue(conflicts.isEmpty(), "Zakończone rezerwacje nie powinny generować konfliktów");
    }

    @Test
    void shouldCorrectyIdentifyActiveAllocationsForResource() {
        Allocation active = new Allocation(mockCustomer, mockResource, LocalDateTime.now(), null);
        active.setId(UUID.randomUUID());
        repository.save(active);

        assertTrue(repository.hasActiveAllocationsForResource(resourceId),
                "Powinna istnieć aktywna rezerwacja dla tego zasobu"); //

        active.setCompleted(true);
        assertFalse(repository.hasActiveAllocationsForResource(resourceId),
                "Po zakończeniu rezerwacji, zasób powinien być wolny");
    }
}