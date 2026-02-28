package com.dge.rag_chat_service.service.impl;

import com.dge.rag_chat_service.dto.FavoriteSessionRequest;
import com.dge.rag_chat_service.entity.ChatSession;
import com.dge.rag_chat_service.exception.ResourceNotFoundException;
import com.dge.rag_chat_service.dto.CreateSessionRequest;
import com.dge.rag_chat_service.dto.RenameSessionRequest;
import com.dge.rag_chat_service.dto.SessionResponse;
import com.dge.rag_chat_service.repository.ChatSessionRepository;
import com.dge.rag_chat_service.service.SessionService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ChatSessionRepository repository;

    /**
     * Creates a new chat session for a user.
     */
    @Override
    public SessionResponse create(CreateSessionRequest req, String userId) {

        log.info("Create session for request={}", req);
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .name(req.name() != null ? req.name() : "New Chat")
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
        ChatSession session = getSession(id);
        session.setName(req.name());
        return getSessionResponse(repository.save(session));
    }

    /**
     * Update the favorite status of a chat session.
     */
    @Override
    public SessionResponse favorite(UUID id, FavoriteSessionRequest req) {
        log.info("Update favorite session for id={} and favorite={}", id, req.favorite());
        ChatSession session = getSession(id);
        session.setFavorite(req.favorite());
        return getSessionResponse(repository.save(session));
    }

    /**
     * Deletes a chat session and all associated messages.
     */

    @Override
    public void delete(UUID id) {
        log.info("Delete session for id={}", id);
        ChatSession session = getSession(id);

        repository.delete(session);
    }

    private ChatSession getSession(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    SessionResponse getSessionResponse(ChatSession session){
        return new SessionResponse(session.getId(),session.getName(),session.getUserId(),session.isFavorite(),session.getCreatedAt(),session.getUpdatedAt());
    }


}
