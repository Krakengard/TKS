package org.example.rental.testconfig;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;

public class TestConfig {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost:8081";
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    public static String getRandomEmail() {
        return "test" + System.currentTimeMillis() + "@example.com";
    }

    public static String getRandomLogin() {
        return "user" + System.currentTimeMillis();
    }
}