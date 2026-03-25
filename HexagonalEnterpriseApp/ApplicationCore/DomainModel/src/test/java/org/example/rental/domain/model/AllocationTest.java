package org.example.rental.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AllocationTest {

    @Test
    void shouldInitializeWithRandomUUIDAndNotCompletedStatus() {
        Allocation allocation = new Allocation();

        assertNotNull(allocation.getId(), "ID powinno zostać wygenerowane automatycznie");
        assertNotNull(allocation.getIdempotencyKey(), "Klucz idempotencji powinien zostać wygenerowany");
        assertFalse(allocation.isCompleted(), "Nowa alokacja nie powinna być ukończona");
    }

    @Test
    void shouldAssignCustomerAndResourceCorrectly() {
        Customer customer = new Customer("jan", "Jan Kowalski", "jan@test.pl", "123", "Adres");
        Resource resource = new Resource("Projektor", "Opis", "Sprzęt", 50.0);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Allocation allocation = new Allocation(customer, resource, start, end);

        assertEquals(customer, allocation.getCustomer() /* */);
        assertEquals(resource, allocation.getResource() /* */);
        assertEquals(start, allocation.getStartTime() /* */);
    }
}