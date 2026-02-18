package com.dge.rag_chat_service.controller;

import com.dge.rag_chat_service.model.CreateMessageRequest;
import com.dge.rag_chat_service.model.MessageResponse;
import com.dge.rag_chat_service.model.SenderType;
import com.dge.rag_chat_service.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for MessageController.
 * Tests the REST endpoints for adding and listing messages.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController Tests")
class MessageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
    }

    @Test
    void testAddMessageSuccess() throws Exception {
        Long sessionId = 1L;

        MessageResponse messageResponse = new MessageResponse(1L, SenderType.USER, "Test message", null, null);

        when(messageService.add(eq(sessionId), any(CreateMessageRequest.class))).thenReturn(messageResponse);

        mockMvc.perform(post("/v1/api/sessions/{sessionId}/messages", sessionId)
                        .contentType("application/json")
                        .content("{\"sender\":\"USER\",\"message\":\"Test message\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("Test message"));

        verify(messageService).add(eq(sessionId), any(CreateMessageRequest.class));
    }

    @Test
    void testAddMessageBadRequest() throws Exception {
        Long sessionId = 1L;
        mockMvc.perform(post("/v1/api/sessions/{sessionId}/messages", sessionId)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListMessagesSuccess() throws Exception {
        Long sessionId = 1L;
        int page = 0;
        int size = 10;

        MessageResponse messageResponse1 = new MessageResponse(1L, SenderType.USER, "Message 1", null, null);
        MessageResponse messageResponse2 = new MessageResponse(2L, SenderType.USER, "Message 2", null, null);


        List<MessageResponse> messages = Arrays.asList(messageResponse1, messageResponse2);
        Page<MessageResponse> pagedMessages = new PageImpl<>(messages, PageRequest.of(page, size), 2);

        when(messageService.list(sessionId, page, size)).thenReturn(pagedMessages);

        mockMvc.perform(get("/v1/api/sessions/{sessionId}/messages", sessionId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[1].id").value(2L));

        verify(messageService).list(sessionId, page, size);
    }

    @Test
    void testListMessagesEmptyPage() throws Exception {
        Long sessionId = 1L;

        Page<MessageResponse> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(messageService.list(sessionId, 0, 10)).thenReturn(emptyPage);

        mockMvc.perform(get("/v1/api/sessions/{sessionId}/messages", sessionId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

}