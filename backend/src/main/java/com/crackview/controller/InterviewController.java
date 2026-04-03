package com.crackview.controller;

import com.crackview.agent.InterviewAgentService;
import com.crackview.dto.ChatRequest;
import com.crackview.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewAgentService interviewAgent;

    public InterviewController(InterviewAgentService interviewAgent) {
        this.interviewAgent = interviewAgent;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String reply = interviewAgent.chat(request.sessionId(), request.message());
        return ResponseEntity.ok(new ChatResponse(request.sessionId(), reply));
    }

    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        interviewAgent.clearSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
