package com.dge.rag_chat_service.model;

import java.time.Instant;
import java.util.Map;

/**
 * Response model for a message in a chat session.
 */
public record MessageResponse(

        Long id,
        SenderType sender,
        String message,
        Map<String, Object> context,
        Instant createdAt

) {}
