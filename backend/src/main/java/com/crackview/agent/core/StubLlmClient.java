package com.crackview.agent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fallback LlmClient when no real LLM provider is configured.
 * Returns a canned response so the application can still start and be tested.
 */
@Component
@ConditionalOnMissingBean(value = LlmClient.class, ignored = StubLlmClient.class)
public class StubLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(StubLlmClient.class);

    @Override
    public String chat(List<Message> messages) {
        log.warn("StubLlmClient is active — no real LLM provider configured. "
                + "Set llm.provider=anthropic and provide an API key for real responses.");

        String lastUserMsg = messages.stream()
                .filter(m -> m.role() == Message.Role.USER)
                .reduce((a, b) -> b)
                .map(Message::content)
                .orElse("");

        return "Thought: I am a stub LLM with no real model behind me.\n"
                + "FinalAnswer: [Stub] I received your message: \"" + lastUserMsg
                + "\". Please configure a real LLM provider.";
    }
}
