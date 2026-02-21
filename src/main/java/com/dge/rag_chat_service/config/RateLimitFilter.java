/*
package com.dge.rag_chat_service.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.distributed.proxy.ProxyManager;
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

@Component
public class RateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final ProxyManager<String> proxyManager;

    // Configurable limits
    private static final long TOKENS = 10;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);

    public RateLimitFilter(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    private BucketConfiguration bucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.classic(
                        TOKENS,
                        Refill.greedy(TOKENS, REFILL_PERIOD)
                ))
                .build();
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest) ||
                !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String apiKey = httpReq.getHeader("X-API-KEY");
        String clientIp = request.getRemoteAddr();

        String key = (apiKey != null && !apiKey.isBlank())
                ? "rate-limit:api:" + apiKey
                : "rate-limit:ip:" + clientIp;

        Bucket bucket = proxyManager.builder()
                .build(key, this::bucketConfiguration);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            httpResp.setHeader("X-RateLimit-Limit", String.valueOf(TOKENS));
            httpResp.setHeader("X-RateLimit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));

            chain.doFilter(request, response);

        } else {

            long waitSeconds = Math.max(
                    1,
                    probe.getNanosToWaitForRefill() / 1_000_000_000
            );

            httpResp.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResp.setHeader("Retry-After", String.valueOf(waitSeconds));
            httpResp.setHeader("X-RateLimit-Limit", String.valueOf(TOKENS));
            httpResp.setHeader("X-RateLimit-Remaining", "0");
            httpResp.setContentType("text/plain;charset=UTF-8");

            httpResp.getWriter().write(
                    "Too Many Requests - Retry after " + waitSeconds + " seconds."
            );

            log.warn("Rate limit exceeded for key={}", key);
        }
    }
}
*/
