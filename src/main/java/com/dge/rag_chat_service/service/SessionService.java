package com.dge.rag_chat_service.service;

import com.dge.rag_chat_service.dto.CreateSessionRequest;
import com.dge.rag_chat_service.dto.RenameSessionRequest;
import com.dge.rag_chat_service.dto.SessionResponse;
import org.springframework.data.domain.Page;

/**
 * Session Service defines the operations for managing chat sessions, including creating, renaming, favoriting, and deleting sessions.
 * Each session is associated with a user and can contain multiple messages.
 * The service ensures that session management is handled efficiently and provides necessary responses for client interactions.
 *
 */
public interface SessionService {

    /**
     * Creates a new chat session for a user.
     */
    SessionResponse create(CreateSessionRequest req);

    /**
     * Retrieves all chat sessions for a specific user.
     */
    Page<SessionResponse> findAllByUserId(String userId, int page, int size);

    /**
     * Rename the chat session name
     */
    SessionResponse rename(Long id, RenameSessionRequest req);

    /**
     * Update the favorite status of a chat session.
     */
    SessionResponse favorite(Long id, boolean value);

    /**
     * Deletes a chat session and all associated messages.
     */
    void delete(Long id);
}
