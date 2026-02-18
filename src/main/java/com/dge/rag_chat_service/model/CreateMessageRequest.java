package com.dge.rag_chat_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request model for creating a new message in a chat session.
 */
public record CreateMessageRequest(
        @NotNull SenderType sender,
        @NotBlank String message,
        Map<String, Object> context
) {}
