package com.crackview.agent;

import com.crackview.agent.core.MockLlmClient;
import com.crackview.agent.core.ToolRegistry;
import com.crackview.agent.tools.KnowledgeGraphTools;
import com.crackview.agent.tools.QuestionBankTools;
import com.crackview.repository.KnowledgeNodeRepository;
import com.crackview.repository.UserKnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class InterviewAgentTest {

    @Mock
    private UserKnowledgeRepository userKnowledgeRepository;

    @Mock
    private KnowledgeNodeRepository knowledgeNodeRepository;

    private InterviewAgent agent;
    private MockLlmClient mockLlm;

    @BeforeEach
    void setUp() {
        mockLlm = new MockLlmClient();
        ToolRegistry registry = new ToolRegistry();
        KnowledgeGraphTools kgTools = new KnowledgeGraphTools(userKnowledgeRepository, knowledgeNodeRepository);
        QuestionBankTools qbTools = new QuestionBankTools();
        agent = new InterviewAgent(mockLlm, registry, kgTools, qbTools);
        agent.init();
    }

    @Test
    @DisplayName("chat - should return final answer from LLM")
    void chat_shouldReturnAnswer() {
        mockLlm.addResponse("""
                Thought: The user wants to start an interview. Let me ask a question.
                FinalAnswer: 好的，让我们开始面试。请介绍一下HashMap的底层数据结构。""");

        String reply = agent.chat("session-1", "开始面试，主题是Java集合");
        assertThat(reply).contains("HashMap");
    }

    @Test
    @DisplayName("chat - should maintain session memory across turns")
    void chat_shouldMaintainMemory() {
        mockLlm.addResponse("""
                Thought: Starting interview.
                FinalAnswer: 请描述HashMap的底层实现。""");
        mockLlm.addResponse("""
                Thought: The candidate answered well, let me follow up.
                FinalAnswer: 不错！那HashMap什么时候会从链表转为红黑树？""");

        agent.chat("session-2", "开始面试");
        String reply2 = agent.chat("session-2", "HashMap基于数组加链表加红黑树");

        assertThat(reply2).contains("红黑树");
        assertThat(agent.getHistory("session-2")).hasSize(4); // 2 user + 2 assistant
    }

    @Test
    @DisplayName("chat - should use tools when LLM requests them")
    void chat_shouldUseTools() {
        mockLlm.addResponse("""
                Thought: Let me check what questions are available for Redis.
                Action: getTopicQuestions
                ActionInput: {"topic": "Redis", "difficulty": "medium"}""");
        mockLlm.addResponse("""
                Thought: Got the questions. Let me ask the first one.
                FinalAnswer: 好的，让我问你一个Redis问题：Redis有哪些数据结构？""");

        String reply = agent.chat("session-3", "面试Redis");
        assertThat(reply).contains("Redis");
    }

    @Test
    @DisplayName("clearSession - should remove session memory")
    void clearSession_shouldRemoveMemory() {
        mockLlm.addResponse("Thought: hi\nFinalAnswer: Hello!");
        agent.chat("session-4", "hello");
        assertThat(agent.getHistory("session-4")).isNotEmpty();

        agent.clearSession("session-4");
        assertThat(agent.getHistory("session-4")).isEmpty();
    }

    @Test
    @DisplayName("getHistory - should return empty list for unknown session")
    void getHistory_shouldReturnEmpty() {
        assertThat(agent.getHistory("unknown")).isEmpty();
    }
}
