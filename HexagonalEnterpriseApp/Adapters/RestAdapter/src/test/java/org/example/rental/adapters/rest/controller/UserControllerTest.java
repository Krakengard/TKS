package org.example.rental.adapters.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rental.domain.model.Customer;
import org.example.rental.port.in.UserUseCase;
import org.example.rental.security.JwsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.example.rental.domain.model.User;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Filtry Spring Security (np. JWT filter) wyłączone, testujemy tylko kontroler
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private JwsService jwsService;

    // Tworzymy regułę ("MixIn"), która mówi: "Jeśli widzisz User, zrób z niego Customer"
    @JsonDeserialize(as = Customer.class)
    private interface UserMixin {}

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Wstrzykujemy regułę do naszego mappera JSON
        objectMapper.addMixIn(User.class, UserMixin.class);
    }

    @Test
    void shouldReturnConflictWhenRegisteringExistingUser() throws Exception {
        // Arrange: Próbujemy zarejestrować usera, ale taki login już istnieje w systemie
        String rawJson = "{\"login\":\"existing_user\",\"name\":\"Test\",\"email\":\"test@test.com\"}";
        Customer existingCustomer = new Customer();
        existingCustomer.setLogin("existing_user");

        // Symulujemy, że UseCase znajduje już takiego użytkownika
        when(userUseCase.getUserByLogin("existing_user")).thenReturn(Optional.of(existingCustomer));

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))

                // Kontroler powinien rzucić ConflictException, co mapuje się na HTTP 409 Conflict
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenSignatureIsInvalidDuringUpdate() throws Exception {
        // Arrange: Symulujemy atak, w którym ktoś próbuje wysłać sfałszowaną sygnaturę
        UUID userId = UUID.randomUUID();
        Customer updateData = new Customer("haker", "Haker", "haker@haker.com", "123", "Adres");

        // Token ID jest poprawny...
        when(jwsService.verifyUserIdToken(eq("valid-token"), eq(userId.toString()))).thenReturn(true);
        // ...ale sygnatura danych wejściowych się NIE zgadza!
        when(jwsService.verifySignature(eq("invalid-signature"), any())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/users/" + userId)
                        .header("X-Verification-Token", "valid-token")
                        .header("X-Object-Signature", "invalid-signature") // Zła sygnatura
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))

                // Kontroler ma obowiązek zablokować to i zwrócić HTTP 400 Bad Request
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid object signature"));
    }
}