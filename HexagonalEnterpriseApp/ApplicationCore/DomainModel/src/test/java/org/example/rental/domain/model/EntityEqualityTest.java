package org.example.rental.domain.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EntityEqualityTest {

    @Test
    void usersWithSameIdShouldBeEqualEvenIfSubclassesDiffer() {
        // Arrange: Tworzymy dwa różne typy użytkowników, ale wymuszamy to samo ID
        UUID sharedId = UUID.randomUUID();

        Customer customer = new Customer("jan", "Jan Kowalski", "jan@test.pl", "123", "Adres");
        customer.setId(sharedId);

        Administrator admin = new Administrator("admin", "Admin", "admin@test.pl", "IT");
        admin.setId(sharedId);

        // Act & Assert: Ponieważ equals() w klasie User sprawdza tylko ID, powinny być równe
        assertEquals(customer, admin, "Encje User z tym samym ID powinny być traktowane jako równe");
        assertEquals(customer.hashCode(), admin.hashCode() /* */);
    }
}