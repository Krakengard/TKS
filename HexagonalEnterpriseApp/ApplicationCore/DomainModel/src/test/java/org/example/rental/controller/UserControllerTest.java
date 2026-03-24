package org.example.rental.controller;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.example.rental.testconfig.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class UserControllerTest extends TestConfig {

    @BeforeEach
    void setUp() {
        // Zaloguj się jako admin aby upewnić się, że token jest dostępny
        try {
            getAdminToken();
        } catch (Exception e) {
            System.out.println("Warning: Could not get admin token in setup: " + e.getMessage());
        }
    }

    @Test
    void shouldCreateCustomerSuccessfully() {
        String adminToken = getAdminToken();
        String randomLogin = getRandomLogin();

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("login", randomLogin);
        customerData.put("name", "Test Customer");
        customerData.put("email", getRandomEmail());
        customerData.put("phoneNumber", "+48123456789");
        customerData.put("address", "Test Address 123");
        customerData.put("type", "customer");
        customerData.put("password", "password123");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(customerData)
                .when()
                .post("/users/register") // UWAGA: tylko /users/register (bez /api/)
                .then()
                .statusCode(201)
                .body("user.id", notNullValue())
                .body("user.login", equalTo(randomLogin))
                .body("user.name", equalTo("Test Customer"))
                .body("user.active", equalTo(true))
                .body("user.phoneNumber", equalTo("+48123456789"));
    }

    @Test
    void shouldCreateAdministratorSuccessfully() {
        String adminToken = getAdminToken();
        String randomLogin = getRandomLogin();

        Map<String, Object> adminData = new HashMap<>();
        adminData.put("login", randomLogin);
        adminData.put("name", "Test Admin");
        adminData.put("email", getRandomEmail());
        adminData.put("department", "IT");
        adminData.put("type", "administrator");
        adminData.put("password", "admin123");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(adminData)
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .body("user.id", notNullValue())
                .body("user.login", equalTo(randomLogin))
                .body("user.department", equalTo("IT"));
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        String adminToken = getAdminToken();
        String randomLogin = getRandomLogin();

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("login", randomLogin);
        customerData.put("name", "User To Get");
        customerData.put("email", getRandomEmail());
        customerData.put("phoneNumber", "+48123456789");
        customerData.put("address", "Test Address");
        customerData.put("type", "customer");
        customerData.put("password", "password123");

        Response createResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(customerData)
                .post("/users/register");

        String userId = createResponse.jsonPath().getString("user.id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("user.id", equalTo(userId))
                .body("user.login", equalTo(randomLogin))
                .body("user.name", equalTo("User To Get"));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        String adminToken = getAdminToken();
        String randomLogin = getRandomLogin();

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("login", randomLogin);
        customerData.put("name", "Original Name");
        customerData.put("email", getRandomEmail());
        customerData.put("phoneNumber", "+48123456789");
        customerData.put("address", "Original Address");
        customerData.put("type", "customer");
        customerData.put("password", "password123");

        Response createResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(customerData)
                .post("/users/register");

        String userId = createResponse.jsonPath().getString("user.id");

        Response getUserResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/{id}", userId)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String verificationToken = getUserResponse.jsonPath().getString("verificationToken");
        String signature = getUserResponse.jsonPath().getString("signature");

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("login", randomLogin);
        updateData.put("name", "Updated Name");
        updateData.put("email", getRandomEmail());
        updateData.put("phoneNumber", "+48987654321");
        updateData.put("address", "Updated Address");
        updateData.put("type", "customer");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .header("X-Object-Signature", signature)
                .header("X-Verification-Token", verificationToken)
                .contentType(ContentType.JSON)
                .body(updateData)
                .when()
                .put("/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("user.id", equalTo(userId))
                .body("user.name", equalTo("Updated Name"))
                .body("user.phoneNumber", equalTo("+48987654321"));
    }

    @Test
    void shouldActivateAndDeactivateUserSuccessfully() {
        String adminToken = getAdminToken();
        String randomLogin = getRandomLogin();

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("login", randomLogin);
        customerData.put("name", "Activation Test User");
        customerData.put("email", getRandomEmail());
        customerData.put("phoneNumber", "+48123456789");
        customerData.put("address", "Test Address");
        customerData.put("type", "customer");
        customerData.put("password", "password123");

        Response createResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(customerData)
                .post("/users/register");

        String userId = createResponse.jsonPath().getString("user.id");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/users/{id}/deactivate", userId)
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("user.active", equalTo(false));

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .post("/users/{id}/activate", userId)
                .then()
                .statusCode(200);

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/users/{id}", userId)
                .then()
                .statusCode(200)
                .body("user.active", equalTo(true));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingUserWithEmptyName() {
        String adminToken = getAdminToken();

        Map<String, Object> invalidUser = new HashMap<>();
        invalidUser.put("login", getRandomLogin());
        invalidUser.put("name", "");
        invalidUser.put("email", getRandomEmail());
        invalidUser.put("type", "customer");
        invalidUser.put("password", "password123");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/users/register")
                .then()
                .statusCode(400)
                .body("error", equalTo("BadRequestException"))
                .body("status", equalTo(400));
    }

    @Test
    void shouldReturnConflictWhenCreatingUserWithDuplicateLogin() {
        String adminToken = getAdminToken();
        String duplicateLogin = "duplicate" + System.currentTimeMillis();

        System.out.println("=== TEST: Creating first user with login: " + duplicateLogin + " ===");

        Map<String, Object> firstUser = new HashMap<>();
        firstUser.put("login", duplicateLogin);
        firstUser.put("name", "First User");
        firstUser.put("email", getRandomEmail());
        firstUser.put("type", "customer");
        firstUser.put("password", "password123");

        Response firstResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(firstUser)
                .when()
                .post("/users/register");

        System.out.println("First response status: " + firstResponse.getStatusCode());
        System.out.println("First response body: " + firstResponse.getBody().asString());

        // Sprawdź czy pierwszy użytkownik został stworzony
        if (firstResponse.getStatusCode() == 201) {
            System.out.println("First user created successfully");
        } else if (firstResponse.getStatusCode() == 200) {
            System.out.println("WARNING: First user already exists or was not created!");
            // Może użytkownik już istnieje? Sprawdźmy
            Response checkResponse = given()
                    .header("Authorization", "Bearer " + adminToken)
                    .when()
                    .get("/users/search/exact?login=" + duplicateLogin);
            System.out.println("Check user exists: " + checkResponse.getBody().asString());
        }

        System.out.println("=== TEST: Creating second user with same login ===");

        Map<String, Object> secondUser = new HashMap<>();
        secondUser.put("login", duplicateLogin);
        secondUser.put("name", "Second User");
        secondUser.put("email", getRandomEmail());
        secondUser.put("type", "customer");
        secondUser.put("password", "password123");

        Response secondResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(secondUser)
                .when()
                .post("/users/register");

        System.out.println("Second response status: " + secondResponse.getStatusCode());
        System.out.println("Second response body: " + secondResponse.getBody().asString());

        secondResponse.then()
                .statusCode(409)
                .body("error", equalTo("ConflictException"))
                .body("status", equalTo(409));
    }

    @Test
    void shouldReturnUnauthorizedWhenGettingUserWithoutAuth() {
        String adminToken = getAdminToken();
        String randomLogin = getRandomLogin();

        Map<String, Object> customerData = new HashMap<>();
        customerData.put("login", randomLogin);
        customerData.put("name", "Unauthorized Test");
        customerData.put("email", getRandomEmail());
        customerData.put("phoneNumber", "+48123456789");
        customerData.put("address", "Test Address");
        customerData.put("type", "customer");
        customerData.put("password", "password123");

        Response createResponse = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(customerData)
                .post("/users/register");

        String userId = createResponse.jsonPath().getString("user.id");

        given()
                .when()
                .get("/users/{id}", userId)
                .then()
                .statusCode(401)
                .body("error", equalTo("UnauthorizedException"))
                .body("status", equalTo(401));
    }

    private String getAdminToken() {
        // Najpierw spróbuj zalogować się jako admin
        Map<String, String> loginData = new HashMap<>();
        loginData.put("login", "admin");
        loginData.put("password", "admin123");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(loginData)
                .when()
                .post("/auth/login"); // UWAGA: tylko /auth/login (bez /api/)

        System.out.println("Login response status: " + response.getStatusCode());
        System.out.println("Login response body: " + response.getBody().asString());

        if (response.getStatusCode() == 200) {
            return response.jsonPath().getString("token");
        }

        // Jeśli nie udało się zalogować, sprawdź dlaczego
        Response debugResponse = given()
                .get("/auth/debug/admin");

        System.out.println("Debug response: " + debugResponse.getBody().asString());

        // Jeśli admin nie istnieje, musimy go stworzyć
        throw new RuntimeException("Admin login failed. Status: " + response.getStatusCode() +
                ". Make sure DataInitializer ran and admin user exists.");
    }
}