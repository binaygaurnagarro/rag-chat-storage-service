package com.dge.rag_chat_service.service.impl;

import com.dge.rag_chat_service.dto.FavoriteSessionRequest;
import com.dge.rag_chat_service.entity.ChatSession;
import com.dge.rag_chat_service.exception.ResourceNotFoundException;
import com.dge.rag_chat_service.dto.CreateSessionRequest;
import com.dge.rag_chat_service.dto.RenameSessionRequest;
import com.dge.rag_chat_service.dto.SessionResponse;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SessionServiceImpl.
 * Tests the service methods for creating, renaming, favouring and deleting chat sessions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionService Tests")
class SessionServiceImplTest {

    @Mock
    private ChatSessionRepository repository;

    @InjectMocks
    private SessionServiceImpl service;

    @BeforeEach
    void setUp() {
        // MockitoExtension initializes mocks
    }

    @Test
    @DisplayName("Create - success with valid request")
    void create_shouldSaveAndReturnSession() {
        CreateSessionRequest req = new CreateSessionRequest("My Session");
        String userid = "user1";

        ChatSession saved = new ChatSession();
        saved.setId(UUID.randomUUID());
        saved.setUserId(userid);
        saved.setName(req.name());
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        when(repository.save(any(ChatSession.class))).thenReturn(saved);

        SessionResponse result = service.create(req, userid);

        verify(repository).save(captor.capture());
        ChatSession toSave = captor.getValue();

        assertThat(toSave.getUserId()).isEqualTo(saved.getUserId());
        assertThat(toSave.getName()).isEqualTo(saved.getName());
        assertThat(result.id()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("Create - null request throws NPE")
    void create_withNullRequest_shouldThrowNpe() {
        assertThrows(NullPointerException.class, () -> service.create(null, "user1"));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Create - empty name")
    void create_withEmptyName_shouldSave() {
        String userid = "user1";
        CreateSessionRequest req = new CreateSessionRequest( "");

        ChatSession saved = new ChatSession();
        saved.setId(UUID.randomUUID());
        saved.setUserId(userid);
        saved.setName(req.name());

        when(repository.save(any(ChatSession.class))).thenReturn(saved);

        SessionResponse result = service.create(req, userid);

        assertThat(result.name()).isEqualTo(saved.getName());
        verify(repository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Create - repository throws exception")
    void create_whenRepositoryThrows_shouldPropagate() {
        String userid = "user1";
        CreateSessionRequest req = new CreateSessionRequest( "Session");
        when(repository.save(any(ChatSession.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> service.create(req, userid));
    }

    @Test
    @DisplayName("List - success with multiple sessions")
    void list_shouldReturnSessions() {
        String userId = "user123";
        int page = 0;
        int size = 2;

        ChatSession chatSession = new ChatSession();
        chatSession.setId(UUID.randomUUID());
        chatSession.setName("Chat Session 1");
        chatSession.setUserId(userId);
        chatSession.setFavorite(false);
        chatSession.setCreatedAt(Instant.now());
        chatSession.setUpdatedAt(Instant.now());

        ChatSession chatSession2 = new ChatSession();
        chatSession2.setId(UUID.randomUUID());
        chatSession2.setName("Chat Session 2");
        chatSession2.setUserId(userId);
        chatSession2.setFavorite(true);
        chatSession2.setCreatedAt(Instant.now());
        chatSession2.setUpdatedAt(Instant.now());

        Page<ChatSession> pageResult = new PageImpl<>(Arrays.asList(chatSession, chatSession2),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")), 2);

        when(repository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(pageResult);

        Page<SessionResponse> result = service.findAllByUserId(userId, page, size);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).isEqualTo(chatSession.getName());
        assertThat(result.getContent().get(1).name()).isEqualTo(chatSession2.getName());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByUserId(eq(userId), pageableCaptor.capture());
        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getPageNumber()).isEqualTo(page);
        assertThat(captured.getPageSize()).isEqualTo(size);

    }

    @Test
    @DisplayName("List - empty page")
    void list_whenEmpty_shouldReturnEmptyPage() {
        String userId = "user123";
        int page = 0;
        int size = 10;

        Page<ChatSession> empty = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")), 0);

        when(repository.findByUserId(eq(userId), any(Pageable.class))).thenReturn(empty);

        Page<SessionResponse> result = service.findAllByUserId(userId, page, size);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Rename - success with valid id and name")
    void rename_shouldUpdateNameAndUpdatedAt() {
        UUID id = UUID.randomUUID();
        ChatSession existingSession = ChatSession.builder()
                .id(id)
                .name("Old Name")
                .userId("u123")
                .build();
        when(repository.findById(id))
                .thenReturn(Optional.of(existingSession));
        when(repository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));

        RenameSessionRequest req = new RenameSessionRequest("New Name");

        SessionResponse result = service.rename(id, req);

        assertEquals("New Name", result.name());
    }

    @Test
    @DisplayName("Rename - session not found throws ResourceNotFoundException")
    void rename_whenSessionNotFound_shouldThrowEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        RenameSessionRequest req = new RenameSessionRequest("anything");

        assertThrows(ResourceNotFoundException.class, () -> service.rename(id, req));
        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rename - null request throws NPE")
    void rename_withNullRequest_shouldThrowNpe() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setName("old");
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(NullPointerException.class, () -> service.rename(id, null));
        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rename - empty name")
    void rename_withEmptyName_shouldUpdate() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setName("Old");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));

        RenameSessionRequest req = new RenameSessionRequest("");

        SessionResponse result = service.rename(id, req);

        assertThat(result.name()).isEqualTo("");
        verify(repository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Rename - repository throws exception")
    void rename_whenRepositoryThrows_shouldPropagate() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class)))
                .thenThrow(new RuntimeException("Save failed"));

        RenameSessionRequest req = new RenameSessionRequest("New");

        assertThrows(RuntimeException.class, () -> service.rename(id, req));
    }

    @Test
    @DisplayName("Favorite - set favorite to true")
    void favorite_shouldSetFavoriteToTrue() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setFavorite(false);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));
        FavoriteSessionRequest req = new FavoriteSessionRequest(true);

