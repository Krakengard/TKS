package org.example.rental.adapters.repository.jpa;

import org.example.rental.domain.model.Administrator;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JpaUserRepositoryTest {

    private JpaUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new JpaUserRepository();
    }

    @Test
    void shouldSaveAndFindUserByLogin() {
        // Arrange
        Customer customer = new Customer("jan_kowalski", "Jan", "jan@test.pl", "123", "Adres");
        customer.setId(UUID.randomUUID());

        // Act
        userRepository.save(customer); //
        Optional<User> found = userRepository.findByLoginExact("jan_kowalski"); //

        // Assert
        assertTrue(found.isPresent());
        assertEquals(customer.getId(), found.get().getId());
        assertEquals("Jan", found.get().getName());
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateLogin() {
        // Arrange
        Customer user1 = new Customer("unique_login", "User1", "u1@test.pl", "1", "A");
        user1.setId(UUID.randomUUID());
        userRepository.save(user1);

        Customer user2 = new Customer("unique_login", "User2", "u2@test.pl", "2", "B");
        user2.setId(UUID.randomUUID());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userRepository.save(user2); //
        });
        assertTrue(exception.getMessage().contains("already exists")); //
    }

    @Test
    void shouldFindUsersByLoginContainingPart() {
        // Arrange
        userRepository.save(createMockCustomer("aleksander"));
        userRepository.save(createMockCustomer("stefan"));
        userRepository.save(createMockCustomer("alek_test"));

        // Act
        List<User> found = userRepository.findByLoginContaining("alek"); //

        // Assert
        assertEquals(2, found.size());
    }

    private Customer createMockCustomer(String login) {
        Customer c = new Customer(login, "Name", login + "@test.pl", "000", "Addr");
        c.setId(UUID.randomUUID());
        return c;
    }
}