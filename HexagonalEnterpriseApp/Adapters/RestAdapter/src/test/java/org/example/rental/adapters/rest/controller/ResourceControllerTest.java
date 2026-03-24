package org.example.rental.adapters.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rental.domain.model.Resource;
import org.example.rental.port.in.ResourceUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ResourceController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResourceUseCase resourceUseCase;

    @Test
    void shouldReturnAllResources() throws Exception {
        Resource projector = new Resource("Projektor", "Epson", "Sprzęt", 50.0);
        when(resourceUseCase.getAllResources()).thenReturn(List.of(projector)); //

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Projektor"))
                .andExpect(jsonPath("$[0].pricePerHour").value(50.0));
    }

    @Test
    void shouldCreateNewResource() throws Exception {
        Resource newResource = new Resource("Laptop", "Dell", "IT", 100.0);

        when(resourceUseCase.createResource(any(Resource.class))).thenAnswer(invocation -> {
            Resource r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        }); //

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newResource)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void shouldReturnBadRequestWhenCreationFails() throws Exception {
        Resource badResource = new Resource("", "Opis", "Typ", -10.0);
        when(resourceUseCase.createResource(any())).thenThrow(new IllegalArgumentException("Invalid data")); //

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badResource)))

                .andExpect(status().isBadRequest());
    }
}