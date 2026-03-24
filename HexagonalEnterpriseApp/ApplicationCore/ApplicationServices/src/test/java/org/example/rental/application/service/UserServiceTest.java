package org.example.rental.application.service;

import org.example.rental.domain.exception.BadRequestException;
import org.example.rental.domain.model.Administrator;
import org.example.rental.domain.model.Customer;
import org.example.rental.port.out.PasswordEncoderPort;
import org.example.rental.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAdvancedTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldThrowExceptionWhenUpdatingUserWithDifferentClassType() {
        UUID userId = UUID.randomUUID();
        Customer existingCustomer = new Customer();
        existingCustomer.setId(userId);

        Administrator updateRequest = new Administrator(); // Inny typ!

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingCustomer));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.updateUser(userId, updateRequest);
        });

        assertTrue(exception.getMessage().contains("Cannot change user type from Customer to Administrator") /* */);
    }

    @Test
    void shouldThrowExceptionWhenChangingPasswordWithWrongCurrentPassword() {
        UUID userId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setPassword("hashedOldPassword");

        when(userRepository.findById(userId)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrongCurrentPassword", "hashedOldPassword")).thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userService.changePassword(userId, "wrongCurrentPassword", "newPassword123");
        });

        assertEquals("Obecne hasło jest nieprawidłowe", exception.getMessage() /* */);
        verify(userRepository, never()).save(any());
    }
}