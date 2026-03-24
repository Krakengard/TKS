package org.example.rental.adapters.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rental.domain.model.Customer;
import org.example.rental.port.in.UserUseCase;
import org.example.rental.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
// Wyłączamy filtry Spring Security (np. sprawdzanie tokenów) na potrzeby testu samego kontrolera
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // Symuluje klienta wysyłającego zapytania HTTP (np. Postmana)

    @Autowired
    private ObjectMapper objectMapper; // Zmienia obiekty Java na JSON

    // Używamy @MockBean (zamiast @Mock), aby Spring wstawił te fałszywe obiekty do swojego kontekstu
    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldReturnTokenAndUserOnSuccessfulLogin() throws Exception {
        // Arrange
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");

        // 1. Domenowy użytkownik, którego "znajdzie" serwis (dla budowy odpowiedzi JSON)
        Customer mockCustomer = new Customer("john_doe", "John Doe", "john@example.com", "123456789", "123 Main St");
        mockCustomer.setActive(true);

        when(userUseCase.getUserByLogin("john_doe")).thenReturn(Optional.of(mockCustomer));

        // 2. TWORZYMY USERA DLA SPRING SECURITY (NAPRAWA BŁĘDU)
        org.springframework.security.core.userdetails.UserDetails springUserDetails =
                new org.springframework.security.core.userdetails.User(
                        "john_doe",
                        "password123",
                        new java.util.ArrayList<>() // Puste role na potrzeby testu
                );

        // 3. Wrzucamy do autentykacji UserDetails ze Springa, a nie Customer
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(springUserDetails, null, null);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        when(jwtService.generateToken(any())).thenReturn("fake-super-secret-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-super-secret-jwt-token"))
                .andExpect(jsonPath("$.user.login").value("john_doe"))
                .andExpect(jsonPath("$.user.type").value("customer"));
    }
}