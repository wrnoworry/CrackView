package com.crackview.agent;

import com.crackview.agent.core.LlmClient;
import com.crackview.agent.core.LlmClient.Message;
import com.crackview.agent.core.ReActEngine;
import com.crackview.agent.core.ReActEngine.ReActResult;
import com.crackview.agent.core.ToolRegistry;
import com.crackview.agent.tools.KnowledgeGraphTools;
import com.crackview.agent.tools.QuestionBankTools;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InterviewAgent implements InterviewAgentService {

    private static final String SYSTEM_PROMPT = """
            你是一个严格但友善的技术面试官，名叫 CrackView Coach。
            你的目标是通过追问找到候选人的知识边界。

            规则：
            1. 如果候选人回答正确但不深入，追问细节和底层原理
            2. 如果候选人回答错误，先简短纠正，然后从更基础的角度重新提问
            3. 如果候选人连续3次回答正确，升级到更深层或相关联的主题
            4. 每个session控制在5-8轮追问
            5. 使用工具来查询候选人的知识掌握情况，动态调整提问策略
            6. 用中文回答

            追问策略：
            - 表面回答 → 追问底层实现
            - 知道原理 → 追问实际场景和取舍
            - 单一知识点 → 追问与其他知识点的关联
            """;

    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final KnowledgeGraphTools knowledgeGraphTools;
    private final QuestionBankTools questionBankTools;

    /** In-memory session store. Replace with Redis for production. */
    private final Map<String, List<Message>> sessionMemory = new ConcurrentHashMap<>();

    private ReActEngine engine;

    public InterviewAgent(LlmClient llmClient,
                          ToolRegistry toolRegistry,
                          KnowledgeGraphTools knowledgeGraphTools,
                          QuestionBankTools questionBankTools) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.knowledgeGraphTools = knowledgeGraphTools;
        this.questionBankTools = questionBankTools;
    }

    @PostConstruct
    void init() {
        toolRegistry.register(knowledgeGraphTools);
        toolRegistry.register(questionBankTools);
        engine = new ReActEngine(llmClient, toolRegistry);
    }

    /**
     * Main entry point: chat with the interview agent.
     *
     * @param sessionId unique session identifier
     * @param userMessage user's message
     * @return the agent's reply
     */
    public String chat(String sessionId, String userMessage) {
        List<Message> history = sessionMemory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        ReActResult result = engine.run(SYSTEM_PROMPT, userMessage, new ArrayList<>(history));

        // Update session memory with the user message and assistant reply
        history.add(Message.user(userMessage));
        history.add(Message.assistant(result.answer()));

        return result.answer();
    }

    /**
     * Returns the conversation history for a given session.
     */
    public List<Message> getHistory(String sessionId) {
        return sessionMemory.getOrDefault(sessionId, List.of());
    }

    /**
     * Clears a session's memory.
     */
    public void clearSession(String sessionId) {
        sessionMemory.remove(sessionId);
    }
}
