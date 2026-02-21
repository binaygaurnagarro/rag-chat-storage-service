package com.dge.rag_chat_service.repository;

import com.dge.rag_chat_service.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for managing ChatMessage entities. It extends JpaRepository to provide basic CRUD operations and defines a custom method to find messages by session ID with pagination support.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    Page<ChatMessage> findBySessionId(UUID sessionId, Pageable pageable);
}
