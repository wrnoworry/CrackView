package com.crackview.agent.core;

import com.crackview.agent.core.ReActEngine.ReActResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReActEngineTest {

    private ToolRegistry registry;

    static class CalculatorTools {
        @AgentTool(description = "Adds two numbers")
        public String add(@ToolParam("first") int a, @ToolParam("second") int b) {
            return String.valueOf(a + b);
        }

        @AgentTool(description = "Multiplies two numbers")
        public String multiply(@ToolParam("first") int a, @ToolParam("second") int b) {
            return String.valueOf(a * b);
        }
    }

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
        registry.register(new CalculatorTools());
    }

    @Test
    @DisplayName("should return FinalAnswer directly when LLM gives immediate answer")
    void directAnswer() {
        MockLlmClient mock = new MockLlmClient(
                "Thought: The user just said hello, I can respond directly.\nFinalAnswer: Hello! How can I help you today?"
        );

        ReActEngine engine = new ReActEngine(mock, registry);
        ReActResult result = engine.run("You are a helpful assistant.", "Hello", List.of());

        assertThat(result.answer()).isEqualTo("Hello! How can I help you today?");
        assertThat(result.steps()).hasSize(1);
        assertThat(result.steps().get(0).finalAnswer()).isNotNull();
        assertThat(result.steps().get(0).toolName()).isNull();
    }

    @Test
    @DisplayName("should execute one tool call then return FinalAnswer")
    void singleToolCall() {
        MockLlmClient mock = new MockLlmClient(
                // First: LLM wants to use a tool
                """
                Thought: I need to add 3 and 5.
                Action: add
                ActionInput: {"a": 3, "b": 5}""",
                // Second: After seeing the observation, LLM gives final answer
                """
                Thought: The result is 8.
                FinalAnswer: 3 + 5 = 8"""
        );

        ReActEngine engine = new ReActEngine(mock, registry);
        ReActResult result = engine.run("You are a calculator.", "What is 3 + 5?", List.of());

        assertThat(result.answer()).isEqualTo("3 + 5 = 8");
        assertThat(result.steps()).hasSize(2);
        assertThat(result.steps().get(0).toolName()).isEqualTo("add");
        assertThat(result.steps().get(0).observation()).isEqualTo("8");
        assertThat(result.steps().get(1).finalAnswer()).isEqualTo("3 + 5 = 8");
    }

    @Test
    @DisplayName("should execute multiple tool calls in sequence")
    void multipleToolCalls() {
        MockLlmClient mock = new MockLlmClient(
                """
                Thought: First I need to add 2 and 3.
                Action: add
                ActionInput: {"a": 2, "b": 3}""",
                """
                Thought: Now multiply the result (5) by 4.
                Action: multiply
                ActionInput: {"a": 5, "b": 4}""",
                """
                Thought: (2+3) * 4 = 20.
                FinalAnswer: The answer is 20."""
        );

        ReActEngine engine = new ReActEngine(mock, registry);
        ReActResult result = engine.run("You are a calculator.", "(2+3) * 4 = ?", List.of());

        assertThat(result.answer()).isEqualTo("The answer is 20.");
        assertThat(result.steps()).hasSize(3);
        assertThat(result.steps().get(0).toolName()).isEqualTo("add");
        assertThat(result.steps().get(0).observation()).isEqualTo("5");
        assertThat(result.steps().get(1).toolName()).isEqualTo("multiply");
        assertThat(result.steps().get(1).observation()).isEqualTo("20");
    }

    @Test
    @DisplayName("should handle unknown tool gracefully")
    void unknownTool() {
        MockLlmClient mock = new MockLlmClient(
                """
                Thought: Let me try a tool that doesn't exist.
                Action: divide
                ActionInput: {"a": 10, "b": 2}""",
                """
                Thought: That tool doesn't exist, let me answer directly.
                FinalAnswer: I couldn't find the divide tool."""
        );

        ReActEngine engine = new ReActEngine(mock, registry);
        ReActResult result = engine.run("assistant", "10 / 2?", List.of());

        assertThat(result.steps().get(0).observation()).contains("Unknown tool");
    }

    @Test
    @DisplayName("should stop after max iterations")
    void maxIterations() {
        // LLM always wants to use tools, never gives FinalAnswer
        MockLlmClient mock = new MockLlmClient();
        for (int i = 0; i < 10; i++) {
            mock.addResponse("""
                    Thought: Let me add again.
                    Action: add
                    ActionInput: {"a": 1, "b": 1}""");
        }

        ReActEngine engine = new ReActEngine(mock, registry, 3);
        ReActResult result = engine.run("assistant", "keep adding", List.of());

        assertThat(result.steps()).hasSize(3);
        assertThat(result.answer()).contains("add");
    }

    @Test
    @DisplayName("should handle LLM response not matching ReAct format")
    void nonReActFormat() {
        MockLlmClient mock = new MockLlmClient(
                "I'm just going to answer directly without using the format."
        );

        ReActEngine engine = new ReActEngine(mock, registry);
        ReActResult result = engine.run("assistant", "hi", List.of());

        assertThat(result.answer()).isEqualTo("I'm just going to answer directly without using the format.");
    }
}
