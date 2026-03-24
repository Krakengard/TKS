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
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserUseCase userUseCase;

    @MockBean
    private JwsService jwsService;

    @JsonDeserialize(as = Customer.class)
    private interface UserMixin {}

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        objectMapper.addMixIn(User.class, UserMixin.class);
    }

    @Test
    void shouldReturnConflictWhenRegisteringExistingUser() throws Exception {
        String rawJson = "{\"login\":\"existing_user\",\"name\":\"Test\",\"email\":\"test@test.com\"}";
        Customer existingCustomer = new Customer();
        existingCustomer.setLogin("existing_user");

        when(userUseCase.getUserByLogin("existing_user")).thenReturn(Optional.of(existingCustomer));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rawJson))

                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnBadRequestWhenSignatureIsInvalidDuringUpdate() throws Exception {
        UUID userId = UUID.randomUUID();
        Customer updateData = new Customer("haker", "Haker", "haker@haker.com", "123", "Adres");

        when(jwsService.verifyUserIdToken(eq("valid-token"), eq(userId.toString()))).thenReturn(true);
        when(jwsService.verifySignature(eq("invalid-signature"), any())).thenReturn(false);

        mockMvc.perform(put("/api/users/" + userId)
                        .header("X-Verification-Token", "valid-token")
                        .header("X-Object-Signature", "invalid-signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid object signature"));
    }
}