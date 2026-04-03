package com.crackview.agent.tools;

import com.crackview.agent.core.AgentTool;
import com.crackview.agent.core.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Provides interview questions to the Agent.
 * MVP: returns from a hardcoded bank. Future: database or AI-generated.
 */
@Component
public class QuestionBankTools {

    private static final Map<String, List<String>> QUESTION_BANK = Map.ofEntries(
            Map.entry("HashMap", List.of(
                    "HashMap的底层数据结构是什么？",
                    "HashMap什么时候会从链表转为红黑树？",
                    "HashMap的扩容机制是怎样的？",
                    "HashMap为什么线程不安全？"
            )),
            Map.entry("线程池", List.of(
                    "ThreadPoolExecutor有哪些核心参数？",
                    "线程池的拒绝策略有哪些？",
                    "如何合理设置线程池大小？",
                    "线程池的工作流程是怎样的？"
            )),
            Map.entry("Redis", List.of(
                    "Redis有哪些数据结构？各自的使用场景？",
                    "Redis的持久化方式有哪些？RDB和AOF的区别？",
                    "什么是缓存穿透、击穿、雪崩？如何解决？",
                    "Redis分布式锁怎么实现？"
            )),
            Map.entry("GC", List.of(
                    "JVM有哪些垃圾回收算法？",
                    "CMS和G1收集器的区别？",
                    "什么情况下会触发Full GC？",
                    "如何排查GC问题？"
            )),
            Map.entry("系统设计", List.of(
                    "如何设计一个短链系统？",
                    "如何设计一个秒杀系统？",
                    "如何保证系统的高可用？",
                    "你会如何设计一个消息推送系统？"
            ))
    );

    @AgentTool(description = "获取某个主题的面试题目列表")
    public String getTopicQuestions(
            @ToolParam("知识主题，如: HashMap, 线程池, Redis, GC, 系统设计") String topic,
            @ToolParam("难度: easy, medium, hard") String difficulty
    ) {
        List<String> questions = QUESTION_BANK.get(topic);
        if (questions == null) {
            return "暂无该主题的题目。可用主题: " + String.join(", ", QUESTION_BANK.keySet());
        }

        int count = switch (difficulty.toLowerCase()) {
            case "easy" -> Math.min(1, questions.size());
            case "hard" -> questions.size();
            default -> Math.min(2, questions.size());
        };

        return questions.subList(0, count).toString();
    }
}
