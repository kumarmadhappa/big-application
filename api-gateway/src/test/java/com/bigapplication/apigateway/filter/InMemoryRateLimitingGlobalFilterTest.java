package com.bigapplication.apigateway.filter;

import com.bigapplication.apigateway.config.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InMemoryRateLimitingGlobalFilterTest {

    private InMemoryRateLimitingGlobalFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setRequestsPerMinute(1);
        properties.setBurstCapacity(0);

        filter = new InMemoryRateLimitingGlobalFilter(properties);
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void authPath_isExemptFromRateLimiting() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login")
                        .header("X-Forwarded-For", "10.0.0.1")
                        .build());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any());
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void secondRequestWithinWindow_returns429() {
        MockServerWebExchange first = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header("X-Forwarded-For", "10.0.0.1")
                        .build());
        MockServerWebExchange second = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/users")
                        .header("X-Forwarded-For", "10.0.0.1")
                        .build());

        StepVerifier.create(filter.filter(first, chain)).verifyComplete();
        StepVerifier.create(filter.filter(second, chain)).verifyComplete();

        assertThat(first.getResponse().getStatusCode()).isNull();
        assertThat(second.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        verify(chain, times(1)).filter(any());
    }

    @Test
    void filterOrder_isMinusTwoHundred() {
        assertThat(filter.getOrder()).isEqualTo(-200);
    }
}
