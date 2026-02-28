package com.dge.rag_chat_service.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request model for renaming an existing chat session.
 */
public record RenameSessionRequest(@NotBlank String name) {

}
