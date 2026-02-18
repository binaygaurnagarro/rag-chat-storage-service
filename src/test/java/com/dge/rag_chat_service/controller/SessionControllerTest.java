package com.dge.rag_chat_service.controller;

import com.dge.rag_chat_service.dto.CreateSessionRequest;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for SessionController.
 * Tests the REST endpoints for creating, renaming, favoriting and deleting chat sessions.
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
    @DisplayName("Create session - success")
    void testCreateSessionSuccess() throws Exception {

        SessionResponse sessionResponse = new SessionResponse(1L, "New Chat Session", "123",false, Instant.now(),Instant.now());

        when(sessionService.create(any(CreateSessionRequest.class))).thenReturn(sessionResponse);

        mockMvc.perform(post("/v1/api/sessions")
                        .contentType("application/json")
                        .content("{\"userId\":\"123\",\"name\":\"New Chat Session\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Chat Session"));

        verify(sessionService).create(any(CreateSessionRequest.class));
    }

    @Test
    @DisplayName("Create session - failure due to missing fields")
    void testCreateSessionBadRequest() throws Exception {

        mockMvc.perform(post("/v1/api/sessions")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFindAllSessionByUserIdSuccess() throws Exception {
        String userId = "user123";

        SessionResponse sessionResponse1 = new SessionResponse(1L, "Session 1", userId,false, Instant.now(),Instant.now());
        SessionResponse sessionResponse2 = new SessionResponse(2L, "Session 2", userId,true, Instant.now(),Instant.now());


        List<SessionResponse> sessionResponses = Arrays.asList(sessionResponse1, sessionResponse2);

        when(sessionService.findAllByUserId(userId)).thenReturn(sessionResponses);

        mockMvc.perform(get("/v1/api/sessions/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(sessionService).findAllByUserId(userId);
    }

    @Test
    void testFindAllSessionByUserIdEmptyPage() throws Exception {
        String userId = "user123";
        when(sessionService.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/api/sessions/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Rename session - success")
    void testRenameSessionSuccess() throws Exception {
        Long sessionId = 1L;

        when(sessionService.rename(anyLong(), any(RenameSessionRequest.class))).thenReturn(new SessionResponse(sessionId, "Updated name", "user123", false, Instant.now(), Instant.now()));

        mockMvc.perform(put("/v1/api/sessions/{id}/rename", sessionId)
                        .contentType("application/json")
                        .content("{\"name\":\"Updated name\"}"))
                .andExpect(status().isOk());

        verify(sessionService).rename(eq(sessionId), any(RenameSessionRequest.class));
    }

    @Test
    @DisplayName("Rename session - failure due to missing name")
    void testRenameSessionBadRequest() throws Exception {
        Long sessionId = 1L;
        mockMvc.perform(put("/v1/api/sessions/{id}/rename", sessionId)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Mark session as favorite - success")
    void testFavoriteSessionSuccess() throws Exception {
        Long sessionId = 1L;
        when(sessionService.favorite(anyLong(), anyBoolean())).thenReturn(new SessionResponse(sessionId, "Session Name", "user123", true, Instant.now(), Instant.now()));

        mockMvc.perform(put("/v1/api/sessions/{id}/favorite", sessionId)
                        .param("value", "true"))
                .andExpect(status().isOk());

        verify(sessionService).favorite(sessionId, true);
    }

    @Test
    @DisplayName("Unmark session as favorite - success")
    void testUnfavoriteSessionSuccess() throws Exception {
        Long sessionId = 1L;
        when(sessionService.favorite(anyLong(), anyBoolean())).thenReturn(new SessionResponse(sessionId, "Session Name", "user123", false, Instant.now(), Instant.now()));

        mockMvc.perform(put("/v1/api/sessions/{id}/favorite", sessionId)
                        .param("value", "false"))
                .andExpect(status().isOk());

        verify(sessionService).favorite(sessionId, false);
    }

    @Test
    @DisplayName("Favorite session - missing parameter")
    void testFavoriteSessionMissingParameter() throws Exception {
        Long sessionId = 1L;

        mockMvc.perform(put("/v1/api/sessions/{id}/favorite", sessionId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Delete session - success")
    void testDeleteSessionSuccess() throws Exception {
        Long sessionId = 1L;
        doNothing().when(sessionService).delete(anyLong());

        mockMvc.perform(delete("/v1/api/sessions/{id}", sessionId))
                .andExpect(status().isNoContent());

        verify(sessionService).delete(sessionId);
    }

}