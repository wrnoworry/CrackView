package com.crackview.agent.core;

import java.util.List;

/**
 * Abstraction over any LLM API (Claude, GPT, local model, etc.).
 * The ReActEngine only depends on this interface, making it LLM-agnostic.
 */
public interface LlmClient {

    /**
     * Sends a list of messages to the LLM and returns the assistant's reply as plain text.
     *
     * @param messages ordered conversation history
     * @return the LLM's text response
     */
    String chat(List<Message> messages);

    record Message(Role role, String content) {
        public enum Role { SYSTEM, USER, ASSISTANT }

        public static Message system(String content) { return new Message(Role.SYSTEM, content); }
        public static Message user(String content)   { return new Message(Role.USER, content); }
        public static Message assistant(String content) { return new Message(Role.ASSISTANT, content); }
    }
}
