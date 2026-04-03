package com.crackview.agent.tools;

import com.crackview.agent.core.AgentTool;
import com.crackview.agent.core.ToolParam;
import com.crackview.model.KnowledgeNode;
import com.crackview.model.UserKnowledge;
import com.crackview.repository.KnowledgeNodeRepository;
import com.crackview.repository.UserKnowledgeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KnowledgeGraphTools {

    private final UserKnowledgeRepository userKnowledgeRepository;
    private final KnowledgeNodeRepository knowledgeNodeRepository;

    public KnowledgeGraphTools(UserKnowledgeRepository userKnowledgeRepository,
                               KnowledgeNodeRepository knowledgeNodeRepository) {
        this.userKnowledgeRepository = userKnowledgeRepository;
        this.knowledgeNodeRepository = knowledgeNodeRepository;
    }

    @AgentTool(description = "查询用户在某个领域的知识掌握程度，返回各知识点名称和分数")
    public String queryKnowledgeGraph(
            @ToolParam("用户ID") Long userId,
            @ToolParam("知识领域: java, database, distributed, system-design, 或 all 查全部") String domain
    ) {
        List<UserKnowledge> records = userKnowledgeRepository.findByUserId(userId);

        if (records.isEmpty()) {
            return "该用户暂无知识掌握记录";
        }

        if (!"all".equalsIgnoreCase(domain)) {
            List<Long> domainNodeIds = knowledgeNodeRepository.findByDomain(domain)
                    .stream().map(KnowledgeNode::getId).toList();
            records = records.stream()
                    .filter(r -> domainNodeIds.contains(r.getNodeId()))
                    .toList();
        }

        if (records.isEmpty()) {
            return "该用户在 " + domain + " 领域暂无记录";
        }

        return records.stream()
                .map(r -> {
                    String nodeName = knowledgeNodeRepository.findById(r.getNodeId())
                            .map(KnowledgeNode::getName).orElse("Unknown");
                    return nodeName + ": " + r.getScore() + "分";
                })
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @AgentTool(description = "获取某个领域的所有知识点列表，用于了解知识体系结构")
    public String listKnowledgeNodes(
            @ToolParam("知识领域: java, database, distributed, system-design") String domain
    ) {
        List<KnowledgeNode> nodes = knowledgeNodeRepository.findByDomain(domain);
        if (nodes.isEmpty()) {
            return "未找到领域: " + domain;
        }

        return nodes.stream()
                .map(n -> String.format("[depth=%d] %s", n.getDepth(), n.getName()))
                .collect(Collectors.joining("\n"));
    }

    @AgentTool(description = "获取用户的薄弱知识点（分数低于指定阈值）")
    public String getWeakSpots(
            @ToolParam("用户ID") Long userId,
            @ToolParam("分数阈值，低于此分数视为薄弱") Float threshold
    ) {
        List<UserKnowledge> weakSpots = userKnowledgeRepository
                .findByUserIdAndScoreLessThan(userId, threshold);

        if (weakSpots.isEmpty()) {
            return "未发现低于 " + threshold + " 分的薄弱知识点";
        }

        return weakSpots.stream()
                .map(r -> {
                    String nodeName = knowledgeNodeRepository.findById(r.getNodeId())
                            .map(KnowledgeNode::getName).orElse("Unknown");
                    return nodeName + ": " + r.getScore() + "分";
                })
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
