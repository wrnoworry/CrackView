package com.crackview.agent.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A mock LlmClient for testing. Returns pre-configured responses in sequence.
 */
public class MockLlmClient implements LlmClient {

    private final Queue<String> responses = new LinkedList<>();

    public MockLlmClient(String... responses) {
        this.responses.addAll(List.of(responses));
    }

    public void addResponse(String response) {
        responses.add(response);
    }

    @Override
    public String chat(List<Message> messages) {
        if (responses.isEmpty()) {
            return "Thought: I have no more responses configured.\nFinalAnswer: Mock complete.";
        }
        return responses.poll();
    }
}
