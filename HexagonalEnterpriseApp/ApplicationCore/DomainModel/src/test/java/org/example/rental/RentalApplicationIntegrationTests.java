package org.example.rental;

import org.example.rental.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RentalApplicationIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void testPositiveUserCRUD() {
        // Create Customer
        Customer newCustomer = new Customer("new_user", "New User", "new@example.com", "+222222222", "New Address");
        ResponseEntity<Customer> createResponse = restTemplate.postForEntity(baseUrl + "/api/users", newCustomer, Customer.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Customer createdCustomer = createResponse.getBody();
        assertNotNull(createdCustomer);
        assertNotNull(createdCustomer.getId());

        // Read
        ResponseEntity<Customer> getResponse = restTemplate.getForEntity(baseUrl + "/api/users/" + createdCustomer.getId(), Customer.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(createdCustomer.getLogin(), getResponse.getBody().getLogin());

        // Update
        createdCustomer.setName("Updated Name");
        HttpEntity<Customer> updateEntity = new HttpEntity<>(createdCustomer);
        ResponseEntity<Customer> updateResponse = restTemplate.exchange(
                baseUrl + "/api/users/" + createdCustomer.getId(),
                HttpMethod.PUT,
                updateEntity,
                Customer.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Updated Name", updateResponse.getBody().getName());
    }

    @Test
    void testPositiveResourceCRUD() {
        // Create
        Resource newResource = new Resource("New Resource", "New Description", "New Type", 35.0);
        ResponseEntity<Resource> createResponse = restTemplate.postForEntity(baseUrl + "/api/resources", newResource, Resource.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Resource createdResource = createResponse.getBody();
        assertNotNull(createdResource);
        assertNotNull(createdResource.getId());

        // Read
        ResponseEntity<Resource> getResponse = restTemplate.getForEntity(baseUrl + "/api/resources/" + createdResource.getId(), Resource.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(createdResource.getName(), getResponse.getBody().getName());

        // Update
        createdResource.setPricePerHour(40.0);
        HttpEntity<Resource> updateEntity = new HttpEntity<>(createdResource);
        ResponseEntity<Resource> updateResponse = restTemplate.exchange(
                baseUrl + "/api/resources/" + createdResource.getId(),
                HttpMethod.PUT,
                updateEntity,
                Resource.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(40.0, updateResponse.getBody().getPricePerHour());
    }

    @Test
    void testPositiveAllocation() {
        // Create customer and resource first
        Customer testCustomer = new Customer("test_user_alloc", "Test User Alloc", "test_alloc@example.com", "+333333333", "Test Address Alloc");
        ResponseEntity<Customer> customerResponse = restTemplate.postForEntity(baseUrl + "/api/users", testCustomer, Customer.class);
        testCustomer = customerResponse.getBody();

        Resource testResource = new Resource("Test Resource Alloc", "Test Description Alloc", "Test Type Alloc", 25.0);
        ResponseEntity<Resource> resourceResponse = restTemplate.postForEntity(baseUrl + "/api/resources", testResource, Resource.class);
        testResource = resourceResponse.getBody();

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);

        String allocationUrl = baseUrl + "/api/allocations?customerId=" + testCustomer.getId() +
                "&resourceId=" + testResource.getId() + "&startTime=" + startTime;

        ResponseEntity<Allocation> response = restTemplate.postForEntity(allocationUrl, null, Allocation.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testNegativeUserValidation() {
        // Użyj konkretnego typu - Customer z pustym loginem
        Customer invalidUser = new Customer("", "Invalid User", "invalid@example.com", "+333333333", "Invalid Address");
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/api/users", invalidUser, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testNegativeResourceValidation() {
        Resource invalidResource = new Resource("", "Invalid Description", "Invalid Type", -10.0);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/api/resources", invalidResource, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testNegativeDuplicateUserLogin() {
        // First user
        Customer firstUser = new Customer("duplicate_user", "First User", "first@example.com", "+444444444", "First Address");
        ResponseEntity<Customer> firstResponse = restTemplate.postForEntity(baseUrl + "/api/users", firstUser, Customer.class);
        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());

        // Second user with same login
        Customer duplicateUser = new Customer("duplicate_user", "Duplicate User", "duplicate@example.com", "+555555555", "Duplicate Address");
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/api/users", duplicateUser, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetUsersAndResources() {
        // Test basic endpoints
        ResponseEntity<String> usersResponse = restTemplate.getForEntity(baseUrl + "/api/users", String.class);
        assertTrue(usersResponse.getStatusCode().is2xxSuccessful());

        ResponseEntity<String> resourcesResponse = restTemplate.getForEntity(baseUrl + "/api/resources", String.class);
        assertTrue(resourcesResponse.getStatusCode().is2xxSuccessful());
    }
    
}