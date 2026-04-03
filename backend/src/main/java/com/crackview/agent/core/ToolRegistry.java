package com.crackview.agent.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Discovers @AgentTool methods from registered beans, builds ToolDefinitions,
 * and executes tools by name at runtime.
 */
@Component
public class ToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, ToolEntry> tools = new LinkedHashMap<>();

    private record ToolEntry(Object bean, Method method, ToolDefinition definition) {}

    /**
     * Scans a bean for @AgentTool methods and registers them.
     */
    public void register(Object toolBean) {
        for (Method method : toolBean.getClass().getDeclaredMethods()) {
            AgentTool annotation = method.getAnnotation(AgentTool.class);
            if (annotation == null) continue;

            String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();
            List<ToolDefinition.ParameterDefinition> params = new ArrayList<>();

            for (Parameter param : method.getParameters()) {
                ToolParam tp = param.getAnnotation(ToolParam.class);
                String desc = tp != null ? tp.value() : param.getName();
                params.add(new ToolDefinition.ParameterDefinition(
                        param.getName(),
                        param.getType().getSimpleName(),
                        desc
                ));
            }

            ToolDefinition def = new ToolDefinition(toolName, annotation.description(), params);
            tools.put(toolName, new ToolEntry(toolBean, method, def));
            log.info("Registered tool: {}", toolName);
        }
    }

    /**
     * Returns all registered tool definitions (for injecting into the LLM prompt).
     */
    public List<ToolDefinition> getAllDefinitions() {
        return tools.values().stream().map(ToolEntry::definition).toList();
    }

    /**
     * Builds the "Available Tools" section of the system prompt.
     */
    public String buildToolsPrompt() {
        StringBuilder sb = new StringBuilder("Available Tools:\n\n");
        for (ToolDefinition def : getAllDefinitions()) {
            sb.append(def.toPromptString()).append("\n");
        }
        return sb.toString();
    }

    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }

    /**
     * Executes a tool by name with the given JSON argument string.
     * Parses the JSON into method parameters and invokes the method via reflection.
     */
    public String execute(String toolName, String argsJson) {
        ToolEntry entry = tools.get(toolName);
        if (entry == null) {
            return "Error: Unknown tool '" + toolName + "'";
        }

        try {
            Map<String, Object> argsMap = objectMapper.readValue(argsJson, new TypeReference<>() {});
            Method method = entry.method();
            Parameter[] params = method.getParameters();
            Object[] args = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                Object raw = argsMap.get(params[i].getName());
                args[i] = convertArg(raw, params[i].getType());
            }

            Object result = method.invoke(entry.bean(), args);
            return result != null ? result.toString() : "null";
        } catch (Exception e) {
            log.error("Tool execution failed: {} with args {}", toolName, argsJson, e);
            return "Error executing tool '" + toolName + "': " + e.getMessage();
        }
    }

    private Object convertArg(Object raw, Class<?> targetType) {
        if (raw == null) return null;
        if (targetType == String.class) return raw.toString();
        if (targetType == Long.class || targetType == long.class) return Long.valueOf(raw.toString());
        if (targetType == Integer.class || targetType == int.class) return Integer.valueOf(raw.toString());
        if (targetType == Double.class || targetType == double.class) return Double.valueOf(raw.toString());
        if (targetType == Float.class || targetType == float.class) return Float.valueOf(raw.toString());
        if (targetType == Boolean.class || targetType == boolean.class) return Boolean.valueOf(raw.toString());
        return raw;
    }
}
