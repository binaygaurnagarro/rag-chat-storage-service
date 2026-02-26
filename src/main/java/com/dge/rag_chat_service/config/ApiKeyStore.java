package com.dge.rag_chat_service.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
@Slf4j
public class ApiKeyStore {

    @Value("${security.api-keys}")
    private String apiKeysProperty;

    private final Map<String, String> apiKeyToUser = new HashMap<>();

    @PostConstruct
    public void init() {
        if (apiKeysProperty == null || apiKeysProperty.isBlank()) {
            throw new IllegalStateException("API keys not configured");
        }

        String[] entries = apiKeysProperty.split(",");

        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            String userId = parts[0].trim();
            String apiKey = parts[1].trim();

            apiKeyToUser.put(apiKey, userId);
        }
        log.info("Loaded API keys property: " + apiKeysProperty);
    }

    public Optional<String> getUserIdByApiKey(String apiKey) {
        return Optional.ofNullable(apiKeyToUser.get(apiKey));
    }
}
