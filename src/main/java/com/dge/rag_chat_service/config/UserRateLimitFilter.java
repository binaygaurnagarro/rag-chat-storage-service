package com.dge.rag_chat_service.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limit filter that enforces per-user (API key) request limits using Bucket4j.
 * - Key resolution: 'X-API-KEY' header if present, otherwise client IP.
 * - Default limit: 10 requests per minute (adjust constants below as needed).
 * - Response: HTTP 429 with simple message and rate-limit headers.
 */
@Component
public class UserRateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(UserRateLimitFilter.class);

    // configurable policy: 10 tokens per minute
    private static final long TOKENS = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    // buckets cache per user/api-key
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(TOKENS, Refill.greedy(TOKENS, REFILL_PERIOD));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> {
            log.debug("Creating new bucket for key={}", k);
            return newBucket();
        });
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String apiKey = httpReq.getHeader("X-API-KEY");
        String clientIp = request.getRemoteAddr();
        String key = (apiKey != null && !apiKey.isBlank()) ? "api:" + apiKey : "ip:" + clientIp;

        Bucket bucket = resolveBucket(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // set rate-limit headers for clients
            httpResp.setHeader("X-RateLimit-Limit", String.valueOf(TOKENS));
            httpResp.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            long waitNanos = probe.getNanosToWaitForRefill();
            long waitSeconds = Math.max(1, (waitNanos + 999_999_999L) / 1_000_000_000L);
            httpResp.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResp.setHeader("Retry-After", String.valueOf(waitSeconds));
            httpResp.setHeader("X-RateLimit-Limit", String.valueOf(TOKENS));
            httpResp.setHeader("X-RateLimit-Remaining", "0");
            httpResp.setContentType("text/plain;charset=UTF-8");
            httpResp.getWriter().write("Too Many Requests - Rate limit exceeded. Retry after " + waitSeconds + " seconds.");
            log.debug("Rate limit exceeded for key={} waitSeconds={}", key, waitSeconds);
        }
    }
}