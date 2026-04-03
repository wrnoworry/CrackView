package com.crackview.agent.core;

import com.crackview.agent.core.LlmClient.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hand-written ReAct (Reasoning + Acting) engine.
 *
 * The loop:
 *   1. Send conversation to LLM with system prompt containing tool descriptions
 *   2. Parse LLM response for Thought / Action / ActionInput / FinalAnswer
 *   3. If Action found → execute tool → append Observation → go to 1
 *   4. If FinalAnswer found → return it
 *   5. If max iterations reached → return whatever we have
 */
public class ReActEngine {

    private static final Logger log = LoggerFactory.getLogger(ReActEngine.class);

    private static final Pattern ACTION_PATTERN = Pattern.compile(
            "Action\\s*:\\s*(.+?)\\s*(?:\\n|$)", Pattern.MULTILINE);
    private static final Pattern ACTION_INPUT_PATTERN = Pattern.compile(
            "ActionInput\\s*:\\s*(.+?)\\s*(?:\\n|$)", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern FINAL_ANSWER_PATTERN = Pattern.compile(
            "FinalAnswer\\s*:\\s*(.+)", Pattern.MULTILINE | Pattern.DOTALL);

    private static final String REACT_INSTRUCTION = """
            
            ## Response Format
            
            You must respond in ONE of these two formats:
            
            **Format A — When you need to use a tool:**
            Thought: <your reasoning about what to do next>
            Action: <tool_name>
            ActionInput: <JSON object with parameters, e.g. {"userId": 1, "domain": "java"}>
            
            **Format B — When you have the final answer:**
            Thought: <your final reasoning>
            FinalAnswer: <your complete answer to the user>
            
            Rules:
            - Always start with Thought
            - Only use ONE tool per turn
            - ActionInput must be valid JSON
            - Do NOT output anything after ActionInput or FinalAnswer
            """;

    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final int maxIterations;

    public ReActEngine(LlmClient llmClient, ToolRegistry toolRegistry) {
        this(llmClient, toolRegistry, 8);
    }

    public ReActEngine(LlmClient llmClient, ToolRegistry toolRegistry, int maxIterations) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.maxIterations = maxIterations;
    }

    /**
     * Runs the full ReAct loop.
     *
     * @param systemPrompt  the agent's persona and rules
     * @param userMessage   the user's input
     * @param chatHistory   prior conversation turns (can be empty for new session)
     * @return the agent's final answer
     */
    public ReActResult run(String systemPrompt, String userMessage, List<Message> chatHistory) {

        // Build full system prompt = persona + tool list + format instructions
        String fullSystemPrompt = systemPrompt + "\n\n"
                + toolRegistry.buildToolsPrompt() + "\n"
                + REACT_INSTRUCTION;

        List<Message> messages = new ArrayList<>();
        messages.add(Message.system(fullSystemPrompt));
        messages.addAll(chatHistory);
        messages.add(Message.user(userMessage));

        List<ReActStep> steps = new ArrayList<>();

        for (int i = 0; i < maxIterations; i++) {
            log.debug("=== ReAct iteration {} ===", i + 1);

            String llmResponse = llmClient.chat(messages);
            log.debug("LLM response:\n{}", llmResponse);

            // Check for FinalAnswer
            Matcher finalMatcher = FINAL_ANSWER_PATTERN.matcher(llmResponse);
            if (finalMatcher.find()) {
                String finalAnswer = finalMatcher.group(1).trim();
                steps.add(new ReActStep(extractThought(llmResponse), null, null, null, finalAnswer));
                return new ReActResult(finalAnswer, steps, messages);
            }

            // Check for Action
            Matcher actionMatcher = ACTION_PATTERN.matcher(llmResponse);
            Matcher inputMatcher = ACTION_INPUT_PATTERN.matcher(llmResponse);

            if (actionMatcher.find() && inputMatcher.find()) {
                String toolName = actionMatcher.group(1).trim();
                String actionInput = inputMatcher.group(1).trim();
                String thought = extractThought(llmResponse);

                log.info("Tool call: {}({})", toolName, actionInput);

                // Execute the tool
                String observation;
                if (toolRegistry.hasTool(toolName)) {
                    observation = toolRegistry.execute(toolName, actionInput);
                } else {
                    observation = "Error: Unknown tool '" + toolName + "'. Available tools: "
                            + toolRegistry.getAllDefinitions().stream()
                            .map(ToolDefinition::name)
                            .toList();
                }

                log.info("Observation: {}", observation);
                steps.add(new ReActStep(thought, toolName, actionInput, observation, null));

                // Append the assistant's response and the observation to messages
                messages.add(Message.assistant(llmResponse));
                messages.add(Message.user("Observation: " + observation));
            } else {
                // LLM didn't follow the format — treat entire response as final answer
                log.warn("LLM response didn't match ReAct format, treating as final answer");
                steps.add(new ReActStep(null, null, null, null, llmResponse));
                return new ReActResult(llmResponse, steps, messages);
            }
        }

        // Max iterations reached
        log.warn("ReAct loop reached max iterations ({})", maxIterations);
        String fallback = "I've done my analysis. Based on what I've gathered:\n"
                + steps.stream()
                .filter(s -> s.observation() != null)
                .map(s -> "- " + s.toolName() + ": " + s.observation())
                .reduce("", (a, b) -> a + "\n" + b);
        return new ReActResult(fallback, steps, messages);
    }

    private String extractThought(String response) {
        Pattern thoughtPattern = Pattern.compile("Thought\\s*:\\s*(.+?)(?=\\nAction|\\nFinalAnswer|$)",
                Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = thoughtPattern.matcher(response);
        return m.find() ? m.group(1).trim() : null;
    }

    /**
     * One step of the ReAct loop.
     */
    public record ReActStep(
            String thought,
            String toolName,
            String actionInput,
            String observation,
            String finalAnswer
    ) {}

    /**
     * The complete result of a ReAct execution.
     */
    public record ReActResult(
            String answer,
            List<ReActStep> steps,
            List<Message> updatedMessages
    ) {}
}
