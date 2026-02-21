package com.dge.rag_chat_service.service;

import com.dge.rag_chat_service.dto.CreateMessageRequest;
import com.dge.rag_chat_service.dto.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Service interface for managing chat messages within a session. It defines methods for adding new messages and retrieving messages with pagination support.
 *
 */
public interface MessageService {

    /**
     * Service method to add a new message to a chat session.
     *
     */
    MessageResponse add(UUID sessionId, CreateMessageRequest req);

    /**
     * Service method to get chat messages for a session with pagination support.
     *
     */
    Page<MessageResponse> list(UUID sessionId, int page, int size);
}
