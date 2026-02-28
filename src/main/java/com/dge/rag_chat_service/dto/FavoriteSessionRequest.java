package com.dge.rag_chat_service.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request model for marking a chat session as favorite or unfavorite.
 */
public record FavoriteSessionRequest(@NotNull Boolean favorite) {
}
