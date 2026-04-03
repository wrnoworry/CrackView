package com.crackview.agent;

/**
 * Interface for the Interview Agent, enabling easy mocking in tests.
 */
public interface InterviewAgentService {

    String chat(String sessionId, String userMessage);

    void clearSession(String sessionId);
}
