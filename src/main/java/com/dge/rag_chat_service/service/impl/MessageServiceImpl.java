package com.dge.rag_chat_service.service.impl;

import com.dge.rag_chat_service.entity.ChatMessage;
import com.dge.rag_chat_service.entity.ChatSession;
import com.dge.rag_chat_service.exception.ResourceNotFoundException;
import com.dge.rag_chat_service.dto.CreateMessageRequest;
import com.dge.rag_chat_service.dto.MessageResponse;
import com.dge.rag_chat_service.repository.ChatMessageRepository;
import com.dge.rag_chat_service.repository.ChatSessionRepository;
import com.dge.rag_chat_service.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service implementation for managing chat messages within a session. It defines methods for adding new messages and retrieving messages with pagination support.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final ChatMessageRepository repository;

    private final ChatSessionRepository sessionRepository;

    /**
     * Service method to add a new message to a chat session.
     *
     */
    @Override
    public MessageResponse add(UUID sessionId, CreateMessageRequest req) {

        log.info("Save chat message for sessionId={} and request={}", sessionId, req);

        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Session not found"));

        ChatMessage m = ChatMessage.builder()
                .session(session)
                .sender(req.sender())
                .message(req.message())
                .context(req.context())
                .build();
        return getMessageResponse(repository.save(m));
    }

    /**
     * Service method to get chat messages for a session with pagination support.
     *
     */
    @Override
    public Page<MessageResponse> list(UUID sessionId, int page, int size) {

        log.info("Get paginated chat messages for sessionId={}", sessionId);

        // Validate session exists
        if (!sessionRepository.existsById(sessionId)) {
            log.error("Session not found for sessionId={}", sessionId);
            throw new ResourceNotFoundException(
                    "Session not found with id: " + sessionId);
        }

        // Sorting by creation time (Oldest ->Newest)
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );

        return repository.findBySessionId(sessionId, pageable).map(this::getMessageResponse);
    }

    MessageResponse getMessageResponse (ChatMessage chatMessage){
        return new MessageResponse(chatMessage.getId(),chatMessage.getSender(), chatMessage.getMessage(), chatMessage.getContext(), chatMessage.getCreatedAt());
    }

}
