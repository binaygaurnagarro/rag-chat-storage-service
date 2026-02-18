package com.dge.rag_chat_service.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request model for creating a new chat session.
 */
public record CreateSessionRequest(
        @NotBlank String userId,
        @NotBlank String name
) {}
