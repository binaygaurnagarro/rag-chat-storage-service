package com.dge.rag_chat_service.model;

import java.time.Instant;

/**
 * Response model for chat session details.
 */
public record SessionResponse(
        Long id,
        String name,
        String userId,
        boolean favorite,
        Instant createdAt,
        Instant updatedAt
) {}
