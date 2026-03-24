package org.example.rental.controller;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.rental.testconfig.TestConfig;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AllocationControllerTest extends TestConfig {

    private String createTestCustomer() {
        Map<String, Object> customerData = new HashMap<>();
        customerData.put("login", getRandomLogin());
        customerData.put("name", "Allocation Test Customer");
        customerData.put("email", getRandomEmail());
        customerData.put("phoneNumber", "+48123456789");
        customerData.put("address", "Test Address");
        customerData.put("type", "customer");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(customerData)
                .post("/users");

        return response.jsonPath().getString("id");
    }

    private String createTestResource() {
        Map<String, Object> resourceData = new HashMap<>();
        resourceData.put("name", "Allocation Test Resource");
        resourceData.put("description", "Test resource for allocation");
        resourceData.put("type", "Test Equipment");
        resourceData.put("pricePerHour", 35.0);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(resourceData)
                .post("/resources");

        return response.jsonPath().getString("id");
    }

    @Test
    void shouldCreateAllocationSuccessfully() {
        String customerId = createTestCustomer();
        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .when()
                .post("/allocations")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("customer.id", equalTo(customerId))
                .body("resource.id", equalTo(resourceId))
                .body("completed", equalTo(false));
    }

    @Test
    void shouldCreateAllocationWithIdempotencyKey() {
        String customerId = createTestCustomer();
        String resourceId = createTestResource();
        UUID idempotencyKey = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        String firstAllocationId = given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .header("Idempotency-Key", idempotencyKey.toString())
                .when()
                .post("/allocations")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getString("id");

        given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .header("Idempotency-Key", idempotencyKey.toString())
                .when()
                .post("/allocations")
                .then()
                .statusCode(409); // Conflict - allocation with this key already exists
    }

    @Test
    void shouldCompleteAllocationSuccessfully() {
        String customerId = createTestCustomer();
        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = startTime.plusHours(2);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        Response allocationResponse = given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .post("/allocations");

        String allocationId = allocationResponse.jsonPath().getString("id");

        given()
                .when()
                .post("/allocations/{id}/complete", allocationId)
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/allocations/customer/{customerId}/past", customerId)
                .then()
                .statusCode(200)
                .body("find { it.id == '" + allocationId + "' }.completed", equalTo(true));
    }

    @Test
    void shouldDeleteAllocationSuccessfully() {
        String customerId = createTestCustomer();
        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        Response allocationResponse = given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .post("/allocations");

        String allocationId = allocationResponse.jsonPath().getString("id");

        given()
                .when()
                .delete("/allocations/{id}", allocationId)
                .then()
                .statusCode(200);
    }

    @Test
    void shouldNotDeleteCompletedAllocation() {
        String customerId = createTestCustomer();
        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = startTime.plusHours(2);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        Response allocationResponse = given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .post("/allocations");

        String allocationId = allocationResponse.jsonPath().getString("id");

        given().post("/allocations/{id}/complete", allocationId);

        given()
                .when()
                .delete("/allocations/{id}", allocationId)
                .then()
                .statusCode(409); // Conflict - cannot delete completed allocation
    }

    @Test
    void shouldReturnBadRequestWhenAllocatingResourceToInactiveCustomer() {
        String customerId = createTestCustomer();
        given().post("/users/{id}/deactivate", customerId);

        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .when()
                .post("/allocations")
                .then()
                .statusCode(400); // Bad Request - nieaktywny klient
    }

    @Test
    void shouldReturnBadRequestForInvalidTimeRange() {
        // Given
        String customerId = createTestCustomer();
        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.minusHours(1); // End time before start time

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .when()
                .post("/allocations")
                .then()
                .statusCode(400); // Bad Request - invalid time range
    }

    @Test
    void shouldReturnConflictWhenAllocatingAlreadyAllocatedResource() {
        String customer1Id = createTestCustomer();
        String customer2Id = createTestCustomer();
        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        given()
                .param("customerId", customer1Id)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .post("/allocations")
                .then()
                .statusCode(201);


        given()
                .param("customerId", customer2Id)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .when()
                .post("/allocations")
                .then()
                .statusCode(400); // Bad Request - resource not available
    }

    @Test
    void shouldGetPastAndCurrentAllocationsForCustomer() {
        String customerId = createTestCustomer();
        String resourceId = createTestResource();

        LocalDateTime startTime = LocalDateTime.now().minusMinutes(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        given()
                .param("customerId", customerId)
                .param("resourceId", resourceId)
                .param("startTime", startTime.format(formatter))
                .param("endTime", endTime.format(formatter))
                .post("/allocations")
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/allocations/customer/{customerId}/current", customerId)
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    void shouldReturnNotFoundForNonExistentAllocation() {
        UUID nonExistentAllocationId = UUID.randomUUID();

        given()
                .when()
                .post("/allocations/{id}/complete", nonExistentAllocationId)
                .then()
                .statusCode(404); // Not Found

        // When & Then - próba usunięcia nieistniejącej alokacji
        given()
                .when()
                .delete("/allocations/{id}", nonExistentAllocationId)
                .then()
                .statusCode(404); // Not Found
    }
}