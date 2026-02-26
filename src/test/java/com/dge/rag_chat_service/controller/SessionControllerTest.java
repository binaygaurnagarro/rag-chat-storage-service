package com.dge.rag_chat_service.controller;

import com.dge.rag_chat_service.dto.RenameSessionRequest;
import com.dge.rag_chat_service.dto.SessionResponse;
import com.dge.rag_chat_service.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for SessionController.
 * Tests the REST endpoints for creating, renaming, favouring and deleting chat sessions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SessionController Tests")
class SessionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private SessionController sessionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sessionController).build();
    }

    @Test
    @DisplayName("Rename session - success")
    void testRenameSessionSuccess() throws Exception {
        UUID sessionId = UUID.randomUUID();

        when(sessionService.rename(any(), any(RenameSessionRequest.class))).thenReturn(new SessionResponse(sessionId, "Updated name", "user123", false, Instant.now(), Instant.now()));

        mockMvc.perform(put("/v1/api/sessions/{id}/rename", sessionId)
                        .contentType("application/json")
                        .content("""
                                {
                                    "name":"Updated name"
                                }
                                """))
                .andExpect(status().isOk());

        verify(sessionService).rename(eq(sessionId), any(RenameSessionRequest.class));
    }

    @Test
    @DisplayName("Mark session as favorite - success")
    void testFavoriteSessionSuccess() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(sessionService.favorite(any(), anyBoolean())).thenReturn(new SessionResponse(sessionId, "Session Name", "user123", true, Instant.now(), Instant.now()));

        mockMvc.perform(put("/v1/api/sessions/{id}/favorite", sessionId)
                        .param("value", "true"))
                .andExpect(status().isOk());

        verify(sessionService).favorite(sessionId, true);
    }

    @Test
    @DisplayName("Unmark session as favorite - success")
    void testUnfavoriteSessionSuccess() throws Exception {
        UUID sessionId = UUID.randomUUID();
        when(sessionService.favorite(any(), anyBoolean())).thenReturn(new SessionResponse(sessionId, "Session Name", "user123", false, Instant.now(), Instant.now()));

        mockMvc.perform(put("/v1/api/sessions/{id}/favorite", sessionId)
                        .param("value", "false"))
                .andExpect(status().isOk());

        verify(sessionService).favorite(sessionId, false);
    }

    @Test
    @DisplayName("Favorite session - missing parameter")
    void testFavoriteSessionMissingParameter() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(put("/v1/api/sessions/{id}/favorite", sessionId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Delete session - success")
    void testDeleteSessionSuccess() throws Exception {
        UUID sessionId = UUID.randomUUID();
        doNothing().when(sessionService).delete(any());

        mockMvc.perform(delete("/v1/api/sessions/{id}", sessionId))
                .andExpect(status().isNoContent());

        verify(sessionService).delete(sessionId);
    }

}