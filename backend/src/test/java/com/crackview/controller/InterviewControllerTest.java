package com.crackview.controller;

import com.crackview.agent.InterviewAgentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterviewController.class)
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InterviewAgentService interviewAgent;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/interview/chat - should return agent reply")
    void chat_shouldReturnReply() throws Exception {
        when(interviewAgent.chat("sess-1", "Hello"))
                .thenReturn("你好！准备好开始面试了吗？");

        mockMvc.perform(post("/api/interview/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("sessionId", "sess-1", "message", "Hello"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-1"))
                .andExpect(jsonPath("$.reply").value("你好！准备好开始面试了吗？"));
    }

    @Test
    @DisplayName("POST /api/interview/chat - should return 400 for missing fields")
    void chat_shouldReturn400_forMissingFields() throws Exception {
        mockMvc.perform(post("/api/interview/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/interview/session/{id} - should clear session")
    void clearSession_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/interview/session/sess-1"))
                .andExpect(status().isNoContent());

        verify(interviewAgent).clearSession("sess-1");
    }
}
