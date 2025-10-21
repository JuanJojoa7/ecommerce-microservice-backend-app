package com.selimhorri.system;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StackIntegrationIT {

    @BeforeAll
    static void setup() {
        String base = System.getProperty("SYSTEM_TEST_BASE_URL", System.getenv().getOrDefault("SYSTEM_TEST_BASE_URL", "http://localhost:8080"));
        RestAssured.baseURI = base;
        // If gateway not reachable, skip the entire system test suite
        try {
            java.net.URI uri = java.net.URI.create(base);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
            try (java.net.Socket s = new java.net.Socket()) {
                s.connect(new java.net.InetSocketAddress(host, port), 500);
            }
        } catch (Exception ex) {
            Assumptions.assumeTrue(false, "System under test not reachable at " + base + " (" + ex.getMessage() + ")");
        }
    }

    @Test @Order(1)
    void products_through_gateway() {
        given().when().get("/product-service/api/products").then().statusCode(anyOf(is(200), is(204)));
    }

    @Test @Order(2)
    void users_through_gateway() {
        given().when().get("/user-service/api/users").then().statusCode(anyOf(is(200), is(204)));
    }

    @Test @Order(3)
    void orders_through_gateway() {
        given().when().get("/order-service/api/orders").then().statusCode(anyOf(is(200), is(204)));
    }

    @Test @Order(4)
    void payments_through_gateway() {
        given().when().get("/payment-service/api/payments").then().statusCode(anyOf(is(200), is(204), is(401), is(403)));
    }

    @Test @Order(5)
    void shipping_through_gateway() {
        given().when().get("/shipping-service/api/shippings").then().statusCode(anyOf(is(200), is(204)));
    }
}
