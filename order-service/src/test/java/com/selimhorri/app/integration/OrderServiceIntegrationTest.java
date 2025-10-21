package com.selimhorri.app.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
    }
)
class OrderServiceIntegrationTest {

    @LocalServerPort
    int port;

    TestRestTemplate rest = new TestRestTemplate();

    @Test
    void getOrders_endpointResponds() {
        // Context path is configured as /order-service in application.yml
        ResponseEntity<String> resp = rest.getForEntity("http://localhost:" + port + "/order-service/api/orders", String.class);
        assertThat(resp.getStatusCodeValue()).isIn(200, 204);
    }
}
