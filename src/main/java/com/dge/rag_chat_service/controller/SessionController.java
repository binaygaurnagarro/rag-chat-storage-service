package com.dge.rag_chat_service.controller;

import com.dge.rag_chat_service.dto.CreateSessionRequest;
import com.dge.rag_chat_service.dto.RenameSessionRequest;
import com.dge.rag_chat_service.dto.SessionResponse;
import com.dge.rag_chat_service.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing chat sessions.
 * Exposes APIs to create, update and delete chat sessions.
 */

@RestController
@RequestMapping("/v1/api/sessions")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
     }

    /**
     * Creates a new chat session.
     *
     * @param req contains userId and session name
     * @return created session
     */
    @PostMapping
    public ResponseEntity<SessionResponse> create(@Valid @RequestBody CreateSessionRequest req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(req));
    }

    /**
     * Retrieves all chat sessions for a specific user with pagination support.
     *
     * @param userId user identifier
     * @param page page number (0-based)
     * @param size page size
     * @return paginated list of chat sessions for the user
     */
    @GetMapping("/{userId}")
    public Page<SessionResponse> findAllByUserId(@PathVariable String userId,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return service.findAllByUserId(userId, page, size);
    }

    /**
     * Rename the chat session.
     *
     * @param id session identifier
     * @param req contains session name
     * @return updated session with new favorite status
     *
     */
    @PutMapping("/{id}/rename")
    public ResponseEntity<SessionResponse> rename(@PathVariable UUID id, @Valid @RequestBody RenameSessionRequest req) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.rename(id, req));
    }

    /**
     * Updates favorites for a chat session.
     *
     * @param id session identifier
     * @param value true to mark as favorite, false to unmark
     * @return updated session with new favorite status
     *
     */
    @PutMapping("/{id}/favorite")
    public ResponseEntity<SessionResponse> favorite(@PathVariable UUID id, @RequestParam boolean value) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.favorite(id, value));
    }

    /**
     * Deletes a chat session by id.
     *
     * @param id session identifier
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>  delete(@PathVariable UUID id) {
        service.delete(id);

        return ResponseEntity.noContent().build();
    }

}
