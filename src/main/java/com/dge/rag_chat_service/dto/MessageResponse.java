package com.dge.rag_chat_service.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response model for a message in a chat session.
 */
public record MessageResponse(

        UUID id,
        SenderType sender,
        String message,
        Map<String, Object> context,
        Instant createdAt

) {}
