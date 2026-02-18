package com.dge.rag_chat_service.service.impl;

import com.dge.rag_chat_service.entity.ChatSession;
import com.dge.rag_chat_service.exception.EntityNotFoundException;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        CreateSessionRequest req = new CreateSessionRequest("user1", "My Session");

        ChatSession saved = new ChatSession();
        saved.setId(1L);
        saved.setUserId("user1");
        saved.setName("My Session");
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        when(repository.save(any(ChatSession.class))).thenReturn(saved);

        SessionResponse result = service.create(req);

        verify(repository).save(captor.capture());
        ChatSession toSave = captor.getValue();

        assertThat(toSave.getUserId()).isEqualTo("user1");
        assertThat(toSave.getName()).isEqualTo("My Session");
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Create - null request throws NPE")
    void create_withNullRequest_shouldThrowNpe() {
        assertThrows(NullPointerException.class, () -> service.create(null));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Create - empty name")
    void create_withEmptyName_shouldSave() {
        CreateSessionRequest req = new CreateSessionRequest("user1", "");

        ChatSession saved = new ChatSession();
        saved.setId(2L);
        saved.setUserId("user1");
        saved.setName("");

        when(repository.save(any(ChatSession.class))).thenReturn(saved);

        SessionResponse result = service.create(req);

        assertThat(result.name()).isEqualTo("");
        verify(repository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Create - null userId")
    void create_withNullUserId_shouldSave() {
        CreateSessionRequest req = new CreateSessionRequest(null, "Session");

        ChatSession saved = new ChatSession();
        saved.setId(3L);
        saved.setUserId(null);
        saved.setName("Session");

        when(repository.save(any(ChatSession.class))).thenReturn(saved);

        SessionResponse result = service.create(req);

        assertThat(result.userId()).isNull();
        verify(repository).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("Create - repository throws exception")
    void create_whenRepositoryThrows_shouldPropagate() {
        CreateSessionRequest req = new CreateSessionRequest("user1", "Session");
        when(repository.save(any(ChatSession.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("List - success with multiple sessions")
    void list_shouldReturnSessions() {
        String userId = "user123";

        ChatSession chatSession = new ChatSession();
        chatSession.setId(1L);
        chatSession.setName("Chat Session 1");
        chatSession.setUserId(userId);
        chatSession.setFavorite(false);
        chatSession.setCreatedAt(Instant.now());
        chatSession.setUpdatedAt(Instant.now());

        ChatSession chatSession2 = new ChatSession();
        chatSession2.setId(2L);
        chatSession2.setName("Chat Session 2");
        chatSession2.setUserId(userId);
        chatSession2.setFavorite(true);
        chatSession2.setCreatedAt(Instant.now());
        chatSession2.setUpdatedAt(Instant.now());


        List<ChatSession> list= Arrays.asList(chatSession, chatSession2);

        when(repository.findByUserId(eq(userId))).thenReturn(list);

        List<SessionResponse> result = service.findAllByUserId(userId);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).name()).isEqualTo(chatSession.getName());
        assertThat(result.get(1).name()).isEqualTo(chatSession2.getName());

        verify(repository).findByUserId(eq(userId));
    }

    @Test
    @DisplayName("List - empty page")
    void list_whenEmpty_shouldReturnEmptyPage() {
        String userId = "user123";

        when(repository.findByUserId(eq(userId))).thenReturn(Collections.emptyList());

        List<SessionResponse> result = service.findAllByUserId(userId);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Rename - success with valid id and name")
    void rename_shouldUpdateNameAndUpdatedAt() {
        Long id = 10L;
        ChatSession existingSession = ChatSession.builder()
                .id(1L)
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
    @DisplayName("Rename - session not found throws EntityNotFoundException")
    void rename_whenSessionNotFound_shouldThrowEntityNotFound() {
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        RenameSessionRequest req = new RenameSessionRequest("anything");

        assertThrows(EntityNotFoundException.class, () -> service.rename(id, req));
        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Rename - null request throws NPE")
    void rename_withNullRequest_shouldThrowNpe() {
        Long id = 5L;
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
        Long id = 11L;
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
        Long id = 12L;
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
        Long id = 2L;
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setFavorite(false);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponse result = service.favorite(id, true);

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(repository).save(captor.capture());
        ChatSession saved = captor.getValue();

        assertThat(saved.isFavorite()).isTrue();
        assertThat(result.favorite()).isTrue();
    }

    @Test
    @DisplayName("Favorite - set favorite to false")
    void favorite_shouldSetFavoriteToFalse() {
        Long id = 3L;
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setFavorite(true);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(inv -> inv.getArgument(0));

        SessionResponse result = service.favorite(id, false);

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(repository).save(captor.capture());
        ChatSession saved = captor.getValue();

        assertThat(saved.isFavorite()).isFalse();
        assertThat(result.favorite()).isFalse();
    }

    @Test
    @DisplayName("Favorite - session not found throws EntityNotFoundException")
    void favorite_whenSessionNotFound_shouldThrowEntityNotFound() {
        Long id = 123L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.favorite(id, true));
        verify(repository).findById(id);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Favorite - repository throws exception")
    void favorite_whenRepositoryThrows_shouldPropagate() {
        Long id = 4L;
        ChatSession existing = new ChatSession();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class)))
                .thenThrow(new RuntimeException("Save failed"));

        assertThrows(RuntimeException.class, () -> service.favorite(id, true));
    }

    @Test
    @DisplayName("Delete - success with valid id")
    void delete_shouldInvokeRepository() {
        Long id = 7L;
        ChatSession existing = new ChatSession();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        doNothing().when(repository).delete(any(ChatSession.class));

        service.delete(id);

        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    @DisplayName("Delete - session not found throws EntityNotFoundException")
    void delete_whenSessionNotFound_shouldThrowEntityNotFound() {
        Long id = 50L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.delete(id));
        verify(repository).findById(id);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("Delete - repository throws exception")
    void delete_whenRepositoryThrows_shouldPropagate() {
        Long id = 8L;
        ChatSession existing = new ChatSession();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        doThrow(new RuntimeException("db error")).when(repository).delete(any(ChatSession.class));

        assertThrows(RuntimeException.class, () -> service.delete(id));
        verify(repository).delete(any());
    }

}