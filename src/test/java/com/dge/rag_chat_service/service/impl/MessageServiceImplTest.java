package com.dge.rag_chat_service.service.impl;

import com.dge.rag_chat_service.entity.ChatMessage;
import com.dge.rag_chat_service.entity.ChatSession;
import com.dge.rag_chat_service.exception.EntityNotFoundException;
import com.dge.rag_chat_service.dto.CreateMessageRequest;
import com.dge.rag_chat_service.dto.MessageResponse;
import com.dge.rag_chat_service.dto.SenderType;
import com.dge.rag_chat_service.repository.ChatMessageRepository;
import com.dge.rag_chat_service.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MessageServiceImpl.
 * Tests the service methods for adding messages and listing messages with pagination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService Tests")
class MessageServiceImplTest {

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private ChatSessionRepository sessionRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        // MockitoExtension initializes mocks
    }

    @Test
    @DisplayName("Add - success with valid request")
    void add_shouldSaveAndReturnMessage() {
        Long sessionId = 11L;
        CreateMessageRequest req = new CreateMessageRequest(SenderType.USER, "Hello world", null);

        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);

        when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(chatSession));

        ChatMessage saved = new ChatMessage();
        saved.setId(100L);
        saved.setSession(chatSession);
        saved.setSender(SenderType.USER);
        saved.setMessage("Hello world");
        saved.setContext(null);
        saved.setCreatedAt(Instant.now());

        when(messageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        MessageResponse result = messageService.add(sessionId, req);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(captor.capture());
        ChatMessage toSave = captor.getValue();

        assertThat(toSave.getSession()).isEqualTo(chatSession);
        assertThat(toSave.getMessage()).isEqualTo("Hello world");
        assertThat(toSave.getSender()).isEqualTo(SenderType.USER);
        assertThat(toSave.getContext()).isNull();

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.message()).isEqualTo("Hello world");
    }

    @Test
    @DisplayName("Add - with context")
    void add_withContextShouldSaveAndReturn() {
        Long sessionId = 12L;
        Map<String, Object> contextData = Map.of("key1", "value1", "key2", 123);
        CreateMessageRequest req = new CreateMessageRequest(SenderType.AI, "Response", contextData);

        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);

        when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(chatSession));

        ChatMessage saved = new ChatMessage();
        saved.setId(101L);
        saved.setSession(chatSession);
        saved.setSender(SenderType.AI);
        saved.setMessage("Response");
        saved.setContext(contextData);

        when(messageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        MessageResponse result = messageService.add(sessionId, req);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messageRepository).save(captor.capture());

        assertThat(captor.getValue().getContext()).isEqualTo(contextData);
        assertThat(result.context()).isEqualTo(contextData);
    }

    @Test
    @DisplayName("Add - session not found throws EntityNotFoundException")
    void add_whenSessionNotFound_shouldThrowEntityNotFound() {
        Long sessionId = 999L;
        CreateMessageRequest req = new CreateMessageRequest(SenderType.USER, "message", null);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> messageService.add(sessionId, req));
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add - empty message")
    void add_withEmptyMessage_shouldSave() {
        Long sessionId = 13L;
        CreateMessageRequest req = new CreateMessageRequest(SenderType.USER, "", null);

        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);

        when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(chatSession));

        ChatMessage saved = new ChatMessage();
        saved.setId(102L);
        saved.setSession(chatSession);
        saved.setMessage("");

        when(messageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        MessageResponse result = messageService.add(sessionId, req);

        assertThat(result.message()).isEmpty();
        verify(messageRepository).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("Add - different sender types")
    void add_withDifferentSenders_shouldSave() {
        Long sessionId = 14L;
        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);

        when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(chatSession));

        for (SenderType senderType : SenderType.values()) {
            CreateMessageRequest req = new CreateMessageRequest(senderType, "test", null);

            ChatMessage saved = new ChatMessage();
            saved.setId(103L);
            saved.setSender(senderType);
            saved.setSession(chatSession);

            when(messageRepository.save(any(ChatMessage.class))).thenReturn(saved);

            MessageResponse result = messageService.add(sessionId, req);

            assertThat(result.sender()).isEqualTo(senderType);
        }
    }

    @Test
    @DisplayName("Add - repository throws exception")
    void add_whenRepositoryThrows_shouldPropagate() {
        Long sessionId = 15L;
        CreateMessageRequest req = new CreateMessageRequest(SenderType.USER, "test", null);

        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);

        when(sessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(chatSession));
        when(messageRepository.save(any(ChatMessage.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> messageService.add(sessionId, req));
    }

    @Test
    @DisplayName("List - success with multiple messages")
    void list_shouldReturnPagedMessages() {
        Long sessionId = 20L;
        int page = 0;
        int size = 2;

        when(sessionRepository.existsById(eq(sessionId))).thenReturn(true);

        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);

        ChatMessage m1 = new ChatMessage();
        m1.setId(1L);
        m1.setSession(chatSession);
        m1.setSender(SenderType.USER);
        m1.setMessage("message 1");
        m1.setCreatedAt(Instant.parse("2024-01-01T10:00:00Z"));

        ChatMessage m2 = new ChatMessage();
        m2.setId(2L);
        m2.setSession(chatSession);
        m2.setSender(SenderType.AI);
        m2.setMessage("message 2");
        m2.setCreatedAt(Instant.parse("2024-01-01T11:00:00Z"));

        Page<ChatMessage> pageResult = new PageImpl<>(Arrays.asList(m1, m2),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")), 2);

        when(messageRepository.findBySessionId(eq(sessionId), any(Pageable.class))).thenReturn(pageResult);

        Page<MessageResponse> result = messageService.list(sessionId, page, size);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).message()).isEqualTo("message 1");
        assertThat(result.getContent().get(1).message()).isEqualTo("message 2");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findBySessionId(eq(sessionId), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(page);
        assertThat(captured.getPageSize()).isEqualTo(size);
    }

    @Test
    @DisplayName("List - empty page")
    void list_whenEmpty_shouldReturnEmptyPage() {
        Long sessionId = 30L;
        int page = 0;
        int size = 10;

        when(sessionRepository.existsById(eq(sessionId))).thenReturn(true);

        Page<ChatMessage> empty = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")), 0);
        when(messageRepository.findBySessionId(eq(sessionId), any(Pageable.class))).thenReturn(empty);

        Page<MessageResponse> result = messageService.list(sessionId, page, size);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(sessionRepository).existsById(sessionId);
    }

    @Test
    @DisplayName("List - session not found throws EntityNotFoundException")
    void list_whenSessionNotFound_shouldThrowEntityNotFound() {
        Long sessionId = 99L;

        when(sessionRepository.existsById(sessionId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> messageService.list(sessionId, 0, 10));
        verify(messageRepository, never()).findBySessionId(any(), any());
    }

    @Test
    @DisplayName("List - different page sizes")
    void list_withDifferentPageSizes_shouldAdjustPageable() {
        Long sessionId = 40L;
        when(sessionRepository.existsById(eq(sessionId))).thenReturn(true);

        Page<ChatMessage> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "createdAt")), 0);
        when(messageRepository.findBySessionId(eq(sessionId), any(Pageable.class))).thenReturn(emptyPage);

        messageService.list(sessionId, 0, 5);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findBySessionId(eq(sessionId), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("List - pagination with multiple pages")
    void list_withMultiplePages_shouldReturnCorrectPage() {
        Long sessionId = 50L;
        int page = 1;
        int size = 1;

        when(sessionRepository.existsById(eq(sessionId))).thenReturn(true);

        ChatMessage m1 = new ChatMessage();
        m1.setId(10L);
        m1.setMessage("page 2 message");

        Page<ChatMessage> pageResult = new PageImpl<>(Arrays.asList(m1),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")), 5);

        when(messageRepository.findBySessionId(eq(sessionId), any(Pageable.class))).thenReturn(pageResult);

        Page<MessageResponse> result = messageService.list(sessionId, page, size);

        assertThat(result.getNumber()).isEqualTo(page);
        assertThat(result.getTotalPages()).isEqualTo(5);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("List - sorting order")
    void list_shouldSortByCreatedAtAscending() {
        Long sessionId = 60L;

        when(sessionRepository.existsById(eq(sessionId))).thenReturn(true);

        Page<ChatMessage> pageResult = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "createdAt")), 0);
        when(messageRepository.findBySessionId(eq(sessionId), any(Pageable.class))).thenReturn(pageResult);

        messageService.list(sessionId, 0, 10);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findBySessionId(eq(sessionId), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getSort()).isNotEmpty();
    }

    @Test
    @DisplayName("List - repository throws exception")
    void list_whenRepositoryThrows_shouldPropagate() {
        Long sessionId = 70L;

        when(sessionRepository.existsById(eq(sessionId))).thenReturn(true);
        when(messageRepository.findBySessionId(eq(sessionId), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> messageService.list(sessionId, 0, 10));
    }

}