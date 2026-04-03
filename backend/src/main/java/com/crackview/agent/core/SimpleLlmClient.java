package com.crackview.agent.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minimal LLM client that calls the Anthropic Messages API directly via HTTP.
 * No LangChain4j dependency needed — just raw HTTP + JSON.
 *
 * Activated when `llm.provider=anthropic` is set in application properties.
 * For testing without a real API, use MockLlmClient instead.
 */
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "anthropic")
public class SimpleLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(SimpleLlmClient.class);

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public SimpleLlmClient(
            @Value("${llm.anthropic.api-key}") String apiKey,
            @Value("${llm.anthropic.model:claude-sonnet-4-5-20250929}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String chat(List<Message> messages) {
        String systemMsg = messages.stream()
                .filter(m -> m.role() == Message.Role.SYSTEM)
                .map(Message::content)
                .collect(Collectors.joining("\n"));

        String messagesJson = messages.stream()
                .filter(m -> m.role() != Message.Role.SYSTEM)
                .map(m -> String.format("""
                        {"role": "%s", "content": "%s"}""",
                        m.role() == Message.Role.USER ? "user" : "assistant",
                        escapeJson(m.content())))
                .collect(Collectors.joining(",", "[", "]"));

        String requestBody = String.format("""
                {
                    "model": "%s",
                    "max_tokens": 4096,
                    "system": "%s",
                    "messages": %s
                }""", model, escapeJson(systemMsg), messagesJson);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.anthropic.com/v1/messages"))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Anthropic API error {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Anthropic API error: " + response.statusCode());
            }

            return extractTextFromResponse(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call Anthropic API", e);
        }
    }

    private String extractTextFromResponse(String responseBody) {
        // Minimal JSON parsing: extract the text content from the response
        // Response format: {"content": [{"type": "text", "text": "..."}], ...}
        int textStart = responseBody.indexOf("\"text\":\"");
        if (textStart == -1) {
            log.error("Unexpected response format: {}", responseBody);
            return "Error: unexpected API response format";
        }
        textStart += "\"text\":\"".length();
        int textEnd = findClosingQuote(responseBody, textStart);
        return unescapeJson(responseBody.substring(textStart, textEnd));
    }

    private int findClosingQuote(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '"' && s.charAt(i - 1) != '\\') return i;
        }
        return s.length();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String s) {
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
