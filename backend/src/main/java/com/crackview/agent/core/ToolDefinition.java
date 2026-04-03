package com.crackview.agent.core;

import java.util.List;

/**
 * Describes a tool that the Agent can invoke.
 * This metadata is injected into the LLM prompt so it knows what tools exist.
 */
public record ToolDefinition(
        String name,
        String description,
        List<ParameterDefinition> parameters
) {
    public record ParameterDefinition(String name, String type, String description) {}

    public String toPromptString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" — ").append(description).append("\n");
        sb.append("  Parameters:\n");
        for (ParameterDefinition p : parameters) {
            sb.append("    - ").append(p.name()).append(" (").append(p.type()).append("): ").append(p.description()).append("\n");
        }
        return sb.toString();
    }
}