        SessionResponse result = service.favorite(id, req);

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(repository).save(captor.capture());
        ChatSession saved = captor.getValue();

        assertThat(saved.isFavorite()).isTrue();
        assertThat(result.favorite()).isTrue();
    }

    @Test
    @DisplayName("Favorite - set favorite to false")
    void favorite_shouldSetFavoriteToFalse() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setFavorite(true);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));
        FavoriteSessionRequest req = new FavoriteSessionRequest(false);

        SessionResponse result = service.favorite(id, req);

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(repository).save(captor.capture());
        ChatSession saved = captor.getValue();

        assertThat(saved.isFavorite()).isFalse();
        assertThat(result.favorite()).isFalse();
    }

    @Test
    @DisplayName("Favorite - session not found throws ResourceNotFoundException")
    void favorite_whenSessionNotFound_shouldThrowEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        FavoriteSessionRequest req = new FavoriteSessionRequest(true);
        assertThrows(ResourceNotFoundException.class, () -> service.favorite(id, req));
        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Favorite - repository throws exception")
    void favorite_whenRepositoryThrows_shouldPropagate() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class)))
                .thenThrow(new RuntimeException("Save failed"));
        FavoriteSessionRequest req = new FavoriteSessionRequest(true);
        assertThrows(RuntimeException.class, () -> service.favorite(id, req));
    }

    @Test
    @DisplayName("Delete - success with valid id")
    void delete_shouldInvokeRepository() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        doNothing().when(repository).delete(any(ChatSession.class));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    @DisplayName("Delete - session not found throws ResourceNotFoundException")
    void delete_whenSessionNotFound_shouldThrowEntityNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(id));
        verify(repository).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete - repository throws exception")
    void delete_whenRepositoryThrows_shouldPropagate() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        doThrow(new RuntimeException("db error")).when(repository).delete(any(ChatSession.class));

        assertThrows(RuntimeException.class, () -> service.delete(id));
        verify(repository).delete(any());
    }

}