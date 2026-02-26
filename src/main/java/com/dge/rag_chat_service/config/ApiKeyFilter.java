package com.dge.rag_chat_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Filter to check for a valid API key in the request header.
 * Excludes Swagger UI and API docs endpoints from authentication.
 */
@Component
@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    private final ApiKeyStore apiKeyStore;

    public ApiKeyFilter(ApiKeyStore apiKeyStore) {
        this.apiKeyStore = apiKeyStore;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/actuator") || path.contains("/swagger-ui") || path.contains("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey == null) {
            unauthorized(response, "Missing API key");
            return;
        }

        var userIdOpt = apiKeyStore.getUserIdByApiKey(apiKey);

        if (userIdOpt.isEmpty()) {
            unauthorized(response, "Invalid API key");
            return;
        }

        String userId = userIdOpt.get();
        var authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("Authenticated user: " + userId);
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> error = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", message
        );

        new ObjectMapper().writeValue(response.getWriter(), error);
    }
}
