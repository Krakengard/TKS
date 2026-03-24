package org.example.rental.adapters.rest.controller;

import org.example.rental.domain.model.Customer;
import org.example.rental.port.in.AllocationUseCase;
import org.example.rental.port.in.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@WebMvcTest(controllers = AllocationController.class)
@AutoConfigureMockMvc // Tutaj NIE wyłączamy filtrów, bo potrzebujemy mockować SecurityContext!
class AllocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllocationUseCase allocationUseCase;

    @MockBean
    private UserUseCase userUseCase;

    @Test
    void shouldForbidCustomerCreatingAllocationForAnotherCustomer() throws Exception {
        // Arrange
        UUID targetCustomerId = UUID.randomUUID();
        Customer targetCustomer = new Customer();
        targetCustomer.setLogin("target_user"); // Klient, NA KTÓREGO robiona jest rezerwacja

        when(userUseCase.getUserById(targetCustomerId)).thenReturn(Optional.of(targetCustomer));

        String startTime = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);
        String endTime = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_DATE_TIME);

        // Act & Assert
        mockMvc.perform(post("/api/allocations")
                        .param("customerId", targetCustomerId.toString())
                        .param("resourceId", UUID.randomUUID().toString())
                        .param("startTime", startTime)
                        .param("endTime", endTime)
                        // Symulujemy zapytanie od "złośliwego" zwykłego klienta, który podał inny login niż targetCustomer
                        .with(user("malicious_user").roles("CUSTOMER"))
                .with(csrf())
                // Ponieważ zalogowany użytkownik (malicious_user) robi rezerwację dla innego (target_user) i nie ma roli ADMIN, oczekujemy 401/403
                ).andExpect(status().isUnauthorized()); // Twój kod rzuca UnauthorizedException
    }

    @Test
    void shouldAllowAdminToCreateAllocationForAnyCustomer() throws Exception {
        // Arrange
        UUID targetCustomerId = UUID.randomUUID();
        Customer targetCustomer = new Customer();
        targetCustomer.setLogin("target_user"); // Klient docelowy

        when(userUseCase.getUserById(targetCustomerId)).thenReturn(Optional.of(targetCustomer));

        String startTime = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_DATE_TIME);
        String endTime = LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_DATE_TIME);

        // Act & Assert
        mockMvc.perform(post("/api/allocations")
                        .param("customerId", targetCustomerId.toString())
                        .param("resourceId", UUID.randomUUID().toString())
                        .param("startTime", startTime)
                        .param("endTime", endTime)
                        // Symulujemy zapytanie od Administratora
                        .with(user("admin_user").roles("ADMIN"))
                .with(csrf())
                // Administrator ma prawo tworzyć rezerwacje w imieniu innych, więc powinno przejść (status 200 OK)
                ).andExpect(status().isOk());
    }

    @Test
    void shouldReturnConflictWhenDeletingCompletedAllocation() throws Exception {
        // Arrange
        UUID allocationId = UUID.randomUUID();
        // Symulujemy, że Delete rzuca IllegalStateException (np. z powodu tego, że rezerwacja się zakończyła)
        doThrow(new IllegalStateException("Cannot delete completed allocation"))
                .when(allocationUseCase).deleteAllocation(allocationId);

        // Act & Assert
        mockMvc.perform(delete("/api/allocations/" + allocationId)
                        // Symulujemy zalogowanego usera
                        .with(user("admin").roles("ADMIN"))
                .with(csrf())
                // Kontroler ma obowiązek złapać IllegalStateException i zamienić to na 409 Conflict
                ).andExpect(status().isConflict());
    }
}