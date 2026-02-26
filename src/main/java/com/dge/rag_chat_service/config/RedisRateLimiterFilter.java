package com.dge.rag_chat_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
/**
 * Filter to implement rate limiting using Redis.
 * Limits each IP address to a certain number of requests within a time window.
 */
@Component
@RequiredArgsConstructor
public class RedisRateLimiterFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    private static final int LIMIT = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");
        String clientIp = request.getRemoteAddr();
        String key = (apiKey != null && !apiKey.isBlank()) ? "api:" + apiKey : "ip:" + clientIp;
        String count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", WINDOW);
        } else {
            String rateLimit = String.valueOf(LIMIT - Integer.parseInt(count));
            if (Integer.parseInt(count) >= LIMIT) {
                response.setHeader("X-RateLimit-Limit", rateLimit);
                response.setHeader("X-RateLimit-Remaining", "0");
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("""
                    {
                      "status":429,
                      "error":"Too Many Requests"
                    }
                """);
                return;
            } else {
                response.setHeader("X-RateLimit-Limit", rateLimit);
                redisTemplate.opsForValue().increment(key);
            }
        }

        chain.doFilter(request, response);
    }
}
