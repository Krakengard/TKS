package org.example.rental.controller;


import  io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.rental.testconfig.TestConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ResourceControllerTest extends TestConfig {

    @Test
    void shouldCreateResourceSuccessfully() {
        Map<String, Object> resourceData = new HashMap<>();
        resourceData.put("name", "Test Projector");
        resourceData.put("description", "High quality video projector");
        resourceData.put("type", "AV Equipment");
        resourceData.put("pricePerHour", 50.0);

        given()
                .contentType(ContentType.JSON)
                .body(resourceData)
                .when()
                .post("/resources")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Test Projector"))
                .body("type", equalTo("AV Equipment"))
                .body("pricePerHour", equalTo(50.0f));
    }

    @Test
    void shouldGetResourceByIdSuccessfully() {
        Map<String, Object> resourceData = new HashMap<>();
        resourceData.put("name", "Resource To Get");
        resourceData.put("description", "Test description");
        resourceData.put("type", "Test Type");
        resourceData.put("pricePerHour", 25.0);

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(resourceData)
                .post("/resources");

        String resourceId = createResponse.jsonPath().getString("id");

        given()
                .when()
                .get("/resources/{id}", resourceId)
                .then()
                .statusCode(200)
                .body("id", equalTo(resourceId))
                .body("name", equalTo("Resource To Get"))
                .body("pricePerHour", equalTo(25.0f));
    }

    @Test
    void shouldUpdateResourceSuccessfully() {
        Map<String, Object> resourceData = new HashMap<>();
        resourceData.put("name", "Original Resource");
        resourceData.put("description", "Original description");
        resourceData.put("type", "Original Type");
        resourceData.put("pricePerHour", 30.0);

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(resourceData)
                .post("/resources");

        String resourceId = createResponse.jsonPath().getString("id");

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Updated Resource");
        updateData.put("description", "Updated description");
        updateData.put("type", "Updated Type");
        updateData.put("pricePerHour", 40.0);

        given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/resources/{id}", resourceId)
                .then()
                .statusCode(200)
                .body("id", equalTo(resourceId))
                .body("name", equalTo("Updated Resource"))
                .body("pricePerHour", equalTo(40.0f));
    }

    @Test
    void shouldDeleteResourceSuccessfully() {
        Map<String, Object> resourceData = new HashMap<>();
        resourceData.put("name", "Resource To Delete");
        resourceData.put("description", "Test description");
        resourceData.put("type", "Test Type");
        resourceData.put("pricePerHour", 20.0);

        Response createResponse = given()
                .contentType(ContentType.JSON)
                .body(resourceData)
                .post("/resources");

        String resourceId = createResponse.jsonPath().getString("id");

        given()
                .when()
                .delete("/resources/{id}", resourceId)
                .then()
                .statusCode(200);

        given()
                .when()
                .get("/resources/{id}", resourceId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingResourceWithNegativePrice() {
        Map<String, Object> invalidResource = new HashMap<>();
        invalidResource.put("name", "Invalid Resource");
        invalidResource.put("description", "Test description");
        invalidResource.put("type", "Test Type");
        invalidResource.put("pricePerHour", -10.0);

        given()
                .contentType(ContentType.JSON)
                .body(invalidResource)
                .when()
                .post("/resources")
                .then()
                .statusCode(400); // Bad Request
    }
}