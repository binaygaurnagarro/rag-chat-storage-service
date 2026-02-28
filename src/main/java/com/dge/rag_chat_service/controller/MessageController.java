package com.dge.rag_chat_service.controller;

import com.dge.rag_chat_service.dto.CreateMessageRequest;
import com.dge.rag_chat_service.dto.MessageResponse;
import com.dge.rag_chat_service.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for managing chat messages.
 * Exposes APIs to Store chat messages and get chat messages with pagination.
 */
@RestController
@RequestMapping("/v1/api/sessions/{sessionId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService service;
    /**
     * Store chat messages with optional retrieved context
     *
     * @param sessionId session identifier
     * @param req contains message content, sender type and optional context
     * @return created chat message
     */
    @PostMapping
    public ResponseEntity<MessageResponse> add(@PathVariable UUID sessionId,
                                               @Valid @RequestBody CreateMessageRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.add(sessionId, req));
    }

    /**
     * Get chat messages for a session with pagination support.
     *
     * @param sessionId session identifier
     * @param page page number (0-based)
     * @param size page size
     * @return paginated list of chat messages for the session
     */
    @GetMapping
    public Page<MessageResponse> list(@PathVariable UUID sessionId,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return service.list(sessionId, page,size);
    }
}
