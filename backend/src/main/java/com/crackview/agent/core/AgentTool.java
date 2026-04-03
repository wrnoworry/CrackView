package com.crackview.agent.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a Tool that can be invoked by the ReAct Agent.
 * The engine will discover these methods, register them, and expose them in the LLM prompt.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgentTool {
    /** Tool name. If empty, method name is used. */
    String name() default "";

    /** Human-readable description shown to the LLM. */
    String description();
}
