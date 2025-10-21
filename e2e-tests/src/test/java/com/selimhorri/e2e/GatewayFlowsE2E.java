package com.selimhorri.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GatewayFlowsE2E {

    @BeforeAll
    static void setup() {
        String base = System.getProperty("GATEWAY_BASE_URL", "http://localhost:8080");
        RestAssured.baseURI = base;
        // Skip E2E tests when gateway is not reachable locally
        try {
            java.net.URI uri = java.net.URI.create(base);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
            try (java.net.Socket s = new java.net.Socket()) {
                s.connect(new java.net.InetSocketAddress(host, port), 250);
            }
        } catch (Exception ex) {
            Assumptions.assumeTrue(false, "Gateway not reachable at " + base + " (" + ex.getMessage() + ")");
        }
    }

    @Test
    @DisplayName("List products through API Gateway")
    void listProducts() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/product-service/api/products")
        .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @DisplayName("List users through API Gateway")
    void listUsers() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/user-service/api/users")
        .then()
            .statusCode(anyOf(is(200), is(204)));
    }

    @Test
    @DisplayName("Create cart and order flow stub")
    void createCartAndOrder() {
        // This checks that the order-service route is reachable; details depend on data fixtures
        given()
            .contentType(ContentType.JSON)
            .body("{\"cartId\":1}")
        .when()
            .post("/order-service/api/orders")
        .then()
            .statusCode(anyOf(is(200), is(201), is(400)));
    }

    @Test
    @DisplayName("Payments route reachable")
    void paymentsReachable() {
        given().accept(ContentType.JSON)
        .when()
            .get("/payment-service/api/payments")
        .then()
            .statusCode(anyOf(is(200), is(204), is(401), is(403)));
    }

    @Test
    @DisplayName("Favourites route reachable")
    void favouritesReachable() {
        given().accept(ContentType.JSON)
        .when()
            .get("/favourite-service/api/favourites")
        .then()
            .statusCode(anyOf(is(200), is(204)));
    }
}
