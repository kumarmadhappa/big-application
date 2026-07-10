package com.bigapplication.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.jwt.secret=test-secret-key-for-jwt-tests-with-32-bytes-min",
        "app.routes.user-service-uri=http://localhost:18081",
        "app.rate-limit.requests-per-minute=1",
        "app.rate-limit.burst-capacity=1"
})
@AutoConfigureWebTestClient
class ApiGatewayActuatorIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private WebTestClient webTestClient;

    @Test
    void healthEndpointShouldBePublic() {
        webTestClient
                .get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    void infoEndpointShouldBePublic() {
        webTestClient
                .get()
                .uri("/actuator/info")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void metricsEndpointShouldBePublic() {
        webTestClient
                .get()
                .uri("/actuator/metrics")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void prometheusEndpointShouldBePublic() {
        webTestClient
                .get()
                .uri("/actuator/prometheus")
                .exchange()
                .expectStatus().isOk();
    }
}
