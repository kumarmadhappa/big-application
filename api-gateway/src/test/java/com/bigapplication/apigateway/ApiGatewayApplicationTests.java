package com.bigapplication.apigateway;

import com.bigapplication.apigateway.filter.InMemoryRateLimitingGlobalFilter;
import com.bigapplication.apigateway.filter.JwtValidationGlobalFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.jwt.secret=test-secret-key-for-jwt-tests-with-32-bytes-min",
        "app.routes.user-service-uri=http://localhost:18081",
        "app.rate-limit.requests-per-minute=1",
        "app.rate-limit.burst-capacity=1"
})
class ApiGatewayApplicationTests {

    @Autowired
    private JwtValidationGlobalFilter jwtValidationGlobalFilter;

    @Autowired
    private InMemoryRateLimitingGlobalFilter inMemoryRateLimitingGlobalFilter;

    @Test
    void contextLoads() {
        org.assertj.core.api.Assertions.assertThat(jwtValidationGlobalFilter).isNotNull();
        org.assertj.core.api.Assertions.assertThat(inMemoryRateLimitingGlobalFilter).isNotNull();
    }
}
