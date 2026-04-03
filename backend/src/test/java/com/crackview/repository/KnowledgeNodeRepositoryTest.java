package com.crackview.repository;

import com.crackview.model.KnowledgeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class KnowledgeNodeRepositoryTest {

    @Autowired
    private KnowledgeNodeRepository nodeRepository;

    private KnowledgeNode rootNode;
    private KnowledgeNode childNode;

    @BeforeEach
    void setUp() {
        nodeRepository.deleteAll();

        rootNode = nodeRepository.save(KnowledgeNode.builder()
                .name("Java基础")
                .domain("java")
                .depth(0)
                .description("Java核心知识")
                .build());

        childNode = nodeRepository.save(KnowledgeNode.builder()
                .name("JVM")
                .domain("java")
                .parent(rootNode)
                .depth(1)
                .description("JVM原理")
                .build());

        nodeRepository.save(KnowledgeNode.builder()
                .name("分布式系统")
                .domain("distributed")
                .depth(0)
                .description("分布式核心知识")
                .build());
    }

    @Test
    @DisplayName("save - should persist node with auto-generated id and uuid")
    void save_shouldPersistNode() {
        assertThat(rootNode.getId()).isNotNull();
        assertThat(rootNode.getUuid()).isNotNull().hasSize(36);
    }

    @Test
    @DisplayName("findByUuid - should return node when uuid exists")
    void findByUuid_shouldReturnNode() {
        Optional<KnowledgeNode> found = nodeRepository.findByUuid(rootNode.getUuid());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Java基础");
    }

    @Test
    @DisplayName("findByDomain - should return all nodes in given domain")
    void findByDomain_shouldReturnDomainNodes() {
        List<KnowledgeNode> javaNodes = nodeRepository.findByDomain("java");
        assertThat(javaNodes).hasSize(2);

        List<KnowledgeNode> distNodes = nodeRepository.findByDomain("distributed");
        assertThat(distNodes).hasSize(1);
    }

    @Test
    @DisplayName("findByParentId - should return direct children")
    void findByParentId_shouldReturnChildren() {
        List<KnowledgeNode> children = nodeRepository.findByParentId(rootNode.getId());
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getName()).isEqualTo("JVM");
    }

    @Test
    @DisplayName("findByParentIsNull - should return only root nodes")
    void findByParentIsNull_shouldReturnRootNodes() {
        List<KnowledgeNode> roots = nodeRepository.findByParentIsNull();
        assertThat(roots).hasSize(2);
    }

    @Test
    @DisplayName("findByDepth - should return nodes at specific depth")
    void findByDepth_shouldReturnCorrectDepth() {
        List<KnowledgeNode> depthZero = nodeRepository.findByDepth(0);
        assertThat(depthZero).hasSize(2);

        List<KnowledgeNode> depthOne = nodeRepository.findByDepth(1);
        assertThat(depthOne).hasSize(1);
    }

    @Test
    @DisplayName("findByDomainAndDepth - should filter by both domain and depth")
    void findByDomainAndDepth_shouldFilter() {
        List<KnowledgeNode> result = nodeRepository.findByDomainAndDepth("java", 0);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java基础");
    }
}
