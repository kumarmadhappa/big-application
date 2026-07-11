package com.bigapplication.apigateway.filter;

import com.bigapplication.apigateway.config.RateLimitProperties;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class InMemoryRateLimitingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(InMemoryRateLimitingGlobalFilter.class);

    private static final List<String> EXEMPT_PATHS = List.of(
            "/actuator/health",
            "/actuator/info",
            "/actuator/metrics",
            "/actuator/metrics/**",
            "/actuator/prometheus",
            "/api/banking/auth/**",
            "/api/auth/**");

    private final RateLimitProperties properties;
    private final Map<String, WindowState> states = new ConcurrentHashMap<>();
    private final org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();

    public InMemoryRateLimitingGlobalFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        if (isExempt(path)) {
            log.info("Rate Limiting skipped for exempt path={}", path);
            return chain.filter(exchange);
        }

        String key = clientKey(exchange);
        WindowState state = states.computeIfAbsent(key, ignored -> new WindowState(Instant.now().toEpochMilli(), new AtomicInteger()));
        long now = Instant.now().toEpochMilli();
        long windowMs = 60_000L;
        int maxRequests = properties.getRequestsPerMinute() + properties.getBurstCapacity();

        synchronized (state) {
            if (now - state.windowStartMs >= windowMs) {
                state.windowStartMs = now;
                state.counter.set(0);
            }
            int current = state.counter.incrementAndGet();
            if (current > maxRequests) {
                log.warn("Rate limit exceeded for key={}", key);
                return tooManyRequests(exchange.getResponse());
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private String clientKey(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    private boolean isExempt(String path) {
        return EXEMPT_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> tooManyRequests(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        byte[] bytes = "{\"success\":false,\"message\":\"Rate limit exceeded\"}"
                .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private static class WindowState {
        private long windowStartMs;
        private final AtomicInteger counter;

        private WindowState(long windowStartMs, AtomicInteger counter) {
            this.windowStartMs = windowStartMs;
            this.counter = counter;
        }
    }
}
