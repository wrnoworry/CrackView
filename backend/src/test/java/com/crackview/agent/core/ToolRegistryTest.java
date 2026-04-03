package com.crackview.agent.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolRegistryTest {

    private ToolRegistry registry;

    static class SampleTools {
        @AgentTool(description = "Adds two numbers")
        public String add(
                @ToolParam("first number") int a,
                @ToolParam("second number") int b
        ) {
            return String.valueOf(a + b);
        }

        @AgentTool(name = "greet", description = "Says hello to a person")
        public String sayHello(@ToolParam("person's name") String name) {
            return "Hello, " + name + "!";
        }
    }

    @BeforeEach
    void setUp() {
        registry = new ToolRegistry();
        registry.register(new SampleTools());
    }

    @Test
    @DisplayName("register - should discover @AgentTool methods and register them")
    void register_shouldDiscoverTools() {
        assertThat(registry.getAllDefinitions()).hasSize(2);
        assertThat(registry.hasTool("add")).isTrue();
        assertThat(registry.hasTool("greet")).isTrue();
    }

    @Test
    @DisplayName("register - should use custom name when provided")
    void register_shouldUseCustomName() {
        assertThat(registry.hasTool("greet")).isTrue();
        assertThat(registry.hasTool("sayHello")).isFalse();
    }

    @Test
    @DisplayName("execute - should invoke tool and return result")
    void execute_shouldInvokeTool() {
        String result = registry.execute("add", "{\"a\": 3, \"b\": 5}");
        assertThat(result).isEqualTo("8");
    }

    @Test
    @DisplayName("execute - should handle string parameters")
    void execute_shouldHandleStringParams() {
        String result = registry.execute("greet", "{\"name\": \"Alice\"}");
        assertThat(result).isEqualTo("Hello, Alice!");
    }

    @Test
    @DisplayName("execute - should return error for unknown tool")
    void execute_shouldReturnError_forUnknownTool() {
        String result = registry.execute("nonexistent", "{}");
        assertThat(result).startsWith("Error: Unknown tool");
    }

    @Test
    @DisplayName("buildToolsPrompt - should generate tool descriptions")
    void buildToolsPrompt_shouldGenerate() {
        String prompt = registry.buildToolsPrompt();
        assertThat(prompt).contains("add");
        assertThat(prompt).contains("Adds two numbers");
        assertThat(prompt).contains("greet");
        assertThat(prompt).contains("Says hello");
    }

    @Test
    @DisplayName("hasTool - should return false for unregistered tool")
    void hasTool_shouldReturnFalse() {
        assertThat(registry.hasTool("delete")).isFalse();
    }
}
