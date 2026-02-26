package com.dge.rag_chat_service.service.impl;

import com.dge.rag_chat_service.entity.ChatSession;
import com.dge.rag_chat_service.exception.ResourceNotFoundException;
import com.dge.rag_chat_service.dto.CreateSessionRequest;
import com.dge.rag_chat_service.dto.RenameSessionRequest;
import com.dge.rag_chat_service.dto.SessionResponse;
import com.dge.rag_chat_service.repository.ChatSessionRepository;
import com.dge.rag_chat_service.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service layer responsible for handling
 * business logic related to chat sessions.
 *
 */
@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

    private final ChatSessionRepository repository;

    public SessionServiceImpl(ChatSessionRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new chat session for a user.
     */
    @Override
    public SessionResponse create(CreateSessionRequest req, String userId) {

        log.info("Create session for request={}", req);
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .name(req.name())
                .build();
        return getSessionResponse(repository.save(session));
    }

    /**
     * Retrieves all chat sessions for a specific user with pagination support.
     *
     */
    @Override
    public Page<SessionResponse> findAllByUserId(String userId, int page, int size) {
        log.info("Get chat sessions for userid={}", userId);
        // Sorting by creation time (Newest ->Oldest)
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return repository.findByUserId(userId, pageable).map(this::getSessionResponse);
    }

    /**
     * Rename the chat session name
     */
    @Override
    public SessionResponse rename(UUID id, RenameSessionRequest req) {
        log.info("Rename session for id={} and request={}", id, req);
        ChatSession session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.setName(req.name());
        return getSessionResponse(repository.save(session));
    }

    /**
     * Update the favorite status of a chat session.
     */
    @Override
    public SessionResponse favorite(UUID id, boolean value) {
        log.info("Update favorite session for id={} and favorite={}", id, value);
        ChatSession session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.setFavorite(value);
        return getSessionResponse(repository.save(session));
    }

    /**
     * Deletes a chat session and all associated messages.
     */

    @Override
    public void delete(UUID id) {
        log.info("Delete session for id={}", id);
        ChatSession session = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Session not found"));

        repository.delete(session);
    }

    SessionResponse getSessionResponse(ChatSession session){
        return new SessionResponse(session.getId(),session.getName(),session.getUserId(),session.isFavorite(),session.getCreatedAt(),session.getUpdatedAt());
    }


}
