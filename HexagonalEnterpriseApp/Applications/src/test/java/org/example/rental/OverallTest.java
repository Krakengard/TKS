package org.example.rental;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rental.domain.model.Customer;
import org.example.rental.domain.model.Resource;
import org.example.rental.domain.model.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@JsonDeserialize(as = Customer.class)
abstract class UserMixin {}
@SpringBootTest
@AutoConfigureMockMvc
class OverallTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setupObjectMapper() {
        objectMapper.addMixIn(User.class, UserMixin.class);
    }

    @Test
    void shouldExecuteFullRentalWorkflow() throws Exception {
        Customer customer = new Customer("integracja_user", "Jan Integracyjny", "int@test.pl", "555", "Ulica 1");
        customer.setPassword("Haslo123!");

        mockMvc.perform(post("/api/users/register") //
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated());

        Resource resource = new Resource("Auto Testowe", "Tesla", "Pojazd", 200.0);
        resource.setId(UUID.randomUUID());

        MvcResult resourceResult = mockMvc.perform(post("/api/resources") //
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isOk())
                .andReturn();

        String resourceResponse = resourceResult.getResponse().getContentAsString();
        UUID savedResourceId = UUID.fromString(objectMapper.readValue(resourceResponse, Map.class).get("id").toString());

        MvcResult userResult = mockMvc.perform(get("/api/users/search/exact?login=integracja_user") //
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andReturn();

        UUID savedCustomerId = UUID.fromString(objectMapper.readValue(userResult.getResponse().getContentAsString(), Map.class).get("id").toString());

        mockMvc.perform(post("/api/allocations") //
                        .param("customerId", savedCustomerId.toString())
                        .param("resourceId", savedResourceId.toString())
                        .param("startTime", LocalDateTime.now().plusDays(1).toString())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("integracja_user").roles("CUSTOMER"))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }
}